package sheg1_steparm.aquaacrobaticsunofficial.core;

import net.minecraftforge.fml.common.Loader;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.ArrayList;
import java.util.List;

public class AquaAcrobaticsLateLoader implements ILateMixinLoader {
    public static volatile boolean isModCompatLoaded = false;

    @Override
    public List<String> getMixinConfigs() {

        List<String> configs = new ArrayList<>();

        if (Loader.isModLoaded("galacticraftcore")) {
            configs.add("mixins.aquaacrobaticsunofficial.galacticraft.json");
        }

        if (Loader.isModLoaded("journeymap")) {
            configs.add("mixins.aquaacrobaticsunofficial.journeymap.json");
        }

        if (Loader.isModLoaded("xaerominimap")) {
            configs.add("mixins.aquaacrobaticsunofficial.xaerosminimap.json");
        }

        if (Loader.isModLoaded("thaumcraft")) {
            configs.add("mixins.aquaacrobaticsunofficial.thaumcraft.json");
        }

        isModCompatLoaded = true;

        return configs;
    }
}