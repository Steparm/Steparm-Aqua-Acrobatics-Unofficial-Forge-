package sheg1_steparm.aquaacrobaticsunofficial.proxy;

import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import sheg1_steparm.aquaacrobaticsunofficial.AquaAcrobatics;
import sheg1_steparm.aquaacrobaticsunofficial.Tags;
import sheg1_steparm.aquaacrobaticsunofficial.biome.BiomeWaterFogColors;
import sheg1_steparm.aquaacrobaticsunofficial.block.BlockBubbleColumn;
import sheg1_steparm.aquaacrobaticsunofficial.config.ConfigHandler;
import sheg1_steparm.aquaacrobaticsunofficial.core.AquaAcrobaticsLateLoader;
import sheg1_steparm.aquaacrobaticsunofficial.handler.CommonHandler;
import sheg1_steparm.aquaacrobaticsunofficial.integration.IntegrationManager;
import sheg1_steparm.aquaacrobaticsunofficial.integration.hats.HatsIntegration;
import sheg1_steparm.aquaacrobaticsunofficial.integration.witchery.WitcheryResurrectedIntegration;
import sheg1_steparm.aquaacrobaticsunofficial.network.NetworkHandler;

@Mod.EventBusSubscriber
public class CommonProxy {
    @GameRegistry.ObjectHolder("aquaacrobaticsunofficial:bubble_column")
    public static BlockBubbleColumn BUBBLE_COLUMN;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        if (ConfigHandler.MISCELLANEOUS_CONFIG.bubbleColumns) {
            event.getRegistry().register(new BlockBubbleColumn());
        }
    }

    private boolean needNetworking() {
        return ConfigHandler.MOVEMENT_CONFIG.enableToggleCrawling;
    }

    public void preInit(FMLPreInitializationEvent event) {
        IntegrationManager.loadCompat();
        if (needNetworking()) {
            NetworkHandler.registerMessages(Tags.MOD_ID);
        }
        MinecraftForge.EVENT_BUS.register(new CommonHandler());
    }

    public void init() {
    }

    public void onMappings() {
    }

    public void postInit() {
        if (IntegrationManager.isHatsEnabled()) {
            HatsIntegration.register();
        }

        if (IntegrationManager.isWitcheryResurrectedEnabled()) {
            WitcheryResurrectedIntegration.register();
        }

        if (!AquaAcrobaticsLateLoader.isModCompatLoaded) {
            AquaAcrobatics.LOGGER.error("Please consider installing MixinBooter to ensure compatibility with more mods");
        }

        BiomeWaterFogColors.recomputeColors();
    }
}