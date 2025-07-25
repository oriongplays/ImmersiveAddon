package fr.dynamx.addons.immersive.common.network;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.addons.immersive.common.network.packets.PacketOpenVehicleParts;
import fr.dynamx.addons.immersive.common.network.packets.PacketOpenVehicleStorage;
import fr.dynamx.addons.immersive.common.network.packets.PacketShowNames;
import fr.dynamx.addons.immersive.common.network.packets.SendRadioFreqConfig;
import fr.dynamx.addons.immersive.common.network.packets.PacketUpdateRadioState;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ImmersiveAddonPacketHandler {
    private static ImmersiveAddonPacketHandler instance;
    private SimpleNetworkWrapper network;
    private int nextID = 0;

    public static ImmersiveAddonPacketHandler getInstance() {
        if (instance == null) instance = new ImmersiveAddonPacketHandler();
        return instance;
    }
    public SimpleNetworkWrapper getNetwork() {
        return network;
    }

    public void registerPackets() {
        this.network = NetworkRegistry.INSTANCE.newSimpleChannel(ImmersiveAddon.ID.toUpperCase());

        this.registerPacket(SendRadioFreqConfig.Handler.class, SendRadioFreqConfig.class, Side.CLIENT);
        this.registerPacket(PacketShowNames.Handler.class, PacketShowNames.class, Side.CLIENT);
        this.registerPacket(PacketOpenVehicleParts.Handler.class, PacketOpenVehicleParts.class, Side.SERVER);
        this.registerPacket(PacketOpenVehicleStorage.Handler.class, PacketOpenVehicleStorage.class, Side.SERVER);
        this.registerPacket(PacketUpdateRadioState.Handler.class, PacketUpdateRadioState.class, Side.SERVER);
    }
    private <REQ extends IMessage, REPLY extends IMessage> void registerPacket(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, Side side) {
        network.registerMessage(messageHandler, requestMessageType, nextID, side);
        nextID++;
    }
}
