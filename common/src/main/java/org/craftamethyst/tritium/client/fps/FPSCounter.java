package org.craftamethyst.tritium.client.fps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.craftamethyst.tritium.TritiumCommon;
import org.craftamethyst.tritium.config.TritiumConfigBase;

import java.util.*;

public class FPSCounter {
    private static final FPSCounter INSTANCE = new FPSCounter();
    private static final long UPDATE_INTERVAL_MS = 2000;
    private static final double ONE_PERCENT_THRESHOLD = 0.01;

    private final Deque<Double> fpsHistory = new ArrayDeque<>();
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
        int historySize = 100;
        while (fpsHistory.size() > historySize) {
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

        String text = getFPSString();
        int textWidth = font.width(text);

        int x = 0, y = 0;

        y = switch (TritiumConfigBase.FPSDisplan.FPSDisplay.position) {
            case TOP_LEFT -> {
                x = 5;
                yield 5;
            }
            case TOP_RIGHT -> {
                x = screenWidth - textWidth - 5;
                yield 5;
            }
            case BOTTOM_LEFT -> {
                x = 5;
                yield screenHeight - 15;
            }
            case BOTTOM_RIGHT -> {
                x = screenWidth - textWidth - 5;
                yield screenHeight - 15;
            }
            case CENTER -> {
                x = (screenWidth - textWidth) / 2;
                yield (screenHeight - 15) / 2;
            }
            default -> y;
        };

        if (TritiumConfigBase.FPSDisplan.FPSDisplay.backgroundOpacity > 0) {
            int bgColor = (int) (TritiumConfigBase.FPSDisplan.FPSDisplay.backgroundOpacity * 255) << 24;
            guiGraphics.fill(x - 2, y - 2, x + textWidth + 2, y + 12, bgColor);
        }

        int color = parseColor(TritiumConfigBase.FPSDisplan.FPSDisplay.textColor);
        guiGraphics.drawString(font, text, x, y, color, TritiumConfigBase.FPSDisplan.FPSDisplay.shadow);
    }

    private String getFPSString() {
        String format = "%.0f";
        int decimals = TritiumConfigBase.FPSDisplan.FPSDisplay.decimalPlaces.value();
        if (decimals > 0) {
            format = "%." + decimals + "f";
        }

        String unit = TritiumConfigBase.FPSDisplan.FPSDisplay.showUnit ? " FPS" : "";

        String minLabel = Component.translatable("config.tritium.fpsDisplay.label.min").getString();
        String avgLabel = Component.translatable("config.tritium.fpsDisplay.label.avg").getString();
        String maxLabel = Component.translatable("config.tritium.fpsDisplay.label.max").getString();

        return switch (TritiumConfigBase.FPSDisplan.FPSDisplay.displayMode) {
            case AVG_ONLY -> avgLabel + String.format(" " + format + unit, avgFPS);
            case ALL ->
                    String.format(format + "｜" + minLabel + " " + format + "｜" + avgLabel + " " + format + "｜" + maxLabel + " " + format + unit,
                            currentFPS, intervalOnePercentLowFPS, avgFPS, intervalMaxFPS);
            case MAX_ONLY -> maxLabel + String.format(" " + format + unit, intervalMaxFPS);
            case MIN_ONLY -> minLabel + String.format(" " + format + unit, intervalOnePercentLowFPS);
            default -> String.format(format + unit, currentFPS);
        };
    }

    private int parseColor(String hexColor) {
        try {
            if (hexColor.startsWith("#")) {
                return Integer.parseInt(hexColor.substring(1), 16);
            }
        } catch (NumberFormatException e) {
            TritiumCommon.LOG.warn("Invalid color format: {}", hexColor);
        }
        return 0xFFFFFF;
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
