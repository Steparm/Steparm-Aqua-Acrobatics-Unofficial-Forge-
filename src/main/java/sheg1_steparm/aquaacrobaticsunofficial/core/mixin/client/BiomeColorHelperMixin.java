package sheg1_steparm.aquaacrobaticsunofficial.core.mixin.client;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sheg1_steparm.aquaacrobaticsunofficial.biome.BiomeWaterFogColors;
import sheg1_steparm.aquaacrobaticsunofficial.config.ConfigHandler;

@Pseudo
@Mixin(targets = {"net/minecraft/world/biome/BiomeColorHelper$3"})
public class BiomeColorHelperMixin {
    /**
     * Typically we would just use the GetWaterColor event... but mods like Thaumcraft don't call it
     * and try to force their own water color on us.
     */
    @Inject(method = "func_180283_a", at = @At("RETURN"), cancellable = true, remap = false)
    @Dynamic("Exists only in an SRG environment")
    private void getNewWaterColorMultiplier(Biome biome, BlockPos position, CallbackInfoReturnable<Integer> cir) {
        if (ConfigHandler.BLOCKS_CONFIG.newWaterColors) {
            cir.setReturnValue(BiomeWaterFogColors.getWaterColorForBiome(biome, cir.getReturnValue()));
        }
    }
}
