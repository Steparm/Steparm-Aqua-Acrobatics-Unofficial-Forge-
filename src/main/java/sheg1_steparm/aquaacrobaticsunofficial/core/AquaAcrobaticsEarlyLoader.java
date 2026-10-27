package sheg1_steparm.aquaacrobaticsunofficial.core;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sheg1_steparm.aquaacrobaticsunofficial.Tags;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.Name("AquaAcrobatics")
@IFMLLoadingPlugin.MCVersion("1.12.2")
public class AquaAcrobaticsEarlyLoader implements IFMLLoadingPlugin, IEarlyMixinLoader {
    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    public static boolean disableBlockUpdateMixins;

    public AquaAcrobaticsEarlyLoader() {
        Configuration config = new Configuration(new File("config", "aquaacrobatics_core.cfg"));
        config.load();

        disableBlockUpdateMixins = config.getBoolean(
                "DisableBlockUpdateMixins",
                "hacks",
                false,
                "Disable problematic ASM mixins"
        );

        config.save();

        LOGGER.info("AquaAcrobatics config loaded. disableBlockUpdateMixins={}", disableBlockUpdateMixins);
    }

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins.aquaacrobaticsunofficial.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}