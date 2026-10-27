package sheg1_steparm.aquaacrobaticsunofficial.handler;

import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sheg1_steparm.aquaacrobaticsunofficial.block.BlockBubbleColumn;
import sheg1_steparm.aquaacrobaticsunofficial.config.ConfigHandler;
import sheg1_steparm.aquaacrobaticsunofficial.entity.IRockableBoat;

public class CommonHandler {
    @SubscribeEvent
    public void onEntityConstructing(EntityEvent.EntityConstructing event) {
        if (event.getEntity() instanceof EntityBoat) {
            if (ConfigHandler.MISCELLANEOUS_CONFIG.bubbleColumns) {
                ((IRockableBoat) event.getEntity()).aquaAcrobatics$doRegisterData();
            }
        }
    }

    @SubscribeEvent
    public void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (!ConfigHandler.MISCELLANEOUS_CONFIG.bubbleColumns) {
            return;
        }
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        if (world.getBlockState(pos).getBlock() == Blocks.MAGMA) {
            BlockPos above = pos.up();
            if (world.getBlockState(above).getMaterial() == Material.WATER) {
                BlockBubbleColumn.placeBubbleColumn(world, above, false);
            }
        } else if (world.getBlockState(pos).getBlock() == Blocks.SOUL_SAND) {
            BlockPos above = pos.up();
            if (world.getBlockState(above).getMaterial() == Material.WATER) {
                BlockBubbleColumn.placeBubbleColumn(world, above, true);
            }
        } else if (world.getBlockState(pos).getMaterial() == Material.WATER) {
            BlockPos below = pos.down();
            if (world.getBlockState(below).getBlock() == Blocks.MAGMA) {
                BlockBubbleColumn.placeBubbleColumn(world, pos, false);
            } else if (world.getBlockState(below).getBlock() == Blocks.SOUL_SAND) {
                BlockBubbleColumn.placeBubbleColumn(world, pos, true);
            }
        }
    }
}