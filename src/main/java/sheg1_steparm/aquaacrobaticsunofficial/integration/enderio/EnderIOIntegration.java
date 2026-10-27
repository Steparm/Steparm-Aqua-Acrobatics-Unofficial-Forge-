package sheg1_steparm.aquaacrobaticsunofficial.integration.enderio;

import crazypants.enderio.base.handler.darksteel.StateController;
import crazypants.enderio.base.item.darksteel.upgrade.elytra.ElytraUpgrade;
import sheg1_steparm.aquaacrobaticsunofficial.integration.IntegrationManager;

public class EnderIOIntegration {
    public static void register() {
        IntegrationManager.elytraOpenHooks.add(player -> {
            if (!StateController.isActive(player, ElytraUpgrade.INSTANCE)) {
                StateController.setActive(player, ElytraUpgrade.INSTANCE, true);
            }
        });
    }
}