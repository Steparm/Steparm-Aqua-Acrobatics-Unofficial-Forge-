package sheg1_steparm.aquaacrobaticsunofficial.core.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sheg1_steparm.aquaacrobaticsunofficial.config.ConfigHandler;

@Mixin(EntityThrowable.class)
public abstract class EntityThrowableMixin extends Entity {
    @Unique
    private final boolean aqua$isNewProjectile = aqua$checkEntityEligibleForProjectile();

    public EntityThrowableMixin(World worldIn) {
        super(worldIn);
    }

    @Unique
    private boolean aqua$checkEntityEligibleForProjectile() {
        return ConfigHandler.MOVEMENT_CONFIG.newProjectileBehavior && getClass().getName().startsWith("net.minecraft.");
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;rayTraceBlocks(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/RayTraceResult;"))
    private RayTraceResult rayTraceThroughLiquid(World world, Vec3d start, Vec3d end) {
        if (aqua$isNewProjectile) {
            return world.rayTraceBlocks(start, end, false, true, false);
        } else {
            return world.rayTraceBlocks(start, end);
        }
    }

    @Inject(method = "onUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/projectile/EntityThrowable;posX:D", opcode = Opcodes.PUTFIELD, ordinal = 0))
    private void doCheckBlockCollision(CallbackInfo ci) {
        if (aqua$isNewProjectile) {
            this.doBlockCollisions();
        }
    }
}