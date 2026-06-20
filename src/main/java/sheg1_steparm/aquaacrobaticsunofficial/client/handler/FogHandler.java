package sheg1_steparm.aquaacrobaticsunofficial.client.handler;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sheg1_steparm.aquaacrobaticsunofficial.biome.BiomeWaterFogColors;
import sheg1_steparm.aquaacrobaticsunofficial.config.ConfigHandler;
import sheg1_steparm.aquaacrobaticsunofficial.entity.player.IPlayerResizeable;
import sheg1_steparm.aquaacrobaticsunofficial.proxy.CommonProxy;
import sheg1_steparm.aquaacrobaticsunofficial.util.math.MathHelperNew;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Uses Forge events to adjust water rendering so it more closely approximates 1.13+.
 * <p>
 * Some of the code in this class is based off of Minecraft 1.16.
 */
public class FogHandler {
    // Based on Minecraft 1.21.7
    static final float FOG_END = 96.0F;
    static final float FOG_START = -8.0F;
    private static HashSet<String> worldProviderClassNames = null;
    private int targetFogColor = -1;
    private int prevFogColor = -1;
    private long fogAdjustTime = -1L;

    public static void recomputeBlacklist() {
        worldProviderClassNames = new HashSet<>();
        worldProviderClassNames.addAll(Arrays.asList(ConfigHandler.MISCELLANEOUS_CONFIG.providerFogBlacklist));
    }

    private boolean shouldSkipFogOverride(World world) {
        if (!ConfigHandler.BLOCKS_CONFIG.newWaterFog) {
            return false;
        }
        return !worldProviderClassNames.contains(world.provider.getClass().getName());
    }

    @SubscribeEvent
    public void registerBlockColors(ColorHandlerEvent.Block event) {
        if (ConfigHandler.MISCELLANEOUS_CONFIG.bubbleColumns) {
            event.getBlockColors().registerBlockColorHandler((state, worldIn, pos, tintIndex) -> worldIn != null && pos != null ? BiomeColorHelper.getWaterColorAtPos(worldIn, pos) : -1, CommonProxy.BUBBLE_COLUMN);
        }
    }

    @SubscribeEvent
    public void onRenderFogDensity(EntityViewRenderEvent.FogDensity event) {
        switch (ConfigHandler.BLOCKS_CONFIG.waterFogMode) {
            case AA_EXP2:
                handleExp2Fog(event);
                break;
            case VANILLA_LINEAR:
                handleLinearFog(event);
                break;
        }
    }

