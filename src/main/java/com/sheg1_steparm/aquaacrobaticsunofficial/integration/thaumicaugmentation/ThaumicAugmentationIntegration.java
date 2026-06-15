package sheg1_steparm.aquaacrobaticsunofficial.integration.thaumicaugmentation;

import sheg1_steparm.aquaacrobaticsunofficial.integration.IntegrationManager;
import thecodex6824.thaumicaugmentation.client.internal.TAHooksClient;

public class ThaumicAugmentationIntegration {
    public static void register() {
        IntegrationManager.elytraOpenHooks.add(TAHooksClient::checkElytra);
    }
}