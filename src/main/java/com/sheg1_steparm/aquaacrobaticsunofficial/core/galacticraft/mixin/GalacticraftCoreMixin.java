package sheg1_steparm.aquaacrobaticsunofficial.core.galacticraft.mixin;

import sheg1_steparm.aquaacrobaticsunofficial.core.AquaAcrobaticsCore;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GalacticraftCore.class)
public class GalacticraftCoreMixin {
    @Inject(method = "preInit", at = @At("TAIL"), remap = false)
    private void markHeightConflict(FMLPreInitializationEvent event, CallbackInfo ci) {
        AquaAcrobaticsCore.LOGGER.info("Notifying Galacticraft that we are a height-conflicting mod");
        GalacticraftCore.isHeightConflictingModInstalled = true;
    }
}