package sheg1_steparm.aquaacrobaticsunofficial.core;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.asm.mixin.Mixins;
import zone.rong.mixinbooter.MixinLoader;

@MixinLoader
public class ModCompatMixinHandler {
    public ModCompatMixinHandler() {
        AquaAcrobaticsCore.LOGGER.info("Aqua Acrobatics Unofficial is loading mod compatibility mixins");
        if(Loader.isModLoaded("galacticraftcore")) {
            Mixins.addConfiguration("META-INF/mixins.aquaacrobaticsunofficial.galacticraft.json");
        }
        if(Loader.isModLoaded("journeymap")) {
            ModContainer jmMod = FMLCommonHandler.instance().findContainerFor("journeymap");
            if(jmMod != null) {
                String version = jmMod.getVersion();
                if(version.equals("1.12.2-5.5.4")) {
                    Mixins.addConfiguration("META-INF/mixins.aquaacrobaticsunofficial.journeymap55.json");
                } else if(version.equals("1.12.2-5.7.1")) {
                    Mixins.addConfiguration("META-INF/mixins.aquaacrobaticsunofficial.journeymap57.json");
                } else {
                    AquaAcrobaticsCore.LOGGER.warn("You have JourneyMap " + version + " installed. Only 1.12.2-5.5.4 and 1.12.2-5.7.1 are patched for water color compatibility.");
                }
            }
        }
        if(Loader.isModLoaded("xaerominimap")) {
            Mixins.addConfiguration("META-INF/mixins.aquaacrobaticsunofficial.xaerosminimap.json");
        }
        if(Loader.isModLoaded("thaumcraft")) {
            Mixins.addConfiguration("META-INF/mixins.aquaacrobaticsunofficial.thaumcraft.json");
        }
        AquaAcrobaticsCore.isModCompatLoaded = true;
    }
}