    private void handleExp2Fog(EntityViewRenderEvent.FogDensity event) {
        Entity eventEntity = event.getEntity();
        if (eventEntity instanceof EntityLivingBase && ((EntityLivingBase) eventEntity).isPotionActive(MobEffects.BLINDNESS)) {
            return;
        }
        if (event.getState().getMaterial() == Material.WATER && shouldSkipFogOverride(eventEntity.getEntityWorld())) {
            GlStateManager.setFog(GlStateManager.FogMode.EXP2);
            float density = 0.05f;
            if (eventEntity instanceof EntityPlayer) {
                EntityPlayer playerEntity = (EntityPlayer) eventEntity;
                float waterVision = ((IPlayerResizeable) playerEntity).aquaAcrobatics$getWaterVision();
                density -= waterVision * waterVision * 0.03F;
                Biome biome = playerEntity.world.getBiome(playerEntity.getPosition());
                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP)) {
                    density += 0.005F;
                }
            }
            event.setDensity(density);
            event.setCanceled(true);
        }
    }

    private void handleLinearFog(EntityViewRenderEvent.FogDensity event) {
        Entity eventEntity = event.getEntity();
        if (eventEntity instanceof EntityLivingBase && ((EntityLivingBase) eventEntity).isPotionActive(MobEffects.BLINDNESS)) {
            return;
        }

        if (event.getState().getMaterial() == Material.WATER && shouldSkipFogOverride(eventEntity.getEntityWorld())) {
            GlStateManager.setFog(GlStateManager.FogMode.LINEAR);

            float renderDistanceInBlocks = Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16.0f;
            float fogEnd = Math.min(renderDistanceInBlocks, FOG_END);

            if (eventEntity instanceof EntityPlayer) {
                EntityPlayer playerEntity = (EntityPlayer) eventEntity;
                float waterVision = ((IPlayerResizeable) playerEntity).aquaAcrobatics$getWaterVision();
                fogEnd *= Math.max(0.25F, waterVision);
                Biome biome = playerEntity.world.getBiome(playerEntity.getPosition());
                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP)) {
                    fogEnd *= 0.85F;
                }
            }

            GlStateManager.setFogStart(FOG_START);
            GlStateManager.setFogEnd(fogEnd);
            event.setCanceled(true);
        }
    }


    /* LOW to override mods like Biomes O' Plenty which force their own underwater fog color */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderFogColor(EntityViewRenderEvent.FogColors event) {
        if (!ConfigHandler.BLOCKS_CONFIG.newWaterColors) {
            return;
        }
        Block blockInside = event.getState().getBlock();
        if ((event.getState().getMaterial() == Material.WATER) && event.getEntity() instanceof EntityPlayer && shouldSkipFogOverride(event.getEntity().getEntityWorld())) {
            float fogRed, fogGreen, fogBlue;
            EntityPlayer playerEntity = (EntityPlayer) event.getEntity();
            long i = System.nanoTime() / 1000000L;
            int j = BiomeWaterFogColors.getWaterFogColorForBiome(playerEntity.world.getBiome(playerEntity.getPosition()));
            if (fogAdjustTime < 0L) {
                targetFogColor = j;
                prevFogColor = j;
                fogAdjustTime = i;
            }
            int k = targetFogColor >> 16 & 255;
            int l = targetFogColor >> 8 & 255;
            int i1 = targetFogColor & 255;
            int j1 = prevFogColor >> 16 & 255;
            int k1 = prevFogColor >> 8 & 255;
            int l1 = prevFogColor & 255;
            float f = MathHelper.clamp((float) (i - fogAdjustTime) / 5000.0F, 0.0F, 1.0F);
            float f1 = MathHelperNew.lerp(f, (float) j1, (float) k);
            float f2 = MathHelperNew.lerp(f, (float) k1, (float) l);
            float f3 = MathHelperNew.lerp(f, (float) l1, (float) i1);
            fogRed = f1 / 255.0F;
            fogGreen = f2 / 255.0F;
            fogBlue = f3 / 255.0F;
            if (targetFogColor != j) {
                targetFogColor = j;
                prevFogColor = MathHelper.floor(f1) << 16 | MathHelper.floor(f2) << 8 | MathHelper.floor(f3);
                fogAdjustTime = i;
            }
            float f6 = ((IPlayerResizeable) playerEntity).aquaAcrobatics$getWaterVision();
            float f9 = Math.min(1.0F / fogRed, Math.min(1.0F / fogGreen, 1.0F / fogBlue));
            fogRed = fogRed * (1.0F - f6) + fogRed * f9 * f6;
            fogGreen = fogGreen * (1.0F - f6) + fogGreen * f9 * f6;
            fogBlue = fogBlue * (1.0F - f6) + fogBlue * f9 * f6;

            double blindnessFactor = 1.0;
            if (playerEntity.isPotionActive(MobEffects.BLINDNESS)) {
                int potionDuration = playerEntity.getActivePotionEffect(MobEffects.BLINDNESS).getDuration();
                if (potionDuration < 20) {
                    blindnessFactor *= (1.0F - (float) potionDuration / 20.0F);
                } else {
                    blindnessFactor = 0.0D;
                }
            }

            if (blindnessFactor < 1.0D) {
                if (blindnessFactor < 0.0D) {
                    blindnessFactor = 0.0D;
                }

                blindnessFactor = blindnessFactor * blindnessFactor;
                fogRed = (float) ((double) fogRed * blindnessFactor);
                fogGreen = (float) ((double) fogGreen * blindnessFactor);
                fogBlue = (float) ((double) fogBlue * blindnessFactor);
            }

            event.setRed(fogRed);
            event.setGreen(fogGreen);
            event.setBlue(fogBlue);
        } else if ((blockInside == Blocks.LAVA || blockInside == Blocks.FLOWING_LAVA)) {
            event.setRed(0.6f);
            event.setGreen(0.1f);
            event.setBlue(0.0f);
            fogAdjustTime = -1L;
        } else {
            fogAdjustTime = -1L;
        }
    }
}