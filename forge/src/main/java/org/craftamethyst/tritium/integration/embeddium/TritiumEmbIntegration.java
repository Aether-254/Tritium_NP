package org.craftamethyst.tritium.integration.embeddium;

import org.craftamethyst.tritium.TritiumCommon;
import org.embeddedt.embeddium.api.OptionGUIConstructionEvent;

public class TritiumEmbIntegration {

    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }

        OptionGUIConstructionEvent.BUS.addListener(TritiumEmbIntegration::onGuiConstruction);
        TritiumCommon.LOG.info("Embeddium integration initialized");
        initialized = true;
    }

    private static void onGuiConstruction(OptionGUIConstructionEvent event) {
        if (event.getPages().stream().anyMatch(page ->
                page.getId().getPath().equals(TritiumCommon.MOD_ID))) {
            return;
        }

        event.addPage(TritiumEmbPage.createPerformancePage());
        event.addPage(TritiumEmbPage.createRenderingPage());
        event.addPage(TritiumEmbPage.createClientOptimizationsPage());
        event.addPage(TritiumEmbPage.createEntitiesPage());
        event.addPage(TritiumEmbPage.createFixesPage());
        event.addPage(TritiumEmbPage.createServerPerformancePage());
        event.addPage(TritiumEmbPage.createTechOptimizationsPage());
        event.addPage(TritiumEmbPage.createNetworkPage());

        TritiumCommon.LOG.debug("Tritium Embeddium configuration pages registered");
    }
}