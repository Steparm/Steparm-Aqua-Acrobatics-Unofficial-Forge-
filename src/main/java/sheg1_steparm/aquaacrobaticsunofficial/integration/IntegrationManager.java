package sheg1_steparm.aquaacrobaticsunofficial.integration;

import net.minecraftforge.fml.common.Loader;
import sheg1_steparm.aquaacrobaticsunofficial.config.ConfigHandler;

import java.util.LinkedList;
import java.util.List;

public class IntegrationManager {
    public static List<IElytraOpenHook> elytraOpenHooks = new LinkedList<>();
    private static boolean isBetweenlandsLoaded;
    private static boolean isChiseledMeLoaded;
    private static boolean isEnderIoLoaded;
    private static boolean isRandomPatchesLoaded;
    private static boolean isMoBendsLoaded;
    private static boolean isWingsLoaded;
    private static boolean isArtemisLibLoaded;
    private static boolean isMorphLoaded;
    private static boolean isHatsLoaded;
    private static boolean isThaumicAugmentationLoaded;
    private static boolean isTrinketsAndBaublesLoaded;
    private static boolean isWitcheryResurrectedLoaded;
    private static boolean isAE2Loaded;

    public static void loadCompat() {
        isAE2Loaded = Loader.isModLoaded("appliedenergistics2");
        isBetweenlandsLoaded = Loader.isModLoaded("thebetweenlands");
        isChiseledMeLoaded = Loader.isModLoaded("chiseled_me");
        isEnderIoLoaded = Loader.isModLoaded("enderio");
        isRandomPatchesLoaded = Loader.isModLoaded("randompatches");
        isMoBendsLoaded = Loader.isModLoaded("mobends");
        isWingsLoaded = Loader.isModLoaded("wings");
        isArtemisLibLoaded = Loader.isModLoaded("artemislib");
        isMorphLoaded = Loader.isModLoaded("morph");
        isHatsLoaded = Loader.isModLoaded("hats");
        isThaumicAugmentationLoaded = Loader.isModLoaded("thaumicaugmentation");
        isTrinketsAndBaublesLoaded = Loader.isModLoaded("xat");
        isWitcheryResurrectedLoaded = Loader.isModLoaded("witchery");
    }

    public static boolean isAE2Enabled() {
        return isAE2Loaded && ConfigHandler.INTEGRATION_CONFIG.ae2Integration;
    }

    public static boolean isBetweenlandsEnabled() {
        return isBetweenlandsLoaded && ConfigHandler.INTEGRATION_CONFIG.betweenlandsIntegration;
    }

    public static boolean isChiseledMeEnabled() {
        return isChiseledMeLoaded && ConfigHandler.INTEGRATION_CONFIG.chiseledMeIntegration;
    }

    public static boolean isEnderIoEnabled() {
        return isEnderIoLoaded && ConfigHandler.INTEGRATION_CONFIG.enderIoIntegration;
    }

    public static boolean isRandomPatchesEnabled() {
        return isRandomPatchesLoaded && ConfigHandler.INTEGRATION_CONFIG.randomPatchesIntegration;
    }

    public static boolean isMoBendsEnabled() {
        return isMoBendsLoaded && ConfigHandler.INTEGRATION_CONFIG.moBendsIntegration;
    }

    public static boolean isWingsEnabled() {
        return isWingsLoaded && ConfigHandler.INTEGRATION_CONFIG.wingsIntegration;
    }

    public static boolean isArtemisLibEnabled() {
        return isArtemisLibLoaded && ConfigHandler.INTEGRATION_CONFIG.artemisLibIntegration;
    }

    public static boolean isMorphEnabled() {
        return isMorphLoaded && ConfigHandler.INTEGRATION_CONFIG.morphIntegration;
    }

    public static boolean isHatsEnabled() {
        return isHatsLoaded && ConfigHandler.INTEGRATION_CONFIG.hatsIntegration;
    }

    public static boolean isThaumicAugmentationEnabled() {
        return isThaumicAugmentationLoaded && ConfigHandler.INTEGRATION_CONFIG.thaumicAugmentationIntegration;
    }

    public static boolean isTrinketsAndBaublesEnabled() {
        return isTrinketsAndBaublesLoaded && ConfigHandler.INTEGRATION_CONFIG.trinketsAndBaublesIntegration;
    }

    public static boolean isWitcheryResurrectedEnabled() {
        return isWitcheryResurrectedLoaded && ConfigHandler.INTEGRATION_CONFIG.witcheryResurrectedIntegration;
    }
}
