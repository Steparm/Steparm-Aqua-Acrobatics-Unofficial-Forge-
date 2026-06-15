package sheg1_steparm.aquaacrobaticsunofficial.proxy;

import sheg1_steparm.aquaacrobaticsunofficial.block.BlockBubbleColumn;
import sheg1_steparm.aquaacrobaticsunofficial.client.handler.AirMeterHandler;
import sheg1_steparm.aquaacrobaticsunofficial.client.handler.FogHandler;
import sheg1_steparm.aquaacrobaticsunofficial.client.model.WaterResourcePack;
import sheg1_steparm.aquaacrobaticsunofficial.config.ConfigHandler;
import sheg1_steparm.aquaacrobaticsunofficial.entity.player.IPlayerResizeable;
import sheg1_steparm.aquaacrobaticsunofficial.network.NetworkHandler;
import sheg1_steparm.aquaacrobaticsunofficial.network.message.PacketSendKey;
import sheg1_steparm.aquaacrobaticsunofficial.optifine.OptifineHelper;
import sheg1_steparm.aquaacrobaticsunofficial.util.Keybindings;
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

import java.util.List;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void onPreInit(FMLPreInitializationEvent event) {

        super.onPreInit(event);
        MinecraftForge.EVENT_BUS.register(new AirMeterHandler());
        MinecraftForge.EVENT_BUS.register(new FogHandler());

        if(ConfigHandler.BlocksConfig.newWaterColors) {
            List<IResourcePack> packs = ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "field_110449_ao");
            packs.add(new WaterResourcePack(event.getSourceFile()));
            FMLClientHandler.instance().refreshResources(VanillaResourceType.TEXTURES);
            OptifineHelper.init();
        }
    }


    @Override
    public void onInit() {
        Keybindings.register();
    }

    @Override
    public void onMappings() {
        if (OptifineHelper.isOFPresent) {
            OptifineHelper.reloadBlockAliases();
        }
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        if(ConfigHandler.MiscellaneousConfig.bubbleColumns)
            ModelLoader.setCustomStateMapper(CommonProxy.BUBBLE_COLUMN, new StateMap.Builder().ignore(BlockLiquid.LEVEL, BlockBubbleColumn.DRAG).build());
    }
    
    @SubscribeEvent
    public static void registerTextures(TextureStitchEvent.Pre event) {
        if(ConfigHandler.BlocksConfig.newWaterColors) {
            TextureMap map = event.getMap();
            map.registerSprite(new ResourceLocation("aquaacrobaticsunofficial:blocks/water_still"));
            map.registerSprite(new ResourceLocation("aquaacrobaticsunofficial:blocks/water_flow"));
        }
    }

    @SubscribeEvent
    public static void onKeyPress(InputEvent.KeyInputEvent event) {
        if(ConfigHandler.MovementConfig.enableToggleCrawling && Keybindings.forceCrawling.isPressed()) {
            IPlayerResizeable player = (IPlayerResizeable) Minecraft.getMinecraft().player;
            if(player != null) {
                if(player.canForceCrawling())
                    NetworkHandler.INSTANCE.sendToServer(new PacketSendKey(PacketSendKey.KeybindPacket.TOGGLE_CRAWLING));
                else {
                    ((EntityPlayerSP)player).sendMessage(new TextComponentTranslation("chat.aquaacrobatics.cannot_toggle_crawling"));
                }
            }
        }
    }

    @Override
    public void onPostInit() {
        super.onPostInit();
        FogHandler.recomputeBlacklist();
    }
}