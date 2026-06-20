package sheg1_steparm.aquaacrobaticsunofficial.core.vanilla;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.passive.EntityFlying;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sheg1_steparm.aquaacrobaticsunofficial.config.ConfigHandler;
import sheg1_steparm.aquaacrobaticsunofficial.entity.EntitySize;
import sheg1_steparm.aquaacrobaticsunofficial.entity.Pose;
import sheg1_steparm.aquaacrobaticsunofficial.entity.player.IPlayerResizeable;
import sheg1_steparm.aquaacrobaticsunofficial.integration.IntegrationManager;
import sheg1_steparm.aquaacrobaticsunofficial.integration.artemislib.ArtemisLibIntegration;
import sheg1_steparm.aquaacrobaticsunofficial.integration.betweenlands.BetweenlandsIntegration;
import sheg1_steparm.aquaacrobaticsunofficial.integration.chiseledme.ChiseledMeIntegration;
import sheg1_steparm.aquaacrobaticsunofficial.integration.morph.MorphIntegration;
import sheg1_steparm.aquaacrobaticsunofficial.integration.trinketsandbaubles.TrinketsAndBaublesIntegration;
import sheg1_steparm.aquaacrobaticsunofficial.integration.wings.WingsIntegration;
import sheg1_steparm.aquaacrobaticsunofficial.integration.witchery.WitcheryResurrectedIntegration;
import sheg1_steparm.aquaacrobaticsunofficial.network.datasync.PoseSerializer;
import sheg1_steparm.aquaacrobaticsunofficial.util.math.MathHelperNew;

