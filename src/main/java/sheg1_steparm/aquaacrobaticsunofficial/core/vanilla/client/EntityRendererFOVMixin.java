package sheg1_steparm.aquaacrobaticsunofficial.core.vanilla.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

@Mixin(EntityRenderer.class)
public class EntityRendererFOVMixin {

    @Shadow
    private float fovModifierHand;
    @Shadow
    private float fovModifierHandPrev;

    private static Boolean HAS_FLUIDLOGGED_API = null;
    private static Method isFluidloggedMethod = null;

    private static void initFluidloggedAPI() {
        if (HAS_FLUIDLOGGED_API != null) return;
        
        boolean hasApi = false;
        try {
            hasApi = Loader.isModLoaded("fluidlogged_api");
        } catch (Exception e) {
            hasApi = false;
        }
        HAS_FLUIDLOGGED_API = hasApi;

        if (HAS_FLUIDLOGGED_API) {
            try {
                Class<?> fluidloggedAPIClass = null;
                try {
                    fluidloggedAPIClass = Class.forName("git.jbredwards.fluidlogged_api.api.API");
                } catch (ClassNotFoundException e1) {
                    try {
                        fluidloggedAPIClass = Class.forName("git.jbredwards.fluidlogged_api.FluidloggedAPI");
                    } catch (ClassNotFoundException e2) {
                        try {
                            fluidloggedAPIClass = Class.forName("net.minecraftforge.fluids.FluidloggedAPI");
                        } catch (ClassNotFoundException e3) {
                        }
                    }
                }
                if (fluidloggedAPIClass != null) {
                    isFluidloggedMethod = fluidloggedAPIClass.getMethod("isFluidlogged", World.class, BlockPos.class);
                }
            } catch (Exception e) {
            }
        }
    }

    private static boolean isPlayerInWaterOrFluidlogged(EntityPlayer player) {
        if (player == null) return false;

        if (player.isInsideOfMaterial(net.minecraft.block.material.Material.WATER)) {
            return true;
        }

        initFluidloggedAPI();
        if (HAS_FLUIDLOGGED_API != null && HAS_FLUIDLOGGED_API && isFluidloggedMethod != null) {
            BlockPos pos = new BlockPos(player.posX, player.posY + player.getEyeHeight(), player.posZ);
            World world = player.getEntityWorld();
            if (world != null) {
                try {
                    boolean result = (boolean) isFluidloggedMethod.invoke(null, world, pos);
                    if (result) {
                        return true;
                    }
                } catch (Exception e) {
                }
            }
        }

        return false;
    }

    private float getWaterFOV(EntityPlayer player, float partialTicks) {
        float waterFov = 60.0F;

        if (player.isPotionActive(MobEffects.SPEED)) {
            waterFov *= 60.0F + (player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() + 1) * 0.05F;
        }
        if (player.isPotionActive(MobEffects.SLOWNESS)) {
            waterFov *= 60.0F - (player.getActivePotionEffect(MobEffects.SLOWNESS).getAmplifier() + 1) * 0.025F;
        }

        float f = this.fovModifierHandPrev + (this.fovModifierHand - this.fovModifierHandPrev) * partialTicks;

        return MathHelper.clamp(waterFov, 0.1F, 60.0F) * f;
    }

    @Inject(method = "getFOVModifier", at = @At("RETURN"), cancellable = true)
    private void onGetFOVModifier(float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Float> cir) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;

        if (player != null && isPlayerInWaterOrFluidlogged(player)) {
            if (cir.getReturnValue() < 50.0f) {
                return; 
            }
            
            float waterFov = getWaterFOV(player, partialTicks);
            cir.setReturnValue(waterFov);
        }
    }
}