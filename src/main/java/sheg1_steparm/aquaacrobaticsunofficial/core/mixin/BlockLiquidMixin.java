package sheg1_steparm.aquaacrobaticsunofficial.core.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import sheg1_steparm.aquaacrobaticsunofficial.config.ConfigHandler;

import javax.annotation.Nonnull;

@Mixin(BlockLiquid.class)
public abstract class BlockLiquidMixin extends Block {
    public BlockLiquidMixin(Material materialIn) {
        super(materialIn);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getLightOpacity(@Nonnull IBlockState state) {
        if (ConfigHandler.BLOCKS_CONFIG.brighterWater && state.getMaterial() == Material.WATER) {
            return 1;
        } else {
            return super.getLightOpacity(state);
        }
    }
}