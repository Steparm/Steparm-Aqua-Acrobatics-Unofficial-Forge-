package sheg1_steparm.aquaacrobaticsunofficial.core.mixin.client;

import sheg1_steparm.aquaacrobaticsunofficial.util.math.MathHelperNew;
import sheg1_steparm.aquaacrobaticsunofficial.entity.player.IPlayerResizeable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Shadow
    @Final
    private Minecraft mc;

    private float eyeHeight;
    private float previousEyeHeight;
    private float entityEyeHeight;
    private float partialTicks;

    @Inject(method = "orientCamera", at = @At("HEAD"))
    private void orientCamera(float partialTicks, CallbackInfo callbackInfo) {
        this.partialTicks = partialTicks;
    }

    @ModifyVariable(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevPosX:D", ordinal = 0), ordinal = 1)
    public float getEyeHeight(float eyeHeight) {
        Entity entity = this.mc.getRenderViewEntity();

        if (!(entity instanceof IPlayerResizeable)) {
            return eyeHeight;
        }

        this.entityEyeHeight = ((IPlayerResizeable) entity).getPlayerEyeHeight();
        return MathHelperNew.lerp(this.partialTicks, this.previousEyeHeight, this.eyeHeight);
    }

    @Inject(method = "updateRenderer", at = @At("TAIL"))
    public void updateRenderer(CallbackInfo callbackInfo) {
        this.interpolateHeight();
    }

    private void interpolateHeight() {
        this.previousEyeHeight = this.eyeHeight;
        this.eyeHeight += (this.entityEyeHeight - this.eyeHeight) * 0.5F;
    }

    @Redirect(
            method = "renderWorldPass",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;isInsideOfMaterial(Lnet/minecraft/block/material/Material;)Z",
                    ordinal = 0
            ),
            require = 0,
            expect = 0
    )
    private boolean ignoreWater(Entity entity, Material material) {
        if(material == Material.WATER)
            return false;
        return entity.isInsideOfMaterial(material);
    }
}