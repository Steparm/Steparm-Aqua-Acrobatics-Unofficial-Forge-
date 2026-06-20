package sheg1_steparm.aquaacrobaticsunofficial.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import sheg1_steparm.aquaacrobaticsunofficial.network.message.PacketSendKey;

public class NetworkHandler {
    public static SimpleNetworkWrapper INSTANCE = null;
    private static int packetId = 0;

    public NetworkHandler() {
    }

    public static int nextID() {
        return packetId++;
    }

    public static void registerMessages(String channelName) {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(channelName);
        registerMessages();
    }

    public static void registerMessages() {
        INSTANCE.registerMessage(PacketSendKey.Handler.class, PacketSendKey.class, nextID(), Side.SERVER);
    }
}