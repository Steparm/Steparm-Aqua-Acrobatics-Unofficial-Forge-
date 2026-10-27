package sheg1_steparm.aquaacrobaticsunofficial.core.vanilla.client;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sheg1_steparm.aquaacrobaticsunofficial.config.ConfigHandler;
import sheg1_steparm.aquaacrobaticsunofficial.core.AquaAcrobaticsEarlyLoader;
import sheg1_steparm.aquaacrobaticsunofficial.optifine.OptifineHelper;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Pseudo
@Mixin(targets = {"net/optifine/shaders/BlockAliases"})
public class BlockAliasesBubbleColumnMixin {
    /**
     * @author embeddedt
     * @reason inject the bubble column into the block aliases
     */
    @Inject(method = "loadBlockAliases", at = @At("RETURN"), remap = false)
    private static void injectAABubbleColumn(InputStream in, String path, List<List<?>> listBlockAliases, CallbackInfo ci) {
        if (!ConfigHandler.MISCELLANEOUS_CONFIG.bubbleColumns) {
            return;
        }

        Block targetBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("aquaacrobaticsunofficial", "bubble_column"));

        if (targetBlock == null) {
            AquaAcrobaticsEarlyLoader.LOGGER.error("Bubble column block not found");
            return;
        }

        int targetId = Block.getIdFromBlock(targetBlock);
        int waterId = Block.getIdFromBlock(Blocks.WATER);

        if (waterId >= listBlockAliases.size()) {
            AquaAcrobaticsEarlyLoader.LOGGER.error("Shader does not have block ID for water");
            return;
        }

        List<?> waterMappings = listBlockAliases.get(waterId);
        List<?> bubbleColumnMappings = waterMappings.stream()
                .map(a -> OptifineHelper.rewriteBlockAliasForNewId(targetId, a)).collect(Collectors.toList());

        while (listBlockAliases.size() <= targetId) {
            listBlockAliases.add(null);
        }

        listBlockAliases.set(targetId, bubbleColumnMappings);
    }
}