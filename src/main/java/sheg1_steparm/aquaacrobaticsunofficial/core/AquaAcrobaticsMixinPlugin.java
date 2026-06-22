package sheg1_steparm.aquaacrobaticsunofficial.core;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class AquaAcrobaticsMixinPlugin implements IMixinConfigPlugin {
    private boolean doesClassExist(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (AquaAcrobaticsEarlyLoader.disableBlockUpdateMixins) {
            if (mixinClassName.contains("BlockSoulSandMixin")
                    || mixinClassName.contains("BlockMagmaMixin")) {

                AquaAcrobaticsEarlyLoader.LOGGER.info("Disabled mixin: {}", mixinClassName);
                return false;
            }
        }

        if (mixinClassName.contains("BlockAliasesBubbleColumnMixin")) {
            return doesClassExist("optifine.OptiFineForgeTweaker");
        }

        return true;
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}