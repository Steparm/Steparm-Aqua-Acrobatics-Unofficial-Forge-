package sheg1_steparm.aquaacrobaticsunofficial.core.vanilla;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import sheg1_steparm.aquaacrobaticsunofficial.config.ConfigHandler;

@Mixin(NetHandlerPlayServer.class)
public abstract class NetHandlerPlayServerMixin {
    @Redirect(method = "processEntityAction", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;motionY:D", opcode = Opcodes.GETFIELD))
    public double getElytraFlyingMotion(EntityPlayerMP player) {
        // 1.15 change for easier elytra takeoff
        return ConfigHandler.MOVEMENT_CONFIG.easyElytraTakeoff ? -1.0 : player.motionY;
    }
}