package sheg1_steparm.aquaacrobaticsunofficial.core.mixin.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sheg1_steparm.aquaacrobaticsunofficial.integration.IntegrationManager;
import sheg1_steparm.aquaacrobaticsunofficial.util.math.MathHelperNew;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    private float thirdPersonDistancePrev;

    @Unique
    private float aquaAcrobatics$eyeHeight;
    @Unique
    private float aquaAcrobatics$previousEyeHeight;
    @Unique
    private float aquaAcrobatics$entityEyeHeight;
    @Unique
    private float aquaAcrobatics$partialTicks;

    @Inject(method = "orientCamera", at = @At("HEAD"))
    private void orientCamera(float partialTicks, CallbackInfo callbackInfo) {
        // field for passing on partialTicks, workaround as @ModifyVariable is unable to handle method arguments in Mixin <0.8
        this.aquaAcrobatics$partialTicks = partialTicks;
    }

    @ModifyVariable(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevPosX:D", ordinal = 0, opcode = Opcodes.GETFIELD), ordinal = 1)
    public float getEyeHeight(float eyeHeight) {
        Entity entity = this.mc.getRenderViewEntity();

        // Do not apply eye height patch if the camera is not a player, or if Random Patches is installed
        if (!(entity instanceof EntityPlayer) || IntegrationManager.isRandomPatchesEnabled()) {
            return eyeHeight;
        }

        // need to do it like this to prevent crash with wings mod
        this.aquaAcrobatics$entityEyeHeight = eyeHeight;
        return MathHelperNew.lerp(this.aquaAcrobatics$partialTicks, this.aquaAcrobatics$previousEyeHeight, this.aquaAcrobatics$eyeHeight);
    }

    @Inject(method = "updateRenderer", at = @At("TAIL"))
    public void updateRenderer(CallbackInfo callbackInfo) {
        this.aquaAcrobatics$interpolateHeight();
    }

    @Unique
    private void aquaAcrobatics$interpolateHeight() {
        this.aquaAcrobatics$previousEyeHeight = this.aquaAcrobatics$eyeHeight;
        this.aquaAcrobatics$eyeHeight += (this.aquaAcrobatics$entityEyeHeight - this.aquaAcrobatics$eyeHeight) * 0.5F;
    }

    // Backport start - Camera logic from modern versions
    @Redirect(
            method = {"updateFogColor", "setupFog", "getFOVModifier"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;getBlockStateAtEntityViewpoint(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;F)Lnet/minecraft/block/state/IBlockState;"
            )
    )
    private IBlockState getBlockStateAtCameraForFog(World world, Entity entity, float partialTicks) {
        IBlockState cameraWaterState = this.aquaAcrobatics$getCameraWaterState(world, entity, partialTicks);
        if (cameraWaterState != null) {
            return cameraWaterState;
        }

        IBlockState state = ActiveRenderInfo.getBlockStateAtEntityViewpoint(world, entity, partialTicks);
        if (state.getMaterial() == Material.WATER) {
            return Blocks.AIR.getDefaultState();
        }
        return state;
    }

    @Redirect(
            method = "updateFogColor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;getFogColor(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;F)Lnet/minecraft/util/math/Vec3d;"
            )
    )
    private Vec3d getFogColor(Block block, World world, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor, float partialTicks) {
        Vec3d cameraPos = this.aquaAcrobatics$getCameraPosition(entity, partialTicks);
        BlockPos cameraBlockPos = new BlockPos(cameraPos);
        IBlockState cameraState = world.getBlockState(cameraBlockPos);
        if (this.aquaAcrobatics$isWaterAtCamera(world, cameraBlockPos, cameraState, cameraPos)) {
            return cameraState.getBlock().getFogColor(world, cameraBlockPos, cameraState, entity, originalColor, partialTicks);
        }
        if (state.getMaterial() == Material.WATER) {
            return originalColor;
        }
        return block.getFogColor(world, pos, state, entity, originalColor, partialTicks);
    }

    @Unique
    private IBlockState aquaAcrobatics$getCameraWaterState(World world, Entity entity, float partialTicks) {
        Vec3d cameraPos = this.aquaAcrobatics$getCameraPosition(entity, partialTicks);
        BlockPos blockPos = new BlockPos(cameraPos);
        IBlockState state = world.getBlockState(blockPos);
        return this.aquaAcrobatics$isWaterAtCamera(world, blockPos, state, cameraPos) ? state : null;
    }

    @Unique
    private boolean aquaAcrobatics$isWaterAtCamera(World world, BlockPos blockPos, IBlockState state, Vec3d cameraPos) {
        if (state.getMaterial() != Material.WATER) {
            return false;
        }

        return cameraPos.y < (double) blockPos.getY() + this.aquaAcrobatics$getWaterHeight(world, blockPos, state);
    }

    @Unique
    private Vec3d aquaAcrobatics$getCameraPosition(Entity entity, float partialTicks) {
        double x = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
        double y = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks;
        double z = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;
        Vec3d eyePos = new Vec3d(x, y + (double) this.aquaAcrobatics$getCameraEyeHeight(entity, partialTicks), z);
        if (this.mc.gameSettings.thirdPersonView <= 0 || this.mc.gameSettings.debugCamEnable) {
            return eyePos;
        }

        return this.aquaAcrobatics$getThirdPersonCameraPosition(entity, partialTicks, eyePos);
    }

    @Unique
    private Vec3d aquaAcrobatics$getThirdPersonCameraPosition(Entity entity, float partialTicks, Vec3d eyePos) {
        double cameraDistance = this.thirdPersonDistancePrev + (4.0F - this.thirdPersonDistancePrev) * partialTicks;
        float yaw = entity.rotationYaw;
        float pitch = entity.rotationPitch;
        if (this.mc.gameSettings.thirdPersonView == 2) {
            pitch += 180.0F;
        }

        double xOffset = (double) (-MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F)) * cameraDistance;
        double zOffset = (double) (MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F)) * cameraDistance;
        double yOffset = (double) (-MathHelper.sin(pitch * 0.017453292F)) * cameraDistance;

        for (int i = 0; i < 8; ++i) {
            float xJitter = (float) ((i & 1) * 2 - 1) * 0.1F;
            float yJitter = (float) ((i >> 1 & 1) * 2 - 1) * 0.1F;
            float zJitter = (float) ((i >> 2 & 1) * 2 - 1) * 0.1F;
            Vec3d from = eyePos.add(xJitter, yJitter, zJitter);
            Vec3d to = new Vec3d(eyePos.x - xOffset + (double) xJitter + (double) zJitter, eyePos.y - yOffset + (double) yJitter, eyePos.z - zOffset + (double) zJitter);
            RayTraceResult result = this.mc.world.rayTraceBlocks(from, to);
            if (result != null) {
                double hitDistance = result.hitVec.distanceTo(eyePos);
                if (hitDistance < cameraDistance) {
                    cameraDistance = hitDistance;
                }
            }
        }

        xOffset = (double) (-MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F)) * cameraDistance;
        zOffset = (double) (MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F)) * cameraDistance;
        yOffset = (double) (-MathHelper.sin(pitch * 0.017453292F)) * cameraDistance;
        return new Vec3d(eyePos.x - xOffset, eyePos.y - yOffset, eyePos.z - zOffset);
    }

    @Unique
    private float aquaAcrobatics$getCameraEyeHeight(Entity entity, float partialTicks) {
        if (entity instanceof EntityPlayer && !IntegrationManager.isRandomPatchesEnabled()) {
            return MathHelperNew.lerp(partialTicks, this.aquaAcrobatics$previousEyeHeight, this.aquaAcrobatics$eyeHeight);
        }
        return entity.getEyeHeight();
    }

    @Unique
    private float aquaAcrobatics$getWaterHeight(World world, BlockPos pos, IBlockState state) {
        Block block = state.getBlock();
        if (block instanceof IFluidBlock) {
            float filled = ((IFluidBlock) block).getFilledPercentage(world, pos);
            return filled < 0.0F ? filled + 1.0F : filled;
        }
        if (block instanceof BlockLiquid) {
            return BlockLiquid.getBlockLiquidHeight(state, world, pos);
        }
        float height = block.getBlockLiquidHeight(world, pos, state, Material.WATER);
        return height > 0.0F ? height : 1.0F;
    }
    // Backport end - Camera logic from modern versions

    /**
     * This mixin is marked as not required, as some mods patch this themselves.
     */
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
        /* 1.13 removed this check */
        if (material == Material.WATER) {
            return false;
        }
        return entity.isInsideOfMaterial(material);
    }
}