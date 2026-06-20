package sheg1_steparm.aquaacrobaticsunofficial.core.vanilla.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sheg1_steparm.aquaacrobaticsunofficial.client.entity.IPlayerSPSwimming;
import sheg1_steparm.aquaacrobaticsunofficial.config.ConfigHandler;
import sheg1_steparm.aquaacrobaticsunofficial.entity.Pose;
import sheg1_steparm.aquaacrobaticsunofficial.entity.player.IPlayerResizeable;
import sheg1_steparm.aquaacrobaticsunofficial.integration.IntegrationManager;
import sheg1_steparm.aquaacrobaticsunofficial.util.MovementInputStorage;
import sheg1_steparm.aquaacrobaticsunofficial.util.math.AxisAlignedBBSpliterator;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("unused")
@Mixin(EntityPlayerSP.class)
public abstract class EntityPlayerSPMixin extends AbstractClientPlayer implements IPlayerSPSwimming {
    @Unique
    private final MovementInputStorage aquaAcrobatics$movementStorage = new MovementInputStorage();
    @Shadow
    @Final
    public NetHandlerPlayClient connection;
    @Shadow
    public MovementInput movementInput;
    @Shadow
    protected Minecraft mc;
    @Shadow
    protected int sprintToggleTimer;
    @Shadow
    private int autoJumpTime;
    @Unique
    private boolean aquaAcrobatics$isCrouching;

