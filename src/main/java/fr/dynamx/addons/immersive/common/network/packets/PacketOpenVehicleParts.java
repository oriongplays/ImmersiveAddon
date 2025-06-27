package fr.dynamx.addons.immersive.common.network.packets;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.addons.immersive.common.GuiHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketOpenVehicleParts implements IMessage {
    private int entityId;

    public PacketOpenVehicleParts() {}

    public PacketOpenVehicleParts(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
    }

    public static class Handler implements IMessageHandler<PacketOpenVehicleParts, IMessage> {
        @Override
        public IMessage onMessage(PacketOpenVehicleParts message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                player.openGui(ImmersiveAddon.INSTANCE, GuiHandler.VEHICLE_PARTS, player.world, message.entityId, 0, 0);
            });
            return null;
        }
    }
}