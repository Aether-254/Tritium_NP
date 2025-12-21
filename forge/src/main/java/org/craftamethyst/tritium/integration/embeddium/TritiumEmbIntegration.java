package org.craftamethyst.tritium.integration.embeddium;

import org.craftamethyst.tritium.TritiumCommon;
import org.embeddedt.embeddium.api.OptionGUIConstructionEvent;

public class TritiumEmbIntegration {


    public static void init() {
            OptionGUIConstructionEvent.BUS.addListener(TritiumEmbIntegration::onGuiConstruction);
            TritiumCommon.LOG.info("Embeddium integration initialized");
    }

    private static void onGuiConstruction(OptionGUIConstructionEvent event) {
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