    public EntityPlayerSPMixin(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Unique
    private static boolean aquaAcrobatics$isAxisAlignedBBNotClear(World world, @Nullable Entity entity, AxisAlignedBB aabb) {
        return aquaAcrobatics$createAxisAlignedBBStream(world, entity, aabb).allMatch(Objects::isNull);
    }

    @Unique
    private static Stream<AxisAlignedBB> aquaAcrobatics$createAxisAlignedBBStream(World world, @Nullable Entity entity, AxisAlignedBB aabb) {
        return StreamSupport.stream(new AxisAlignedBBSpliterator(world, entity, aabb), false);
    }

    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isSneaking()Z"))
    private boolean onUpdateWalkingPlayerIsSneaking(EntityPlayerSP playerIn) {
        // send actual sneak state to server
        return this.aquaAcrobatics$isActuallySneaking();
    }

    @Inject(method = "isSneaking", at = @At("HEAD"), cancellable = true)
    public void isSneaking(CallbackInfoReturnable<Boolean> callbackInfo) {
        // don't check this directly every time to prevent crash with random things mod caused by loop
        callbackInfo.setReturnValue(this.aquaAcrobatics$isCrouching);
    }

    @Override
    public void spawnRunningParticles() {
        if (!this.capabilities.isFlying && !this.isSpectator() && !this.isSneaking() && !this.isInLava() && this.isEntityAlive()) {
            super.spawnRunningParticles();
        }
    }

    @Override
    public boolean aquaAcrobatics$isActuallySneaking() {
        // switched with #isSneaking
        return this.movementInput != null && this.movementInput.sneak;
    }

    @Override
    public boolean aquaAcrobatics$isForcedDown() {
        return ((IPlayerResizeable) this).aquaAcrobatics$isResizingAllowed() && !this.capabilities.isFlying ? this.isSneaking() || ((IPlayerResizeable) this).aquaAcrobatics$isVisuallySwimming() : this.aquaAcrobatics$isActuallySneaking();
    }

    @Override
    public boolean aquaAcrobatics$isUsingSwimmingAnimation() {
        return this.aquaAcrobatics$isUsingSwimmingAnimation(this.movementInput.moveForward, this.movementInput.moveStrafe);
    }

    @Override
    public boolean aquaAcrobatics$isUsingSwimmingAnimation(float moveForward, float moveStrafe) {
        if (this.aquaAcrobatics$canSwim()) {
            return this.aquaAcrobatics$isMovingForward(moveForward, moveStrafe);
        }
        if (ConfigHandler.MOVEMENT_CONFIG.sidewaysSprinting) {
            return moveForward >= 0.8F || Math.abs(moveStrafe) > 0.8F;
        }
        return moveForward >= 0.8F;
    }

    @Override
    public boolean aquaAcrobatics$canSwim() {
        return ((IPlayerResizeable) this).aquaAcrobatics$getEyesInWaterPlayer();
    }

    @Override
    public boolean aquaAcrobatics$isMovingForward(float moveForward, float moveStrafe) {
        if (moveForward > 1.0E-5F) {
            return true;
        } else if (ConfigHandler.MOVEMENT_CONFIG.sidewaysSwimming) {
            return Math.abs(moveStrafe) > 1.0E-5F;
        }
        return false;
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    protected void pushOutOfBlocks(double x, double y, double z, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (ConfigHandler.playerBlockCollisions != ConfigHandler.PlayerBlockCollisions.EXACT) {
            return;
        }
        if (!this.noClip) {
            this.aquaAcrobatics$setPlayerOffsetMotion(x, z);
        }
        // return value is never used
        callbackInfo.setReturnValue(false);
    }

    @Unique
    private void aquaAcrobatics$setPlayerOffsetMotion(double x, double z) {
        BlockPos blockpos = new BlockPos(x, this.posY, z);
        if (this.aquaAcrobatics$shouldBlockPushPlayer(blockpos)) {
            double d0 = x - blockpos.getX();
            double d1 = z - blockpos.getZ();
            EnumFacing direction = null;
            double d2 = Double.MAX_VALUE;
            EnumFacing[] xzPlane = new EnumFacing[]{EnumFacing.WEST, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH};

            for (EnumFacing direction1 : xzPlane) {
                EnumFacing.Axis axis = direction1.getAxis();
                double d3 = axis == EnumFacing.Axis.X ? d0 : axis == EnumFacing.Axis.Z ? d1 : 0.0;
                double d4 = direction1.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 1.0 - d3 : d3;
                if (d4 < d2 && !this.aquaAcrobatics$shouldBlockPushPlayer(blockpos.offset(direction1))) {
                    d2 = d4;
                    direction = direction1;
                }
            }

            if (direction != null) {
                if (direction.getAxis() == EnumFacing.Axis.X) {
                    this.motionX = 0.1 * direction.getDirectionVec().getX();
                } else {
                    this.motionZ = 0.1 * direction.getDirectionVec().getZ();
                }
            }
        }
    }

    @Unique
    private boolean aquaAcrobatics$shouldBlockPushPlayer(BlockPos pos) {
        double minY = this.getEntityBoundingBox().minY;
        double maxY = this.getEntityBoundingBox().maxY;
        AxisAlignedBB aabb = new AxisAlignedBB(pos.getX(), minY, pos.getZ(), pos.getX() + 1.0, maxY, pos.getZ() + 1.0);

        // don't use IBlockState#causesSuffocation as it works differently in newer versions
        return !aquaAcrobatics$isAxisAlignedBBNotClear(this.world, this, aabb.shrink(1.0E-7));
    }

    @Dynamic
    @Redirect(method = {"pushOutOfBlocks"}, at = @At(value = "INVOKE", target = "Ljava/lang/Math;ceil(D)D"))
    private double ceil(double a) {
        if (ConfigHandler.playerBlockCollisions == ConfigHandler.PlayerBlockCollisions.APPROXIMATE) {
            a -= 0.65;
        }

        // make the player be able to sneak under full cubes with their new height of 1.5 blocks
        return Math.ceil(a);
    }

    @Inject(method = "onLivingUpdate", at = @At("HEAD"))
    public void onLivingUpdatePre(CallbackInfo callbackInfo) {
        this.aquaAcrobatics$updateSprintToggleTimer();
        this.aquaAcrobatics$movementStorage.copyFrom(this.movementInput);
        this.aquaAcrobatics$movementStorage.isSprinting = this.isSprinting();
        this.aquaAcrobatics$movementStorage.isFlying = this.capabilities.isFlying;
        this.aquaAcrobatics$movementStorage.isStartingToFly = this.aquaAcrobatics$isStartingToFly();
    }

    @Unique
    private void aquaAcrobatics$updateSprintToggleTimer() {
        // added in 1.13+, so do this for the actual field
        if (this.movementInput.sneak) {
            this.sprintToggleTimer = 0;
        }

        this.aquaAcrobatics$movementStorage.sprintToggleTimer = this.sprintToggleTimer;
        if (this.aquaAcrobatics$movementStorage.sprintToggleTimer > 0) {
            --this.aquaAcrobatics$movementStorage.sprintToggleTimer;
        }
        if (this.isHandActive() && !this.isRiding()) {
            this.aquaAcrobatics$movementStorage.sprintToggleTimer = 0;
        }
    }

    @Unique
    private boolean aquaAcrobatics$isStartingToFly() {
        if (this.capabilities.allowFlying) {
            if (this.mc.playerController.isSpectatorMode()) {
                return !this.capabilities.isFlying;
            } else if (!this.movementInput.jump && this.mc.gameSettings.keyBindJump.isKeyDown() && this.autoJumpTime == 0) {
                return this.flyToggleTimer != 0 && !((IPlayerResizeable) this).aquaAcrobatics$isSwimming();
            }
        }
        return false;
    }

    @Dynamic
    @Inject(method = {"onLivingUpdate"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/entity/EntityPlayerSP;wasFallFlying:Z", opcode = Opcodes.PUTFIELD))
    public void onLivingUpdate(CallbackInfo callbackInfo) {
        this.aquaAcrobatics$updatePlayerMoveState();
        this.aquaAcrobatics$isCrouching = this.aquaAcrobatics$isCrouching(!((IPlayerResizeable) this).aquaAcrobatics$isPoseClear(Pose.STANDING));
        // handle sprinting behaviour
        this.setSprinting(this.aquaAcrobatics$movementStorage.isSprinting);
        boolean isSaturated = (float) this.getFoodStats().getFoodLevel() > 6.0F || this.capabilities.allowFlying;
        this.aquaAcrobatics$startSprinting(isSaturated);
        this.aquaAcrobatics$stopSprinting(isSaturated);
        // handle misc movement
        this.aquaAcrobatics$handleElytraTakeoff();
        this.aquaAcrobatics$handleWaterSneaking();
        this.aquaAcrobatics$slowDownSneakFlying();
    }

    @Unique
    private void aquaAcrobatics$updatePlayerMoveState() {
        if (!this.movementInput.sneak && this.aquaAcrobatics$isForcedDown()) {
            this.movementInput.moveStrafe = (float) ((double) this.movementInput.moveStrafe * 0.3);
            this.movementInput.moveForward = (float) ((double) this.movementInput.moveForward * 0.3);
        }
        if (this.movementInput.sneak && !this.aquaAcrobatics$isForcedDown()) {
            this.movementInput.moveStrafe = (float) ((double) this.movementInput.moveStrafe / 0.3);
            this.movementInput.moveForward = (float) ((double) this.movementInput.moveForward / 0.3);
        }
    }

    @Unique
    private boolean aquaAcrobatics$isCrouching(boolean cantStand) {
        if ((!this.aquaAcrobatics$movementStorage.isFlying || !cantStand) && this.getTicksElytraFlying() <= 4) {
            if (!((IPlayerResizeable) this).aquaAcrobatics$isSwimming() && (this.onGround || !this.isInWater())) {
                if (!this.isOnLadder() && (((IPlayerResizeable) this).aquaAcrobatics$isPoseClear(Pose.CROUCHING) || this.noClip)) {
                    return this.movementInput.sneak || ((IPlayerResizeable) this).aquaAcrobatics$isResizingAllowed() && !this.isPlayerSleeping() && cantStand;
                }
            }
        }
        return false;
    }

    @Unique
    private void aquaAcrobatics$startSprinting(boolean isSaturated) {
        boolean wasSneaking = this.aquaAcrobatics$movementStorage.sneak;
        boolean wasSwimming = this.aquaAcrobatics$isUsingSwimmingAnimation(this.aquaAcrobatics$movementStorage.moveForward, this.aquaAcrobatics$movementStorage.moveStrafe);
        boolean isSprintingEnvironment = this.onGround || this.aquaAcrobatics$canSwim() || this.aquaAcrobatics$movementStorage.isFlying;
        if (isSprintingEnvironment && !wasSneaking && !wasSwimming && this.aquaAcrobatics$isUsingSwimmingAnimation() && !this.isSprinting() && isSaturated && !this.isHandActive() && !this.isPotionActive(MobEffects.BLINDNESS)) {
            if (this.aquaAcrobatics$movementStorage.sprintToggleTimer <= 0 && !this.mc.gameSettings.keyBindSprint.isKeyDown()) {
                this.sprintToggleTimer = ConfigHandler.MOVEMENT_CONFIG.noDoubleTapSprinting ? 0 : 7;
            } else {
                this.setSprinting(true);
            }
        }
        if (!this.isSprinting() && (!this.isInWater() || this.aquaAcrobatics$canSwim()) && this.aquaAcrobatics$isUsingSwimmingAnimation() && isSaturated && !this.isHandActive() && !this.isPotionActive(MobEffects.BLINDNESS) && this.mc.gameSettings.keyBindSprint.isKeyDown()) {
            this.setSprinting(true);
        }
    }

    @Unique
    private void aquaAcrobatics$stopSprinting(boolean isSaturated) {
        if (this.isSprinting()) {
            boolean isNotMoving = !this.aquaAcrobatics$isMovingForward(this.movementInput.moveForward, this.movementInput.moveStrafe) || !isSaturated;
            // don't stop sprint flying when breaching water surface
            boolean hasCollided = isNotMoving || this.collidedHorizontally || this.isInWater() && !this.aquaAcrobatics$canSwim() && !this.aquaAcrobatics$movementStorage.isFlying;
            if (((IPlayerResizeable) this).aquaAcrobatics$isSwimming()) {
                if (!this.movementInput.sneak && isNotMoving || !this.isInWater()) {
                    this.setSprinting(false);
                }
            } else if (hasCollided) {
                this.setSprinting(false);
            }
        }
    }

    @Override
    public boolean aquaAcrobatics$canPerformElytraTakeoff() {
        return (ConfigHandler.MOVEMENT_CONFIG.easyElytraTakeoff && this.movementInput.jump && !this.aquaAcrobatics$movementStorage.isStartingToFly && !this.aquaAcrobatics$movementStorage.jump && this.motionY >= 0.0 && !this.capabilities.isFlying && !this.isRiding() && !this.isOnLadder());
    }

    @Unique
    private void aquaAcrobatics$handleElytraTakeoff() {
        // 1.15 change for easier elytra takeoff
        if (aquaAcrobatics$canPerformElytraTakeoff()) {
            ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (itemstack.getItem() == Items.ELYTRA && ItemElytra.isUsable(itemstack)) {
                this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_FALL_FLYING));
            } else {
                IntegrationManager.elytraOpenHooks.forEach(hook -> hook.openElytra((EntityPlayerSP) (Object) this));
            }
        }
    }

    @Unique
    private void aquaAcrobatics$handleWaterSneaking() {
        // needs to be handled on the client since the server doesn't receive actual sneak state while in water
        if (this.isInWater() && this.movementInput.sneak && !this.capabilities.isFlying) {
            this.aquaAcrobatics$handleSneakWater();
        }
    }

    @Unique
    private void aquaAcrobatics$slowDownSneakFlying() {
        if (this.capabilities.isFlying && this.isCurrentViewEntity()) {
            if (this.movementInput.sneak) {
                // normally used to counter sneaking slowdown when flying, but sneaking is no longer activated while flying now
                this.movementInput.moveStrafe = (float) ((double) this.movementInput.moveStrafe * 0.3);
                this.movementInput.moveForward = (float) ((double) this.movementInput.moveForward * 0.3);
            }
        }
    }

    @Unique
    protected void aquaAcrobatics$handleSneakWater() {
        this.motionY -= 0.03999999910593033 * this.getEntityAttribute(SWIM_SPEED).getAttributeValue();
    }

    @Shadow
    protected abstract boolean isCurrentViewEntity();
}