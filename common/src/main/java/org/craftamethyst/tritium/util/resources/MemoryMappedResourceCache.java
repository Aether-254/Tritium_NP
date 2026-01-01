package org.craftamethyst.tritium.util.resources;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class MemoryMappedResourceCache implements IResourceCache {
    private static final Cleaner CLEANER = Cleaner.create();

    private final Path zipPath;
    private final FileChannel zipChannel;
    private final OverlayLayout overlayLayout;
    private final MemoryMapper memoryMapper;

    private final Map<String, FileRegion> fileRegions = new ConcurrentHashMap<>();
    private final Map<String, ByteBuffer> activeBuffers = new ConcurrentHashMap<>();
    private final Map<PackType, Set<String>> namespaceRegistry = new EnumMap<>(PackType.class);
    private final Map<String, ResourceRecord> resourceRegistry = new ConcurrentHashMap<>();

    private final LayeredCache layeredCache;
    private final boolean useRegionMapping;
    private final boolean isZipFile;
    private final Map<String, byte[]> zipFileCache = new ConcurrentHashMap<>();

    public MemoryMappedResourceCache(Path zipPath, List<String> overlays,
                                     OverlayLayout layout) throws IOException {
        this(zipPath, overlays, layout, true, 64 * 1024 * 1024);
    }

    public MemoryMappedResourceCache(Path zipPath, List<String> overlays,
                                     OverlayLayout layout, boolean regionMapping,
                                     long mappingLimit) throws IOException {
        this.zipPath = zipPath;
        this.isZipFile = Files.isRegularFile(zipPath) && zipPath.toString().toLowerCase().endsWith(".zip");

        if (isZipFile) {
            this.zipChannel = null;
            this.memoryMapper = null;
        } else {
            this.zipChannel = FileChannel.open(zipPath, StandardOpenOption.READ);
            this.memoryMapper = new MemoryMapper(zipChannel, mappingLimit);
        }

        this.overlayLayout = layout;
        this.useRegionMapping = regionMapping && !isZipFile;

        this.layeredCache = new LayeredCache(8192, 1024 * 1024);

        for (PackType type : PackType.values()) {
            namespaceRegistry.put(type, ConcurrentHashMap.newKeySet());
        }

        buildResourceCatalog();
    }

    private void buildResourceCatalog() throws IOException {
        if (isZipFile) {
            buildResourceCatalogFromZip();
        } else {
            buildResourceCatalogFromFileSystem();
        }
    }

    private void buildResourceCatalogFromZip() throws IOException {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    String relativePath = normalizePath(entry.getName());
                    fileRegions.put(relativePath, new FileRegion(0, 0));
                    indexResourcePath(relativePath);
                    extractNamespace(relativePath);

                    byte[] data = readZipEntry(zipFile, entry);
                    if (data != null) {
                        zipFileCache.put(relativePath, data);
                    }
                }
            }
        }
    }

    private byte[] readZipEntry(ZipFile zipFile, ZipEntry entry) throws IOException {
        try (var inputStream = zipFile.getInputStream(entry)) {
            return inputStream.readAllBytes();
        }
    }

    private void buildResourceCatalogFromFileSystem() throws IOException {
        try (Stream<Path> walk = Files.walk(zipFileSystem().getPath("/"))) {
            walk.filter(Files::isRegularFile)
                    .parallel()
                    .forEach(this::catalogFile);
        }
    }

    private void catalogFile(Path filePath) {
        String relativePath = normalizePath(filePath);

        if (useRegionMapping) {
            try {
                FileRegion region = memoryMapper.mapRegion(filePath);
                fileRegions.put(relativePath, region);
            } catch (IOException e) {
                fileRegions.put(relativePath, new FileRegion(0, 0));
            }
        } else {
            fileRegions.put(relativePath, new FileRegion(0, 0));
        }

        indexResourcePath(relativePath);
        extractNamespace(relativePath);
    }

    private void indexResourcePath(String path) {
        for (PackType type : PackType.values()) {
            String typePrefix = type.getDirectory() + "/";
            if (path.startsWith(typePrefix)) {
                String remaining = path.substring(typePrefix.length());
                int namespaceEnd = remaining.indexOf('/');
                if (namespaceEnd > 0) {
                    String namespace = remaining.substring(0, namespaceEnd);
                    String resourcePath = remaining.substring(namespaceEnd + 1);

                    ResourceRecord record = resourceRegistry.computeIfAbsent(
                            namespace + ":" + resourcePath,
                            k -> new ResourceRecord(type, path)
                    );

                    record.addOverlayVariant(determineOverlayLayer(path), path);
                }
                break;
            }
        }
    }

    private void extractNamespace(String path) {
        for (PackType type : PackType.values()) {
            String namespace = extractNamespace(path, type);
            if (namespace != null && isValidNamespace(namespace)) {
                namespaceRegistry.get(type).add(namespace);
            }
        }
    }

    private static boolean isValidNamespace(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') ||
                    c == '_' || c == '-' || c == '.')) {
                return false;
            }
        }
        return !s.isEmpty();
    }

    private String extractNamespace(String path, PackType type) {
        String remaining = path;

        if (overlayLayout.isOverlayPath(path)) {
            int firstSlash = path.indexOf('/');
            if (firstSlash == -1) return null;
            remaining = path.substring(firstSlash + 1);
        }

        String typeDir = type.getDirectory() + "/";
        if (!remaining.startsWith(typeDir)) return null;

        String afterType = remaining.substring(typeDir.length());
        int namespaceEnd = afterType.indexOf('/');
        return namespaceEnd == -1 ? null : afterType.substring(0, namespaceEnd);
    }

    private String determineOverlayLayer(String path) {
        if (!overlayLayout.isOverlayPath(path)) {
            return "";
        }
        int firstSlash = path.indexOf('/');
        return firstSlash == -1 ? "" : path.substring(0, firstSlash);
    }

    @Override
    public byte @Nullable [] getResource(PackType type, ResourceLocation location) {
        ResourceRecord record = resourceRegistry.get(location.getNamespace() + ":" + location.getPath());
        if (record == null || record.type != type) {
            return null;
        }

        for (String overlay : overlayLayout.getSearchOrder()) {
            String variantPath = record.getOverlayVariant(overlay);
            if (variantPath != null) {
                return getResourceData(variantPath);
            }
        }

        return null;
    }

    private byte @Nullable [] getResourceData(String path) {
        if (isZipFile) {
            return zipFileCache.get(path);
        }

        ByteBuffer cached = layeredCache.get(path);
        if (cached != null && cached.hasArray()) {
            return cached.array();
        }

        FileRegion region = fileRegions.get(path);
        if (region == null || region.size == 0) {
            return null;
        }

        try {
            ByteBuffer buffer;
            if (useRegionMapping && region.offset > 0) {
                buffer = memoryMapper.mapRegion(region.offset, region.size);
            } else {
                buffer = readFileContent(path);
            }

            if (buffer != null) {
                layeredCache.put(path, buffer);
                return buffer.array();
            }
        } catch (IOException e) {
        }

        return readFileContentFallback(path);
    }

    private ByteBuffer readFileContent(String path) throws IOException {
        Path filePath = zipFileSystem().getPath("/" + path);
        if (!Files.exists(filePath)) {
            return null;
        }

        long size = Files.size(filePath);
        if (size > Integer.MAX_VALUE) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.allocateDirect((int) size);
        try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            channel.read(buffer);
        }
        buffer.flip();

        return buffer;
    }

    private byte @Nullable [] readFileContentFallback(String path) {
        try {
            Path filePath = zipFileSystem().getPath("/" + path);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public byte @Nullable [] getRootResource(String... parts) {
        String path = String.join("/", parts);

        if (isZipFile) {
            return zipFileCache.get(path);
        }

        ByteBuffer cached = layeredCache.get(path);
        if (cached != null && cached.hasArray()) {
            return cached.array();
        }

        return getResourceData(path);
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return Collections.unmodifiableSet(namespaceRegistry.getOrDefault(type, Collections.emptySet()));
    }

    @Override
    public void listResources(PackType type, String namespace,
                              String path, ResourceConsumer consumer) {
        String namespacePrefix = type.getDirectory() + '/' + namespace + '/';
        String directoryPrefix = namespacePrefix + (path.isEmpty() ? "" : path + '/');

        Set<String> processed = ConcurrentHashMap.newKeySet();

        for (String overlay : overlayLayout.getSearchOrder()) {
            String overlayPrefix = overlay.isEmpty() ? "" : overlay + "/";
            String fullNamespacePrefix = overlayPrefix + namespacePrefix;
            String fullDirectoryPrefix = overlayPrefix + directoryPrefix;

            fileRegions.forEach((filePath, region) -> {
                if (filePath.startsWith(fullNamespacePrefix)) {
                    processResourceListing(filePath, region, fullNamespacePrefix,
                            fullDirectoryPrefix, namespace,
                            path.isEmpty(), processed, consumer);
                }
            });
        }
    }

    private void processResourceListing(String filePath, FileRegion region,
                                        String namespacePrefix, String directoryPrefix,
                                        String namespace, boolean isDirectory,
                                        Set<String> processed, ResourceConsumer consumer) {
        if (isDirectory) {
            int nextSlash = filePath.indexOf('/', namespacePrefix.length());
            if (nextSlash > 0 && nextSlash < filePath.length() - 1) {
                String relative = filePath.substring(namespacePrefix.length(), nextSlash + 1);
                if (processed.add(relative)) {
                    emitResource(relative, filePath, namespace, consumer);
                }
            }
        } else if (filePath.startsWith(directoryPrefix)) {
            String relative = filePath.substring(namespacePrefix.length());
            if (processed.add(relative)) {
                emitResource(relative, filePath, namespace, consumer);
            }
        }
    }

    private void emitResource(String relativePath, String filePath,
                              String namespace, ResourceConsumer consumer) {
        ResourceLocation location = ResourceLocation.tryBuild(namespace, relativePath);
        if (location != null) {
            byte[] data = getResourceData(filePath);
            if (data != null) {
                consumer.accept(location, data);
            }
        }
    }

    @Override
    public void clear() {
        layeredCache.clear();
        activeBuffers.clear();
        fileRegions.clear();
        namespaceRegistry.clear();
        resourceRegistry.clear();
        zipFileCache.clear();

        if (memoryMapper != null) {
            memoryMapper.cleanup();
        }

        try {
            if (zipChannel != null && zipChannel.isOpen()) {
                zipChannel.close();
            }
        } catch (IOException ignored) {}

        if (zipFileSystem != null && zipFileSystem.isOpen()) {
            try {
                zipFileSystem.close();
            } catch (IOException ignored) {}
        }
    }

    private FileSystem zipFileSystem() throws IOException {
        if (zipFileSystem == null || !zipFileSystem.isOpen()) {
            synchronized (this) {
                if (zipFileSystem == null || !zipFileSystem.isOpen()) {
                    zipFileSystem = FileSystems.newFileSystem(zipPath);
                }
            }
        }
        return zipFileSystem;
    }

    private String normalizePath(Path path) {
        String str = path.toString();
        return str.startsWith("/") ? str.substring(1) : str;
    }

    private String normalizePath(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private static final class MemoryMapper {
        private final FileChannel channel;
        private final long mappingLimit;
        private final Map<Long, MappedByteBuffer> mappedRegions = new ConcurrentHashMap<>();

        MemoryMapper(FileChannel channel, long mappingLimit) {
            this.channel = channel;
            this.mappingLimit = mappingLimit;
        }

        ByteBuffer mapRegion(long offset, int size) throws IOException {
            if (size > mappingLimit) {
                return null;
            }

            return mappedRegions.computeIfAbsent(offset, k -> {
                try {
                    MappedByteBuffer buffer = channel.map(
                            FileChannel.MapMode.READ_ONLY, offset, size
                    );
                    CLEANER.register(this, new BufferCleaner(buffer));
                    return buffer;
                } catch (IOException e) {
                    return null;
                }
            });
        }

        FileRegion mapRegion(Path filePath) throws IOException {
            try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ)) {
                long size = Files.size(filePath);
                if (size <= mappingLimit) {
                    MappedByteBuffer buffer = fileChannel.map(
                            FileChannel.MapMode.READ_ONLY, 0, size
                    );
                    CLEANER.register(this, new BufferCleaner(buffer));
                    return new FileRegion(0, (int) size);
                }
            }
            return new FileRegion(0, 0);
        }

        void cleanup() {
            mappedRegions.clear();
        }

        private static class BufferCleaner implements Runnable {
            private final MappedByteBuffer buffer;

            BufferCleaner(MappedByteBuffer buffer) {
                this.buffer = buffer;
            }

            @Override
            public void run() {
                if (buffer != null && buffer.isDirect()) {
                    sun.misc.Unsafe unsafe = getUnsafe();
                    if (unsafe != null) {
                        try {
                            unsafe.invokeCleaner(buffer);
                        } catch (Exception ignored) {}
                    }
                }
            }

            private sun.misc.Unsafe getUnsafe() {
                try {
                    return sun.misc.Unsafe.getUnsafe();
                } catch (Exception e) {
                    return null;
                }
            }
        }
    }

    private static final class LayeredCache {
        private final Map<String, ByteBuffer> l1Cache;
        private final Map<String, ByteBuffer> l2Cache;
        private final int l1Size;
        private final int l2Size;

        LayeredCache(int l1Size, int l2Size) {
            this.l1Size = l1Size;
            this.l2Size = l2Size;
            this.l1Cache = Collections.synchronizedMap(new LruCache(l1Size));
            this.l2Cache = Collections.synchronizedMap(new LruCache(l2Size));
        }

        ByteBuffer get(String key) {
            ByteBuffer buffer = l1Cache.get(key);
            if (buffer != null) {
                return buffer;
            }

            buffer = l2Cache.get(key);
            if (buffer != null) {
                l1Cache.put(key, buffer);
            }

            return buffer;
        }

        void put(String key, ByteBuffer buffer) {
            if (buffer.remaining() <= l1Size / 4) {
                l1Cache.put(key, buffer);
            } else if (buffer.remaining() <= l2Size / 4) {
                l2Cache.put(key, buffer);
            }
        }

        void clear() {
            l1Cache.clear();
            l2Cache.clear();
        }

        private static class LruCache extends LinkedHashMap<String, ByteBuffer> {
            private final int capacity;

            LruCache(int capacity) {
                super(16, 0.75f, true);
                this.capacity = capacity;
            }

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, ByteBuffer> eldest) {
                return size() > capacity;
            }
        }
    }

    private static final class FileRegion {
        final long offset;
        final int size;

        FileRegion(long offset, int size) {
            this.offset = offset;
            this.size = size;
        }
    }

    private static final class ResourceRecord {
        final PackType type;
        final Map<String, String> overlayVariants = new ConcurrentHashMap<>();

        ResourceRecord(PackType type, String basePath) {
            this.type = type;
            this.overlayVariants.put("", basePath);
        }

        void addOverlayVariant(String overlay, String path) {
            overlayVariants.put(overlay, path);
        }

        String getOverlayVariant(String overlay) {
            return overlayVariants.get(overlay);
        }
    }

    public interface OverlayLayout {
        boolean isOverlayPath(String path);
        List<String> getSearchOrder();
    }

    public static final class HierarchicalOverlayLayout implements OverlayLayout {
        private final List<String> overlays;

        public HierarchicalOverlayLayout(List<String> overlays) {
            this.overlays = new ArrayList<>(overlays);
            Collections.reverse(this.overlays);
        }

        @Override
        public boolean isOverlayPath(String path) {
            int firstSlash = path.indexOf('/');
            if (firstSlash == -1) return false;
            return !overlays.isEmpty() && overlays.contains(path.substring(0, firstSlash));
        }

        @Override
        public List<String> getSearchOrder() {
            List<String> order = new ArrayList<>(overlays);
            order.add("");
            return order;
        }
    }

    private volatile FileSystem zipFileSystem;
}