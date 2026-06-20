package sheg1_steparm.aquaacrobaticsunofficial.integration.trinketsandbaubles;

import net.minecraft.entity.player.EntityPlayer;
import xzeroair.trinkets.capabilities.Capabilities;
import xzeroair.trinkets.capabilities.race.EntityProperties;

@SuppressWarnings("deprecation")
public class TrinketsAndBaublesIntegration {
    public static float getResizeFactor(EntityPlayer player) {
        EntityProperties props = Capabilities.getEntityRace(player);
        if (props != null) {
            return (float) props.getSize() / 100f;
        } else {
            return 1f;
        }
    }
}
