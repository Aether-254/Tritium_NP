package org.craftamethyst.tritium.cull;

public class RenderCacheEntry {
    private final boolean shouldRender;
    private final long timestamp;
    private final long cacheDuration;

    public RenderCacheEntry(boolean shouldRender, long cacheDuration) {
        this.shouldRender = shouldRender;
        this.timestamp = System.currentTimeMillis();
        this.cacheDuration = cacheDuration;
    }

    public boolean isShouldRender() {
        return shouldRender;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > cacheDuration;
    }
}