import javax.annotation.Nonnull;
import java.util.Map;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase implements IPlayerResizeable {
    @Unique
    private static final EntitySize STANDING_SIZE = EntitySize.flexible(0.6F, 1.8F);
    @Unique
    private static final Map<Pose, EntitySize> SIZE_BY_POSE = ImmutableMap.<Pose, EntitySize>builder().put(Pose.STANDING, STANDING_SIZE).put(Pose.SLEEPING, EntitySize.fixed(0.2F, 0.2F)).put(Pose.FALL_FLYING, EntitySize.flexible(0.6F, 0.6F)).put(Pose.SWIMMING, EntitySize.flexible(0.6F, 0.6F)).put(Pose.SPIN_ATTACK, EntitySize.flexible(0.6F, 0.6F)).put(Pose.CROUCHING, EntitySize.flexible(0.6F, 1.5F)).put(Pose.DYING, EntitySize.fixed(0.2F, 0.2F)).build();
    @Unique
    private static final DataParameter<Pose> aquaAcrobatics$POSE = EntityDataManager.createKey(EntityPlayerMixin.class, PoseSerializer.POSE);
    @Unique
    private static final DataParameter<Boolean> TOGGLED_CRAWLING = EntityDataManager.createKey(EntityPlayerMixin.class, DataSerializers.BOOLEAN);
    @Shadow
    public PlayerCapabilities capabilities;
    @Shadow
    public float prevCameraYaw;
    @Shadow
    public float cameraYaw;
    @Shadow(remap = false)
    public float eyeHeight;
    @Unique
    protected boolean aquaAcrobatics$eyesInWater;
    @Unique
    protected boolean aquaAcrobatics$eyesInWaterPlayer;
    @Unique
    private EntitySize aquaAcrobatics$size;
    // Forge adds an eyeHeight field, we need a different name
    @Unique
    private float aquaAcrobatics$playerEyeHeight;
    @Unique
    private float aquaAcrobatics$previousEyeHeight;
    @Unique
    private float aquaAcrobatics$swimAnimation;
    @Unique
    private float aquaAcrobatics$lastSwimAnimation;
    @Unique
    private float aquaAcrobatics$timeUnderwater;
    @Unique
    private boolean aquaAcrobatics$inBubbleColumn;

    public EntityPlayerMixin(World worldIn) {
        super(worldIn);
    }

    @Unique
    private float aquaAcrobatics$findEntitySizeScaleFactor() {
        float finalFactor = 1f;
        if (IntegrationManager.isTrinketsAndBaublesEnabled()) {
            finalFactor *= TrinketsAndBaublesIntegration.getResizeFactor((EntityPlayer) (Object) this);
        }
        if (IntegrationManager.isChiseledMeEnabled()) {
            finalFactor *= ChiseledMeIntegration.getResizeFactor((EntityPlayer) (Object) this);
        }
        return finalFactor;
    }

    @Unique
    private float aquaAcrobatics$findEyeScaleFactor() {
        float finalFactor = 1f;
        if (IntegrationManager.isArtemisLibEnabled()) {
            finalFactor *= ArtemisLibIntegration.getEyeFactor((EntityPlayer) (Object) this);
        }
        if (IntegrationManager.isChiseledMeEnabled()) {
            finalFactor *= ChiseledMeIntegration.getResizeFactor((EntityPlayer) (Object) this);
        }
        return finalFactor;
    }

    @Unique
    private EntitySize aquaAcrobatics$handleEntitySizeScaling(EntitySize in) {
        float finalFactor = aquaAcrobatics$findEntitySizeScaleFactor();
        if (finalFactor == 1f) {
            return in;
        } else {
            return in.scale(finalFactor);
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructed(CallbackInfo callbackInfo) {
        this.aquaAcrobatics$size = aquaAcrobatics$handleEntitySizeScaling(EntitySize.flexible(0.6F, 1.8F));
        this.aquaAcrobatics$playerEyeHeight = this.aquaAcrobatics$getEyeHeight(Pose.STANDING, this.aquaAcrobatics$size);
        this.dataManager.register(aquaAcrobatics$POSE, Pose.STANDING);
        if (ConfigHandler.MOVEMENT_CONFIG.enableToggleCrawling) {
            this.dataManager.register(TOGGLED_CRAWLING, false);
        }
    }

    @Override
    public void notifyDataManagerChange(@Nonnull DataParameter<?> key) {
        if (aquaAcrobatics$POSE.equals(key)) {
            this.aquaAcrobatics$recalculateEyeHeight();
            this.aquaAcrobatics$recalculateSize();
            if (IntegrationManager.isArtemisLibEnabled()) {
                ArtemisLibIntegration.updateSwimmingSize(this.aquaAcrobatics$getPlayer(), this.aquaAcrobatics$getPose());
            }
        }

        super.notifyDataManagerChange(key);
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if (IntegrationManager.isWitcheryResurrectedEnabled() && WitcheryResurrectedIntegration.HAS_TRANSFORMED) {
            // A bit buggy, think some packages are not sent from Witchery. Sneaking updates the camera though.
            this.aquaAcrobatics$playerEyeHeight = this.aquaAcrobatics$getEyeHeight(Pose.STANDING, this.aquaAcrobatics$size);
            WitcheryResurrectedIntegration.HAS_TRANSFORMED = false;
        } else if (this.isInWater()) {
            int i = this.isSpectator() ? 10 : 1;
            this.aquaAcrobatics$timeUnderwater = MathHelper.clamp(this.aquaAcrobatics$timeUnderwater + i, 0, 600);
        } else if (this.aquaAcrobatics$timeUnderwater > 0) {
            this.aquaAcrobatics$timeUnderwater = MathHelper.clamp(this.aquaAcrobatics$timeUnderwater - 10, 0, 600);
        }

        // updateAquatics
        this.aquaAcrobatics$updateEyesInWater();
        this.aquaAcrobatics$updateSwimming();
    }

    // based on 1.16
    @Override
    public float aquaAcrobatics$getWaterVision() {
        if (!this.isInWater()) {
            return 0.0f;
        } else {
            if (this.aquaAcrobatics$timeUnderwater >= 600.0f) {
                return 1.0f;
            } else {
                float f2 = MathHelper.clamp(this.aquaAcrobatics$timeUnderwater / 100.0f, 0.0f, 1.0f);
                float f3 = this.aquaAcrobatics$timeUnderwater < 100.0f ? 0.0f : MathHelper.clamp((this.aquaAcrobatics$timeUnderwater - 100.0f) / 500.0f, 0.0f, 1.0f);
                return f2 * 0.6f + f3 * 0.39999998f;
            }
        }
    }

    @Override
    public boolean aquaAcrobatics$canForceCrawling() {
        return ConfigHandler.MOVEMENT_CONFIG.enableToggleCrawling && !this.isRiding() && !this.capabilities.isFlying && !this.isOnLadder();
    }

    @Override
    public boolean aquaAcrobatics$isForcingCrawling() {
        return this.aquaAcrobatics$canForceCrawling() && this.dataManager.get(TOGGLED_CRAWLING);
    }

    @Override
    public void aquaAcrobatics$setForcingCrawling(boolean flag) {
        if (!this.aquaAcrobatics$canForceCrawling()) {
            return;
        }
        this.dataManager.set(TOGGLED_CRAWLING, flag);
    }

    @Override
    public boolean aquaAcrobatics$canSwim() {
        return this.aquaAcrobatics$eyesInWater && this.isInWater();
    }

    @Override
    public void aquaAcrobatics$updateSwimming() {
        if (this.capabilities.isFlying) {
            this.aquaAcrobatics$setSwimming(false);
        } else if (this.aquaAcrobatics$isSwimming()) {
            this.aquaAcrobatics$setSwimming(this.isSprinting() && this.isInWater() && !this.isRiding());
        } else {
            this.aquaAcrobatics$setSwimming(this.isSprinting() && this.aquaAcrobatics$canSwim() && !this.isRiding());
        }
    }

    @Unique
    private void aquaAcrobatics$updateEyesInWater() {
        this.aquaAcrobatics$eyesInWater = this.isInsideOfMaterial(Material.WATER);
    }

    @Unique
    @SuppressWarnings("UnusedReturnValue")
    protected boolean aquaAcrobatics$updateEyesInWaterPlayer() {
        this.aquaAcrobatics$eyesInWaterPlayer = this.isInsideOfMaterial(Material.WATER);
        return this.aquaAcrobatics$eyesInWaterPlayer;
    }

    @Override
    public boolean aquaAcrobatics$getEyesInWaterPlayer() {
        return this.aquaAcrobatics$eyesInWaterPlayer;
    }

    @Override
    public final float aquaAcrobatics$getWidth() {
        return this.aquaAcrobatics$size.width;
    }

    @Override
    public final float aquaAcrobatics$getHeight() {
        return this.aquaAcrobatics$size.height;
    }

    @Override
    public EntitySize aquaAcrobatics$getSize(Pose poseIn) {
        return aquaAcrobatics$handleEntitySizeScaling(SIZE_BY_POSE.getOrDefault(poseIn, STANDING_SIZE));
    }

    @Override
    public void aquaAcrobatics$recalculateSize() {
        EntitySize oldSize = this.aquaAcrobatics$size;
        Pose pose = this.aquaAcrobatics$getPose();
        EntitySize newSize = this.aquaAcrobatics$getSize(pose);
        if (this.aquaAcrobatics$isResizingAllowed()) {
            this.aquaAcrobatics$recalculateSize(oldSize, newSize);
            // don't forget to update those
            this.width = newSize.width;
            this.height = newSize.height;
        }

        // update after calling #isResizingAllowed
        this.aquaAcrobatics$size = newSize;
    }

    @Unique
    protected void aquaAcrobatics$recalculateSize(EntitySize oldSize, EntitySize newSize) {
        if (newSize.width < oldSize.width) {
            double d0 = (double) newSize.width / 2.0;
            this.setEntityBoundingBox(new AxisAlignedBB(this.posX - d0, this.posY, this.posZ - d0, this.posX + d0, this.posY + (double) newSize.height, this.posZ + d0));
        } else {
            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
            this.setEntityBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double) newSize.width, axisalignedbb.minY + (double) newSize.height, axisalignedbb.minZ + (double) newSize.width));
            if (newSize.width > oldSize.width && !this.firstUpdate && !this.world.isRemote) {
                float distance = oldSize.width - newSize.width;
                this.move(MoverType.SELF, distance, 0.0, distance);
            }
        }
    }

    @Unique
    private void aquaAcrobatics$recalculateEyeHeight() {
        Pose pose = this.aquaAcrobatics$getPose();
        EntitySize entitysize = this.aquaAcrobatics$getSize(pose);
        this.aquaAcrobatics$playerEyeHeight = this.aquaAcrobatics$getEyeHeight(pose, entitysize);
        this.aquaAcrobatics$previousEyeHeight = this.eyeHeight;
    }

    @Override
    public boolean aquaAcrobatics$isResizingAllowed() {

        if (IntegrationManager.isMorphEnabled() && MorphIntegration.isMorphing(this.aquaAcrobatics$getPlayer())) {
            return false;
        }

        // is another mod interfering
        final float delta = 0.025F;
        AxisAlignedBB bb = this.getEntityBoundingBox();
        // something is not right
        if (this.width < delta || this.height < delta || bb.maxX - bb.minX < delta || bb.maxY - bb.minY < delta) {
            return true;
        }

        boolean sizeIsOk = Math.abs(this.width / this.aquaAcrobatics$getWidth() - 1.0F) < delta && Math.abs(this.height / this.aquaAcrobatics$getHeight() - 1.0F) < delta;
        boolean boundingBoxIsOk = Math.abs((bb.maxX - bb.minX) / this.aquaAcrobatics$getWidth() - 1.0F) < delta && Math.abs((bb.maxY - bb.minY) / this.aquaAcrobatics$getHeight() - 1.0F) < delta;
        return sizeIsOk && boundingBoxIsOk;
    }

    @Unique
    protected float aquaAcrobatics$getEyeHeight(Pose poseIn, EntitySize sizeIn) {
        if (IntegrationManager.isWitcheryResurrectedEnabled()) {
            switch (WitcheryResurrectedIntegration.getCurrentTransformation()) {
                case BAT:
                case WOLF:
                    return 0.5f;
            }
        }
        return poseIn == Pose.SLEEPING || poseIn == Pose.DYING ? 0.2F : this.aquaAcrobatics$getStandingEyeHeight(poseIn, sizeIn);
    }

    @Override
    public boolean aquaAcrobatics$isActuallySneaking() {
        return this.isSneaking();
    }

    @Override
    public float aquaAcrobatics$getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) {
        switch (poseIn) {
            case SWIMMING:
            case FALL_FLYING:
            case SPIN_ATTACK:
                return 0.4F;
            case CROUCHING:
                // far less than in vanilla 1.12, so better treat mods differently
                return this.eyeHeight - (this.aquaAcrobatics$isResizingAllowed() ? 0.35F : 0.08F);
            default:
                return this.eyeHeight;
        }
    }

    @Inject(method = "getEyeHeight", at = @At("HEAD"), cancellable = true)
    public final void getEyeHeight(CallbackInfoReturnable<Float> callbackInfoReturnable) {
        callbackInfoReturnable.setReturnValue(this.aquaAcrobatics$playerEyeHeight * aquaAcrobatics$findEyeScaleFactor());
    }

    @Shadow
    public abstract boolean isSpectator();

    @Override
    public Pose aquaAcrobatics$getPose() {
        return this.dataManager.get(aquaAcrobatics$POSE);
    }

    @Override
    public void aquaAcrobatics$setPose(Pose poseIn) {
        this.dataManager.set(aquaAcrobatics$POSE, poseIn);
    }

    @Override
    public boolean aquaAcrobatics$isPoseClear(Pose poseIn) {
        if (poseIn == Pose.CROUCHING && IntegrationManager.isBetweenlandsEnabled() && BetweenlandsIntegration.couldPlayerPhase((EntityPlayer) (Object) this)) {
            return true;
        }
        return this.world.getCollisionBoxes(this, this.aquaAcrobatics$getBoundingBox(poseIn)).isEmpty();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void preparePlayerToSpawn() {
        // need to overwrite whole method due to this being client exclusive
        this.aquaAcrobatics$setPose(Pose.STANDING);
        super.preparePlayerToSpawn();
        this.setHealth(this.getMaxHealth());
        this.deathTime = 0;
    }

    @Inject(method = "updateSize", at = @At("HEAD"), cancellable = true)
    protected void updateSize(CallbackInfo callbackInfo) {
        this.aquaAcrobatics$updateSwimAnimation();
        this.aquaAcrobatics$updateEyesInWaterPlayer();
        FMLCommonHandler.instance().onPlayerPostTick(this.aquaAcrobatics$getPlayer());
        // run after Forge event in case a mod still wants to do changes
        this.aquaAcrobatics$updatePose();
        this.aquaAcrobatics$updateEyeHeight();
        // cancel vanilla updateSize
        callbackInfo.cancel();
    }

    @Unique
    protected void aquaAcrobatics$updatePose() {
        if (this.aquaAcrobatics$getShouldBeDead()) {
            // this is completely ignored in vanilla
            this.aquaAcrobatics$setPose(Pose.DYING);
        } else if (this.isPlayerSleeping()) {
            // handle this before swimming pose clear check
            this.aquaAcrobatics$setPose(Pose.SLEEPING);
        } else if (this.aquaAcrobatics$isPoseClear(Pose.SWIMMING)) {
            Pose pose;
            if (IntegrationManager.isWingsEnabled() ? WingsIntegration.onFlightCheck(this.aquaAcrobatics$getPlayer(), this.isElytraFlying()) : this.isElytraFlying()) {
                pose = Pose.FALL_FLYING;
            } else if (this.aquaAcrobatics$isForcingCrawling() || this.aquaAcrobatics$isSwimming()) {
                pose = Pose.SWIMMING;
                // otherwise unable to sneak on client when there is not enough space for the pose, but actual player size is smaller
            } else if (this.aquaAcrobatics$isActuallySneaking() && !this.capabilities.isFlying && (this.onGround || !this.isInWater()) && !this.isOnLadder()) {
                pose = Pose.CROUCHING;
            } else {
                pose = Pose.STANDING;
            }

            Pose pose1;
            if (!this.noClip && !this.isRiding() && this.aquaAcrobatics$isResizingAllowed() && !this.aquaAcrobatics$isPoseClear(pose)) {
                if (this.aquaAcrobatics$isPoseClear(Pose.CROUCHING)) {
                    pose1 = Pose.CROUCHING;
                } else {
                    if (ConfigHandler.MOVEMENT_CONFIG.enableCrawling) {
                        pose1 = Pose.SWIMMING;
                    } else {
                        pose1 = Pose.STANDING;
                    }
                }
            } else {
                pose1 = pose;
            }

            this.aquaAcrobatics$setPose(pose1);
        }
    }

    @Unique
    private void aquaAcrobatics$updateEyeHeight() {
        if (this.eyeHeight != this.aquaAcrobatics$previousEyeHeight) {
            this.aquaAcrobatics$recalculateEyeHeight();
        }
    }

    @Unique
    protected AxisAlignedBB aquaAcrobatics$getBoundingBox(Pose p_213321_1_) {
        EntitySize entitysize = this.aquaAcrobatics$getSize(p_213321_1_);
        float f = entitysize.width / 2.0F;
        return new AxisAlignedBB(this.posX - (double) f, this.posY, this.posZ - (double) f, this.posX + (double) f, this.posY + (double) entitysize.height, this.posZ + (double) f);
    }

    @Override
    public boolean aquaAcrobatics$getShouldBeDead() {
        return this.getHealth() <= 0.0F;
    }

    @Override
    public boolean aquaAcrobatics$isSwimming() {
        return !this.capabilities.isFlying && !this.isSpectator() && this.getFlag(4);
    }

    @Override
    public void aquaAcrobatics$setSwimming(boolean flag) {
        this.setFlag(4, flag);
    }

    @Override
    public boolean aquaAcrobatics$isActuallySwimming() {
        boolean isFallFlying = !this.isElytraFlying() && this.aquaAcrobatics$getPose() == Pose.FALL_FLYING;
        return this.aquaAcrobatics$getPose() == Pose.SWIMMING || (IntegrationManager.isWingsEnabled() ? !WingsIntegration.onFlightCheck(this.aquaAcrobatics$getPlayer(), !isFallFlying) : isFallFlying);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean aquaAcrobatics$isVisuallySwimming() {
        return this.aquaAcrobatics$isActuallySwimming() && !this.isInWater();
    }

    @Override
    public float aquaAcrobatics$getSwimAnimation(float partialTicks) {
        return MathHelperNew.lerp(partialTicks, this.aquaAcrobatics$lastSwimAnimation, this.aquaAcrobatics$swimAnimation);
    }

    @Unique
    private void aquaAcrobatics$updateSwimAnimation() {
        this.aquaAcrobatics$lastSwimAnimation = this.aquaAcrobatics$swimAnimation;
        if (this.aquaAcrobatics$isActuallySwimming()) {
            this.aquaAcrobatics$swimAnimation = Math.min(1.0F, this.aquaAcrobatics$swimAnimation + 0.09F);
        } else {
            this.aquaAcrobatics$swimAnimation = Math.max(0.0F, this.aquaAcrobatics$swimAnimation - 0.09F);
        }
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void travel(float strafe, float vertical, float forward, CallbackInfo callbackInfo) {
        double d0 = this.posX;
        double d1 = this.posY;
        double d2 = this.posZ;
        if (this.aquaAcrobatics$isSwimming() && !this.isRiding()) {
            double d3 = this.getLookVec().y;
            double d4 = d3 < -0.2 ? 0.085 : 0.06;
            IBlockState fluidState = this.world.getBlockState(new BlockPos(this.posX, this.posY + 1.0 - 0.1, this.posZ));
            if (d3 <= 0.0 || this.isJumping || fluidState.getBlock() instanceof BlockLiquid || fluidState.getBlock() instanceof IFluidBlock) {
                double d5 = this.motionY;
                this.motionY += (d3 - d5) * d4;
            }
        }

        double d3 = this.motionY;
        float f = this.jumpMovementFactor;
        if (this.capabilities.isFlying && !this.isRiding()) {
            this.jumpMovementFactor = this.capabilities.getFlySpeed() * (float) (this.isSprinting() ? 2 : 1);
        }

        // replaces a section in super method, therefore super is called otherwise
        if (!this.capabilities.isFlying && this.isInWater()) {
            if (this.isServerWorld() || this.canPassengerSteer()) {
                double d8 = this.posY;
                float f5 = this.isSprinting() ? 0.9F : this.getWaterSlowDown();
                float f6 = 0.02F;
                float f7 = (float) EnchantmentHelper.getDepthStriderModifier(this);
                if (f7 > 3.0F) {
                    f7 = 3.0F;
                }

                if (!this.onGround) {
                    f7 *= 0.5F;
                }

                if (f7 > 0.0F) {
                    f5 += (0.54600006F - f5) * f7 / 3.0F;
                    f6 += (this.getAIMoveSpeed() - f6) * f7 / 3.0F;
                }

                this.moveRelative(strafe, vertical, forward, f6);
                this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
                if (this.collidedHorizontally && this.isOnLadder()) {
                    this.motionY = 0.2;
                }

                this.motionX *= f5;
                this.motionY *= 0.8;
                this.motionZ *= f5;
                this.aquaAcrobatics$applyGravity();
                if (this.collidedHorizontally && this.isOffsetPositionInLiquid(this.motionX, this.motionY + 0.6 - this.posY + d8, this.motionZ)) {
                    this.motionY = 0.3;
                }

                this.aquaAcrobatics$updateLimbSwing();
            }
        } else {
            super.travel(strafe, vertical, forward);
        }

        if (this.capabilities.isFlying && !this.isRiding()) {
            this.motionY = d3 * 0.6D;
            this.jumpMovementFactor = f;
            this.fallDistance = 0.0F;
            this.setFlag(7, false);
        }

        this.addMovementStat(this.posX - d0, this.posY - d1, this.posZ - d2);
        callbackInfo.cancel();
    }

    @Unique
    public void aquaAcrobatics$applyGravity() {
        if (!this.hasNoGravity() && !this.isSprinting()) {
            if (this.motionY <= 0.0 && Math.abs(this.motionY - 0.005) >= 0.003 && Math.abs(this.motionY - 0.08 / 16.0) < 0.003) {
                this.motionY = -0.003;
            } else {
                this.motionY -= 0.08 / 16.0;
            }
        }
    }

    @Unique
    private void aquaAcrobatics$updateLimbSwing() {
        this.prevLimbSwingAmount = this.limbSwingAmount;
        double d5 = this.posX - this.prevPosX;
        double d7 = this.posZ - this.prevPosZ;
        double d9 = this instanceof EntityFlying ? this.posY - this.prevPosY : 0.0;
        float f10 = MathHelper.sqrt(d5 * d5 + d9 * d9 + d7 * d7) * 4.0F;

        if (f10 > 1.0F) {
            f10 = 1.0F;
        }

        this.limbSwingAmount += (f10 - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;
    }

    @Shadow
    public abstract void addMovementStat(double p_71000_1_, double p_71000_3_, double p_71000_5_);

    @Inject(method = "onLivingUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isRemote:Z", opcode = Opcodes.GETFIELD), cancellable = true, slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;playShoulderEntityAmbientSound(Lnet/minecraft/nbt/NBTTagCompound;)V")))
    public void onLivingUpdate(CallbackInfo callbackInfo) {
        // disable bobbing view when swimming
        float f = 0.0F;
        if (this.onGround && !this.aquaAcrobatics$getShouldBeDead() && !this.aquaAcrobatics$isSwimming()) {
            f = Math.min(0.1F, MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ));
        }

        this.cameraYaw = this.prevCameraYaw + (f - this.prevCameraYaw) * 0.4F;
        // no longer exists in 1.13+
        this.cameraPitch = 0.0F;

        if (!ConfigHandler.MISCELLANEOUS_CONFIG.sneakingForParrots) {
            return;
        }

        if (!this.world.isRemote && (this.isSneaking() || this.isInWater())) {
            this.spawnShoulderEntities();
        }

        callbackInfo.cancel();
    }

    @Inject(method = "addShoulderEntity", at = @At("HEAD"), cancellable = true)
    public void addShoulderEntity(NBTTagCompound p_192027_1_, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (ConfigHandler.MISCELLANEOUS_CONFIG.sneakingForParrots && this.isSneaking()) {
            callbackInfo.setReturnValue(false);
        }
    }

    @Shadow
    protected abstract void spawnShoulderEntities();

    @Redirect(method = "trySleep", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;setSize(FF)V"))
    public void setSizeTrySleep(EntityPlayer player, float width, float height) {
        this.aquaAcrobatics$setPose(Pose.SLEEPING);
    }

    // removed wakeUpPlayer hook as it's not important and is conflicting with sponge forge (they're using overwrite for that method)
    @Unique
    private EntityPlayer aquaAcrobatics$getPlayer() {
        return (EntityPlayer) (Object) this;
    }
}