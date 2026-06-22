package sheg1_steparm.aquaacrobaticsunofficial.core;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
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
            ModContainer jm = FMLCommonHandler.instance().findContainerFor("journeymap");

            if (jm != null) {
                String v = jm.getVersion();

                if ("1.12.2-5.5.4".equals(v)) {
                    configs.add("mixins.aquaacrobaticsunofficial.journeymap55.json");
                } else if ("1.12.2-5.7.1".equals(v)) {
                    configs.add("mixins.aquaacrobaticsunofficial.journeymap57.json");
                } else {
                    AquaAcrobaticsEarlyLoader.LOGGER.warn(
                            "Unknown JourneyMap version: {}, compatibility not guaranteed", v
                    );
                }
            }
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