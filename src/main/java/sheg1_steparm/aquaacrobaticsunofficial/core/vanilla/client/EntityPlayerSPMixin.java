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
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Mixin(EntityPlayerSP.class)
public abstract class EntityPlayerSPMixin extends AbstractClientPlayer implements IPlayerSPSwimming {

    @Shadow
    @Final
    public NetHandlerPlayClient connection;

    @Shadow
    protected Minecraft mc;

    @Shadow
    protected int sprintToggleTimer;

    @Shadow
    private int autoJumpTime;

    @Shadow
    public MovementInput movementInput;

    private final MovementInputStorage movementStorage = new MovementInputStorage();

    private boolean isCrouching;

    public EntityPlayerSPMixin(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isSneaking()Z"))
    private boolean onUpdateWalkingPlayerIsSneaking(EntityPlayerSP playerIn) {
        return isActuallySneaking();
    }

    @Inject(method = "isSneaking", at = @At("HEAD"), cancellable = true)
    public void isSneaking(CallbackInfoReturnable<Boolean> callbackInfo) {
        callbackInfo.setReturnValue(this.isCrouching);
    }

    @Override
    public void spawnRunningParticles() {
        if (!this.capabilities.isFlying && !this.isSpectator() && !this.isSneaking() && !this.isInLava() && this.isEntityAlive()) {
            super.spawnRunningParticles();
        }
    }

    public boolean isActuallySneaking() {
        return this.movementInput != null && this.movementInput.sneak;
    }

    public boolean isForcedDown() {
        return ((IPlayerResizeable) this).aquaAcrobatics$isResizingAllowed() && !this.capabilities.isFlying ? this.isSneaking() || ((IPlayerResizeable) this).aquaAcrobatics$isVisuallySwimming() : isActuallySneaking();
    }

    public boolean isUsingSwimmingAnimation() {
        return isUsingSwimmingAnimation(this.movementInput.moveForward, this.movementInput.moveStrafe);
    }

    public boolean isUsingSwimmingAnimation(float moveForward, float moveStrafe) {
        if (canSwim()) {
            return isMovingForward(moveForward, moveStrafe);
        }
        if (ConfigHandler.MOVEMENT_CONFIG.sidewaysSprinting) {
            return moveForward >= 0.8F || Math.abs(moveStrafe) > 0.8F;
        }
        return moveForward >= 0.8F;
    }

    public boolean canSwim() {
        return ((IPlayerResizeable) this).aquaAcrobatics$getEyesInWaterPlayer();
    }

    public boolean isMovingForward(float moveForward, float moveStrafe) {
        if (moveForward > 1.0E-5F) {
            return true;
        }
        if (ConfigHandler.MOVEMENT_CONFIG.sidewaysSwimming) {
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
            setPlayerOffsetMotion(x, z);
        }
        callbackInfo.setReturnValue(false);
    }

    private void setPlayerOffsetMotion(double x, double z) {
        BlockPos blockpos = new BlockPos(x, this.posY, z);
        if (shouldBlockPushPlayer(blockpos)) {
            double d0 = x - blockpos.getX();
            double d1 = z - blockpos.getZ();
            EnumFacing direction = null;
            double d2 = Double.MAX_VALUE;
            EnumFacing[] xzPlane = {EnumFacing.WEST, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH};
            for (EnumFacing direction1 : xzPlane) {
                EnumFacing.Axis axis = direction1.getAxis();
                double d3 = axis == EnumFacing.Axis.X ? d0 : axis == EnumFacing.Axis.Z ? d1 : 0.0D;
                double d4 = direction1.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 1.0D - d3 : d3;
                if (d4 < d2 && !shouldBlockPushPlayer(blockpos.offset(direction1))) {
                    d2 = d4;
                    direction = direction1;
                }
            }
            if (direction != null) {
                if (direction.getAxis() == EnumFacing.Axis.X) {
                    this.motionX = 0.1D * direction.getDirectionVec().getX();
                } else {
                    this.motionZ = 0.1D * direction.getDirectionVec().getZ();
                }
            }
        }
    }

    private boolean shouldBlockPushPlayer(BlockPos pos) {
        double minY = this.getEntityBoundingBox().minY;
        double maxY = this.getEntityBoundingBox().maxY;
        AxisAlignedBB aabb = new AxisAlignedBB(pos.getX(), minY, pos.getZ(), pos.getX() + 1.0D, maxY, pos.getZ() + 1.0D);
        return !isAxisAlignedBBNotClear(this.world, this, aabb.shrink(1.0E-7D));
    }

    private static boolean isAxisAlignedBBNotClear(World world, @Nullable Entity entity, AxisAlignedBB aabb) {
        return createAxisAlignedBBStream(world, entity, aabb).allMatch(Objects::isNull);
    }

    private static Stream<AxisAlignedBB> createAxisAlignedBBStream(World world, @Nullable Entity entity, AxisAlignedBB aabb) {
        return StreamSupport.stream(new AxisAlignedBBSpliterator(world, entity, aabb), false);
    }

    @Redirect(method = {"pushOutOfBlocks", "localPushOutOfBlocks"}, at = @At(value = "INVOKE", target = "Ljava/lang/Math;ceil(D)D"))
    private double ceil(double a) {
        if (ConfigHandler.playerBlockCollisions == ConfigHandler.PlayerBlockCollisions.APPROXIMATE) {
            a -= 0.65D;
        }
        return Math.ceil(a);
    }

    @Inject(method = "onLivingUpdate", at = @At("HEAD"))
    public void onLivingUpdatePre(CallbackInfo callbackInfo) {
        updateSprintToggleTimer();
        this.movementStorage.copyFrom(this.movementInput);
        this.movementStorage.isSprinting = this.isSprinting();
        this.movementStorage.isFlying = this.capabilities.isFlying;
        this.movementStorage.isStartingToFly = isStartingToFly();
    }

    private void updateSprintToggleTimer() {
        if (this.movementInput.sneak) {
            this.sprintToggleTimer = 0;
        }
        this.movementStorage.sprintToggleTimer = this.sprintToggleTimer;
        if (this.movementStorage.sprintToggleTimer > 0) {
            this.movementStorage.sprintToggleTimer--;
        }
        if (this.isHandActive() && !this.isRiding()) {
            this.movementStorage.sprintToggleTimer = 0;
        }
    }

    private boolean isStartingToFly() {
        if (this.capabilities.allowFlying) {
            if (this.mc.playerController.isSpectatorMode()) {
                return !this.capabilities.isFlying;
            }
            if (!this.movementInput.jump && this.mc.gameSettings.keyBindJump.isKeyDown() && this.autoJumpTime == 0) {
                return this.flyToggleTimer != 0 && !((IPlayerResizeable) this).aquaAcrobatics$isSwimming();
            }
        }
        return false;
    }

    @Inject(method = {"onLivingUpdate", "localOnLivingUpdate"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/entity/EntityPlayerSP;wasFallFlying:Z"))
    public void onLivingUpdate(CallbackInfo callbackInfo) {
        updatePlayerMoveState();
        this.isCrouching = isCrouching(!((IPlayerResizeable) this).aquaAcrobatics$isPoseClear(Pose.STANDING));

        this.setSprinting(this.movementStorage.isSprinting);
        boolean isSaturated = this.getFoodStats().getFoodLevel() > 6.0F || this.capabilities.allowFlying;
        startSprinting(isSaturated);
        stopSprinting(isSaturated);
        handleElytraTakeoff();
        handleWaterSneaking();
        slowDownSneakFlying();
    }

    private void updatePlayerMoveState() {
        if (!this.movementInput.sneak && isForcedDown()) {
            this.movementInput.moveStrafe = (float) (this.movementInput.moveStrafe * 0.3D);
            this.movementInput.moveForward = (float) (this.movementInput.moveForward * 0.3D);
        }
        if (this.movementInput.sneak && !isForcedDown()) {
            this.movementInput.moveStrafe = (float) (this.movementInput.moveStrafe / 0.3D);
            this.movementInput.moveForward = (float) (this.movementInput.moveForward / 0.3D);
        }
    }

    private boolean isCrouching(boolean cantStand) {
        if ((!this.movementStorage.isFlying || !cantStand) && this.getTicksElytraFlying() <= 4) {
            if (!((IPlayerResizeable) this).aquaAcrobatics$isSwimming() && (this.onGround || !this.isInWater())) {
                if (!this.isOnLadder() && (((IPlayerResizeable) this).aquaAcrobatics$isPoseClear(Pose.CROUCHING) || this.noClip)) {
                    return this.movementInput.sneak || (((IPlayerResizeable) this).aquaAcrobatics$isResizingAllowed() && !this.isPlayerSleeping() && cantStand);
                }
            }
        }
        return false;
    }

    private void startSprinting(boolean isSaturated) {
        boolean wasSneaking = this.movementStorage.sneak;
        boolean wasSwimming = isUsingSwimmingAnimation(this.movementStorage.moveForward, this.movementStorage.moveStrafe);
        boolean isSprintingEnvironment = this.onGround || canSwim() || this.movementStorage.isFlying;
        if (isSprintingEnvironment && !wasSneaking && !wasSwimming && isUsingSwimmingAnimation() && !this.isSprinting() && isSaturated && !this.isHandActive() && !this.isPotionActive(MobEffects.BLINDNESS)) {
            if (this.movementStorage.sprintToggleTimer <= 0 && !this.mc.gameSettings.keyBindSprint.isKeyDown()) {
                this.sprintToggleTimer = ConfigHandler.MOVEMENT_CONFIG.noDoubleTapSprinting ? 0 : 7;
            } else {
                this.setSprinting(true);
            }
        }
        if (!this.isSprinting() && (!this.isInWater() || canSwim()) && isUsingSwimmingAnimation() && isSaturated && !this.isHandActive() && !this.isPotionActive(MobEffects.BLINDNESS) && this.mc.gameSettings.keyBindSprint.isKeyDown()) {
            this.setSprinting(true);
        }
    }

    private void stopSprinting(boolean isSaturated) {
        if (this.isSprinting()) {
            boolean isNotMoving = !isMovingForward(this.movementInput.moveForward, this.movementInput.moveStrafe) || !isSaturated;
            boolean hasCollided = isNotMoving || this.collidedHorizontally || (this.isInWater() && !canSwim() && !this.movementStorage.isFlying);
            if (((IPlayerResizeable) this).aquaAcrobatics$isSwimming()) {
                if ((!this.movementInput.sneak && isNotMoving) || !this.isInWater()) {
                    this.setSprinting(false);
                }
            } else if (hasCollided) {
                this.setSprinting(false);
            }
        }
    }

    public boolean canPerformElytraTakeoff() {
        return ConfigHandler.MOVEMENT_CONFIG.easyElytraTakeoff && this.movementInput.jump && !this.movementStorage.isStartingToFly && !this.movementStorage.jump && this.motionY >= 0.0D && !this.capabilities.isFlying && !this.isRiding() && !this.isOnLadder();
    }

    private void handleElytraTakeoff() {
        if (canPerformElytraTakeoff()) {
            ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (itemstack.getItem() == Items.ELYTRA && ItemElytra.isUsable(itemstack)) {
                this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_FALL_FLYING));
            } else {
                IntegrationManager.elytraOpenHooks.forEach(hook -> hook.openElytra((EntityPlayerSP) (Object) this));
            }
        }
    }

    private void handleWaterSneaking() {
        if (this.isInWater() && this.movementInput.sneak && !this.capabilities.isFlying) {
            handleSneakWater();
        }
    }

    private void slowDownSneakFlying() {
        if (this.capabilities.isFlying && isCurrentViewEntity()) {
            if (this.movementInput.sneak) {
                this.movementInput.moveStrafe = (float) (this.movementInput.moveStrafe * 0.3D);
                this.movementInput.moveForward = (float) (this.movementInput.moveForward * 0.3D);
            }
        }
    }

    protected void handleSneakWater() {
        this.motionY -= 0.03999999910593033D * this.getEntityAttribute(SWIM_SPEED).getAttributeValue();
    }

    @Shadow
    protected abstract boolean isCurrentViewEntity();
}