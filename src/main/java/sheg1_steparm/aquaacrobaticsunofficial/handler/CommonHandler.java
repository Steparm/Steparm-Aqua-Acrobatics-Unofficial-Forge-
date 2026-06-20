package sheg1_steparm.aquaacrobaticsunofficial.handler;

import net.minecraft.entity.item.EntityBoat;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
}