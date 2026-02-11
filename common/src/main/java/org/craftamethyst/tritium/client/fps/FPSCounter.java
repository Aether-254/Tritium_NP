package org.craftamethyst.tritium.client.fps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.craftamethyst.tritium.config.TritiumConfigBase;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class FPSCounter {
    private static final FPSCounter INSTANCE = new FPSCounter();
    private static final long UPDATE_INTERVAL_MS = 2000;
    private static final double ONE_PERCENT_THRESHOLD = 0.01;
    private static final int HISTORY_SIZE = 100;

    private static final int COLOR_VERY_LOW = 0xFF5555;
    private static final int COLOR_LOW = 0xFFAA00;
    private static final int COLOR_MEDIUM = 0xFFFF55;
    private static final int COLOR_HIGH = 0x55FF55;
    private static final int COLOR_LABEL = 0xFFFFFF;

    private final Deque<Double> fpsHistory = new ArrayDeque<>(HISTORY_SIZE);
    private final Deque<Double> recentFpsBuffer = new ArrayDeque<>();
    private final List<Double> fpsForOnePercentLow = new ArrayList<>();
    private double currentFPS = 0;
    private double avgFPS = 0;
    private boolean isFirstUpdate = true;
    private long lastUpdateTime = 0;
    private double dynamicMinFPS = Double.MAX_VALUE;
    private double dynamicMaxFPS = 0;
    private double intervalOnePercentLowFPS = Double.MAX_VALUE;
    private double intervalMaxFPS = 0;

    private FPSCounter() {
    }

    public static FPSCounter getInstance() {
        return INSTANCE;
    }

    public void update() {
        currentFPS = Minecraft.getInstance().getFps();
        updateDynamicStats();
        recentFpsBuffer.addLast(currentFPS);
        fpsForOnePercentLow.add(currentFPS);

        long currentTime = System.currentTimeMillis();

        if (isFirstUpdate) {
            lastUpdateTime = currentTime;
            isFirstUpdate = false;
        }

        if (currentTime - lastUpdateTime >= UPDATE_INTERVAL_MS) {
            updateIntervalStats();
            lastUpdateTime = currentTime;
        }

        fpsHistory.addLast(currentFPS);
        if (fpsHistory.size() > HISTORY_SIZE) {
            fpsHistory.removeFirst();
        }

        if (!fpsHistory.isEmpty()) {
            double sum = 0;
            for (double fps : fpsHistory) {
                sum += fps;
            }
            avgFPS = sum / fpsHistory.size();
        }
    }

    private void updateDynamicStats() {
        if (currentFPS < dynamicMinFPS) {
            dynamicMinFPS = currentFPS;
        }
        if (currentFPS > dynamicMaxFPS) {
            dynamicMaxFPS = currentFPS;
        }
    }

    private void updateIntervalStats() {
        if (fpsForOnePercentLow.isEmpty()) {
            return;
        }

        List<Double> sortedFps = new ArrayList<>(fpsForOnePercentLow);
        Collections.sort(sortedFps);

        int onePercentIndex = (int) Math.ceil(sortedFps.size() * ONE_PERCENT_THRESHOLD);
        if (onePercentIndex >= sortedFps.size()) {
            onePercentIndex = sortedFps.size() - 1;
        }

        intervalOnePercentLowFPS = sortedFps.get(onePercentIndex);

        double intervalMax = 0;
        for (double fps : recentFpsBuffer) {
            if (fps > intervalMax) {
                intervalMax = fps;
            }
        }
        intervalMaxFPS = intervalMax;
        recentFpsBuffer.clear();
        fpsForOnePercentLow.clear();
    }

    public void render(GuiGraphics guiGraphics, Font font, int screenWidth, int screenHeight) {
        if (!TritiumConfigBase.FPSDisplan.FPSDisplay.enabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;

        String[] parts = getFPSParts();

        int totalWidth = 0;
        for (String part : parts) {
            totalWidth += font.width(part);
        }

        int x = 0;
        int y = 0;

        switch (TritiumConfigBase.FPSDisplan.FPSDisplay.position) {
            case TOP_LEFT -> {
                x = 5;
                y = 5;
            }
            case TOP_RIGHT -> {
                x = screenWidth - totalWidth - 5;
                y = 5;
            }
            case BOTTOM_LEFT -> {
                x = 5;
                y = screenHeight - 15;
            }
            case BOTTOM_RIGHT -> {
                x = screenWidth - totalWidth - 5;
                y = screenHeight - 15;
            }
            case CENTER -> {
                x = (screenWidth - totalWidth) / 2;
                y = (screenHeight - 15) / 2;
            }
        }

        if (TritiumConfigBase.FPSDisplan.FPSDisplay.backgroundOpacity > 0) {
            int bgColor = (int) (TritiumConfigBase.FPSDisplan.FPSDisplay.backgroundOpacity * 255) << 24;
            guiGraphics.fill(x - 2, y - 2, x + totalWidth + 2, y + 12, bgColor);
        }

        int currentX = x;
        for (String part : parts) {
            int color = getPartColor(part);
            if ((color & 0xFF000000) == 0) {
                color = color | 0xFF000000;
            }
            guiGraphics.drawString(font, part, currentX, y, color, TritiumConfigBase.FPSDisplan.FPSDisplay.shadow);
            currentX += font.width(part);
        }
    }

    private int getColorForFPS(double fps) {
        int color;
        if (fps < 15) {
            color = COLOR_VERY_LOW;
        } else if (fps < 30) {
            color = COLOR_LOW;
        } else if (fps < 60) {
            color = COLOR_MEDIUM;
        } else {
            color = COLOR_HIGH;
        }
        return 0xFF000000 | color;
    }

    private String[] getFPSParts() {
        String unit = TritiumConfigBase.FPSDisplan.FPSDisplay.showUnit ? " FPS" : "";
        String minLabel = Component.translatable("config.tritium.fpsDisplay.label.min").getString();
        String avgLabel = Component.translatable("config.tritium.fpsDisplay.label.avg").getString();
        String maxLabel = Component.translatable("config.tritium.fpsDisplay.label.max").getString();

        switch (TritiumConfigBase.FPSDisplan.FPSDisplay.displayMode) {
            case AVG_ONLY -> {
                return new String[]{
                        avgLabel + " ",
                        String.format("%.0f", avgFPS),
                        unit
                };
            }
            case ALL -> {
                return new String[]{
                        String.format("%.0f", currentFPS),
                        "｜",
                        minLabel + " ",
                        String.format("%.0f", intervalOnePercentLowFPS),
                        "｜",
                        avgLabel + " ",
                        String.format("%.0f", avgFPS),
                        "｜",
                        maxLabel + " ",
                        String.format("%.0f", intervalMaxFPS),
                        unit
                };
            }
            case MAX_ONLY -> {
                return new String[]{
                        maxLabel + " ",
                        String.format("%.0f", intervalMaxFPS),
                        unit
                };
            }
            case MIN_ONLY -> {
                return new String[]{
                        minLabel + " ",
                        String.format("%.0f", intervalOnePercentLowFPS),
                        unit
                };
            }
            default -> {
                return new String[]{
                        String.format("%.0f", currentFPS),
                        unit
                };
            }
        }
    }

    private int getPartColor(String part) {
        if (part.contains("min") || part.contains("avg") || part.contains("max") || part.equals("｜") || part.equals(" FPS")) {
            return 0xFF000000 | COLOR_LABEL;
        }

        try {
            double fps = Double.parseDouble(part.trim());
            return getColorForFPS(fps);
        } catch (NumberFormatException e) {
            return 0xFF000000 | COLOR_LABEL;
        }
    }

    public void resetHistory() {
        fpsHistory.clear();
        recentFpsBuffer.clear();
        fpsForOnePercentLow.clear();
        avgFPS = 0;
        currentFPS = 0;
        dynamicMinFPS = Double.MAX_VALUE;
        dynamicMaxFPS = 0;
        intervalOnePercentLowFPS = Double.MAX_VALUE;
        intervalMaxFPS = 0;
        isFirstUpdate = true;
        lastUpdateTime = 0;
    }
}