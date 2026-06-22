package sheg1_steparm.aquaacrobaticsunofficial.core.vanilla;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import sheg1_steparm.aquaacrobaticsunofficial.block.BlockBubbleColumn;

import javax.annotation.Nonnull;
import java.util.Random;

@Mixin(BlockSoulSand.class)
public abstract class BlockSoulSandMixin extends Block {
    public BlockSoulSandMixin(Material p_i45394_1_) {
        super(p_i45394_1_);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(@Nonnull IBlockState state, World worldIn, @Nonnull BlockPos thisPos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
        worldIn.scheduleUpdate(thisPos, this, this.tickRate(worldIn));
    }

    @Override
    public void onBlockAdded(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
    }

    @Override
    public int tickRate(@Nonnull World worldIn) {
        return 20;
    }

    @Override
    public void updateTick(@Nonnull World worldIn, BlockPos pos, @Nonnull IBlockState state, @Nonnull Random rand) {
        BlockBubbleColumn.placeBubbleColumn(worldIn, pos.up(), true);
    }
}