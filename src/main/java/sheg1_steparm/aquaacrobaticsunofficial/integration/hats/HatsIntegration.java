package sheg1_steparm.aquaacrobaticsunofficial.integration.hats;

import me.ichun.mods.hats.client.render.helper.HelperPlayer;
import me.ichun.mods.hats.common.core.ApiHandler;
import net.minecraft.entity.EntityLivingBase;
import sheg1_steparm.aquaacrobaticsunofficial.entity.player.IPlayerResizeable;

public class HatsIntegration {
    public static void register() {
        ApiHandler.registerHelper(new HelperPlayer() {
            @Override
            public float getHatScale(EntityLivingBase entityIn) {
                if (entityIn instanceof IPlayerResizeable) {
                    if (((IPlayerResizeable) entityIn).aquaAcrobatics$getSwimAnimation(1.0F) > 0.0F) {
                        return 0.0F;
                    }
                }
                return super.getHatScale(entityIn);
            }
        });
    }
}