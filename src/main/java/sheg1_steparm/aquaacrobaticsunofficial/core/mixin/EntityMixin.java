package sheg1_steparm.aquaacrobaticsunofficial.core.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockVine;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import sheg1_steparm.aquaacrobaticsunofficial.config.ConfigHandler;
import sheg1_steparm.aquaacrobaticsunofficial.entity.IBubbleColumnInteractable;
import sheg1_steparm.aquaacrobaticsunofficial.entity.player.IPlayerResizeable;

@Mixin(Entity.class)
public abstract class EntityMixin implements IBubbleColumnInteractable {
    @Shadow
    public double motionY;
    @Shadow
    public float fallDistance;
    @Shadow
    public World world;

    @Shadow
    public abstract boolean isSneaking();

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z"))
    public boolean isSneaking(Entity entity) {
        // patches two calls to allow falling off blocks when not pressing sneak key but being in crouching pose
        if (entity instanceof IPlayerResizeable) {
            return ((IPlayerResizeable) entity).aquaAcrobatics$isActuallySneaking();
        }

        return this.isSneaking();
    }

    @Override
    public void aquaAcrobatics$onEnterBubbleColumn(boolean downwards) {
        if (!downwards) {
            this.motionY = Math.min(0.7, this.motionY + 0.06);
        } else {
            this.motionY = Math.max(-0.3, this.motionY - 0.03);
        }
        this.fallDistance = 0.0F;
    }

    @Override
    public void aquaAcrobatics$onEnterBubbleColumnWithAirAbove(boolean downwards) {
        if (!downwards) {
            this.motionY = Math.min(1.8, this.motionY + 0.1);
        } else {
            this.motionY = Math.max(-0.9, this.motionY - 0.03);
        }
    }

    @ModifyVariable(method = "move", ordinal = 0, name = "block", at = @At("LOAD"))
    private Block getFakeClimbingBlock(Block original) {
        if (ConfigHandler.MOVEMENT_CONFIG.newClimbingBehavior && original instanceof BlockVine) {
            return Blocks.LADDER;
        }
        return original;
    }
}