package sheg1_steparm.aquaacrobaticsunofficial.proxy;

import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import sheg1_steparm.aquaacrobaticsunofficial.block.BlockBubbleColumn;
import sheg1_steparm.aquaacrobaticsunofficial.client.handler.AirMeterHandler;
import sheg1_steparm.aquaacrobaticsunofficial.client.handler.FogHandler;
import sheg1_steparm.aquaacrobaticsunofficial.client.model.WaterResourcePack;
import sheg1_steparm.aquaacrobaticsunofficial.config.ConfigHandler;
import sheg1_steparm.aquaacrobaticsunofficial.entity.player.IPlayerResizeable;
import sheg1_steparm.aquaacrobaticsunofficial.integration.IntegrationManager;
import sheg1_steparm.aquaacrobaticsunofficial.integration.artemislib.ArtemisLibIntegration;
import sheg1_steparm.aquaacrobaticsunofficial.integration.enderio.EnderIOIntegration;
import sheg1_steparm.aquaacrobaticsunofficial.integration.mobends.MoBendsIntegration;
import sheg1_steparm.aquaacrobaticsunofficial.integration.thaumicaugmentation.ThaumicAugmentationIntegration;
import sheg1_steparm.aquaacrobaticsunofficial.network.NetworkHandler;
import sheg1_steparm.aquaacrobaticsunofficial.network.message.PacketSendKey;
import sheg1_steparm.aquaacrobaticsunofficial.optifine.OptifineHelper;
import sheg1_steparm.aquaacrobaticsunofficial.util.Keybindings;

import java.util.List;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        if (ConfigHandler.MISCELLANEOUS_CONFIG.bubbleColumns) {
            ModelLoader.setCustomStateMapper(CommonProxy.BUBBLE_COLUMN, new StateMap.Builder().ignore(BlockLiquid.LEVEL, BlockBubbleColumn.DRAG).build());
        }
    }

    @SubscribeEvent
    public static void registerTextures(TextureStitchEvent.Pre event) {
        if (ConfigHandler.BLOCKS_CONFIG.newWaterColors) {
            TextureMap map = event.getMap();
            /* Register the custom 1.13-style texture used by most in-world renderers */
            map.registerSprite(new ResourceLocation("aquaacrobaticsunofficial:blocks/water_still"));
            map.registerSprite(new ResourceLocation("aquaacrobaticsunofficial:blocks/water_flow"));
        }
    }

    @SubscribeEvent
    public static void onKeyPress(InputEvent.KeyInputEvent event) {
        // null check for hot reload
        if (ConfigHandler.MOVEMENT_CONFIG.enableToggleCrawling && Keybindings.forceCrawling != null && Keybindings.forceCrawling.isPressed()) {
            IPlayerResizeable player = (IPlayerResizeable) Minecraft.getMinecraft().player;
            if (player != null) {
                if (player.aquaAcrobatics$canForceCrawling())
                    NetworkHandler.INSTANCE.sendToServer(new PacketSendKey(PacketSendKey.KeybindPacket.TOGGLE_CRAWLING));
                else {
                    ((EntityPlayerSP) player).sendMessage(new TextComponentTranslation("chat.aquaacrobatics.cannot_toggle_crawling"));
                }
            }
        }
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(new AirMeterHandler());
        MinecraftForge.EVENT_BUS.register(new FogHandler());

        if (ConfigHandler.BLOCKS_CONFIG.newWaterColors) {
            List<IResourcePack> packs = ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "field_110449_ao");
            packs.add(new WaterResourcePack(event.getSourceFile()));
            FMLClientHandler.instance().refreshResources(VanillaResourceType.TEXTURES);
            OptifineHelper.init();
        }
    }

    @Override
    public void init() {
        Keybindings.register();
    }

    @Override
    public void onMappings() {
        if (OptifineHelper.isOFPresent) {
            OptifineHelper.reloadBlockAliases();
        }
    }

    @Override
    public void postInit() {
        super.postInit();
        FogHandler.recomputeBlacklist();

        if (IntegrationManager.isMoBendsEnabled()) {
            MoBendsIntegration.register();
        }

        if (IntegrationManager.isArtemisLibEnabled()) {
            ArtemisLibIntegration.register();
        }

        if (IntegrationManager.isEnderIoEnabled()) {
            EnderIOIntegration.register();
        }

        if (IntegrationManager.isThaumicAugmentationEnabled()) {
            ThaumicAugmentationIntegration.register();
        }
    }
}