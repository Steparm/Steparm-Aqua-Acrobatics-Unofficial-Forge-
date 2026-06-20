package sheg1_steparm.aquaacrobaticsunofficial.core.galacticraft.mixin;

import micdoodle8.mods.galacticraft.core.entities.player.GCEntityClientPlayerMP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sheg1_steparm.aquaacrobaticsunofficial.entity.player.IPlayerResizeable;

@Mixin(GCEntityClientPlayerMP.class)
public abstract class GCEntityClientPlayerMPMixin extends EntityPlayerSP {
    public GCEntityClientPlayerMPMixin(Minecraft mcIn, World worldIn, NetHandlerPlayClient netHandler, StatisticsManager statFileWriter, RecipeBook book) {
        super(mcIn, worldIn, netHandler, statFileWriter, book);
    }

    @Inject(method = "getEyeHeight", at = @At("HEAD"), cancellable = true)
    private void getEyeHeight(CallbackInfoReturnable<Float> cir) {
        if (this.isPlayerSleeping()) {
            cir.setReturnValue(0.2F);
            return;
        }

        IPlayerResizeable player = (IPlayerResizeable) this;

        cir.setReturnValue(player.aquaAcrobatics$getStandingEyeHeight(player.aquaAcrobatics$getPose(), player.aquaAcrobatics$getSize(player.aquaAcrobatics$getPose())));
    }
}