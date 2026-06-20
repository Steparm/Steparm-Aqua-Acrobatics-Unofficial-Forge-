package sheg1_steparm.aquaacrobaticsunofficial.core.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import sheg1_steparm.aquaacrobaticsunofficial.config.ConfigHandler;
import sheg1_steparm.aquaacrobaticsunofficial.entity.player.IPlayerResizeable;
import sheg1_steparm.aquaacrobaticsunofficial.proxy.CommonProxy;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin extends Entity {
    public EntityLivingBaseMixin(World worldIn) {
        super(worldIn);
    }

    @Accessor(value = "isJumping")
    public abstract boolean aqua$isJumping();

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;isSneaking()Z"))
    public boolean isSneaking(EntityLivingBase entity) {
        // make sneaking on ladders work again since removing the pose client-side prevents the actual mechanic from working
        if (entity instanceof IPlayerResizeable) {
            return ((IPlayerResizeable) entity).aquaAcrobatics$isActuallySneaking();
        }

        return this.isSneaking();
    }

    @Unique
    private boolean aqua$isLosingAir() {
        if (ConfigHandler.MISCELLANEOUS_CONFIG.bubbleColumns && this.world.getBlockState(new BlockPos(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ)).getBlock() == CommonProxy.BUBBLE_COLUMN) {
            return false; // Pretend not to be in water
        }
        return this.isInsideOfMaterial(Material.WATER);
    }

    @WrapOperation(
            method = "onEntityUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityLivingBase;isInsideOfMaterial(Lnet/minecraft/block/material/Material;)Z"
            )
    )
    private boolean checkBubbleBreathing(EntityLivingBase instance, Material material, Operation<Boolean> original) {
        if (material == Material.WATER) {
            return aqua$isLosingAir();
        }

        return original.call(instance, material);
    }

    @ModifyArg(method = "onEntityUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;setAir(I)V"), index = 0)
    private int getNewAirValue(int original) {
        if (ConfigHandler.MISCELLANEOUS_CONFIG.slowAirReplenish && original == 300 && this.getAir() >= -20 && !aqua$isLosingAir()) {
            int oldAirValue = Math.max(this.getAir(), 0);
            return Math.min(oldAirValue + 4, 300);
        }
        return original;
    }

    @Redirect(method = "travel", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/EntityLivingBase;collidedHorizontally:Z", ordinal = 1))
    private boolean isJumpingOnLadder(EntityLivingBase instance) {
        if (ConfigHandler.MOVEMENT_CONFIG.newClimbingBehavior) {
            return instance.collidedHorizontally || ((EntityLivingBaseMixin) (Object) instance).aqua$isJumping();
        } else {
            return instance.collidedHorizontally;
        }
    }
}