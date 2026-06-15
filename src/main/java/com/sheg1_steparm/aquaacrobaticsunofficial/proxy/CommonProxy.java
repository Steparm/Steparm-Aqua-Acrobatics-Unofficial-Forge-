package sheg1_steparm.aquaacrobaticsunofficial.proxy;

import sheg1_steparm.aquaacrobaticsunofficial.AquaAcrobatics;
import sheg1_steparm.aquaacrobaticsunofficial.biome.BiomeWaterFogColors;
import sheg1_steparm.aquaacrobaticsunofficial.block.BlockBubbleColumn;
import sheg1_steparm.aquaacrobaticsunofficial.config.ConfigHandler;
import sheg1_steparm.aquaacrobaticsunofficial.core.AquaAcrobaticsCore;
import sheg1_steparm.aquaacrobaticsunofficial.handler.CommonHandler;
import sheg1_steparm.aquaacrobaticsunofficial.network.NetworkHandler;
import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber
public class CommonProxy {
    @GameRegistry.ObjectHolder("aquaacrobaticsunofficial:bubble_column")
    public static BlockBubbleColumn BUBBLE_COLUMN;

    private boolean needNetworking() {
        return ConfigHandler.MovementConfig.enableToggleCrawling;
    }

    public void onPreInit(FMLPreInitializationEvent event) {
        if(needNetworking())
            NetworkHandler.registerMessages(AquaAcrobatics.MODID);
        MinecraftForge.EVENT_BUS.register(new CommonHandler());
    }

    public void onInit() {

    }

    public void onMappings() {

    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        if(ConfigHandler.MiscellaneousConfig.bubbleColumns)
            event.getRegistry().register(new BlockBubbleColumn());
    }


    public void onPostInit() {
        if(!AquaAcrobaticsCore.isModCompatLoaded)
            AquaAcrobatics.LOGGER.error("Please consider installing MixinBooter to ensure compatibility with more mods");

        BiomeWaterFogColors.recomputeColors();
    }
}