package sheg1_steparm.aquaacrobaticsunofficial.core.galacticraft;

import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sheg1_steparm.aquaacrobaticsunofficial.core.AquaAcrobaticsEarlyLoader;

@Mixin(GalacticraftCore.class)
public class GalacticraftCoreMixin {
    @Inject(method = "preInit", at = @At("TAIL"), remap = false)
    private void markHeightConflict(FMLPreInitializationEvent event, CallbackInfo ci) {
        AquaAcrobaticsEarlyLoader.LOGGER.info("Notifying Galacticraft that we are a height-conflicting mod");
        GalacticraftCore.isHeightConflictingModInstalled = true;
    }
}