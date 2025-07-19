package fr.dynamx.addons.immersive.common.network.packets;

import fr.dynamx.addons.immersive.common.modules.RadioModule;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Synchronizes radio state from the client controlling the vehicle to the server.
 */
public class PacketUpdateRadioState implements IMessage {
    private int entityId;
    private int index;
    private boolean on;

    public PacketUpdateRadioState() {}

    public PacketUpdateRadioState(int entityId, int index, boolean on) {
        this.entityId = entityId;
        this.index = index;
        this.on = on;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
        index = buf.readInt();
        on = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(index);
        buf.writeBoolean(on);
    }

    public static class Handler implements IMessageHandler<PacketUpdateRadioState, IMessage> {
        @Override
        public IMessage onMessage(PacketUpdateRadioState message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                World world = player.world;
                if (world.getEntityByID(message.entityId) instanceof fr.dynamx.common.entities.BaseVehicleEntity) {
                    fr.dynamx.common.entities.BaseVehicleEntity<?> veh =
                            (fr.dynamx.common.entities.BaseVehicleEntity<?>) world.getEntityByID(message.entityId);
                    RadioModule module = veh.getModuleByType(RadioModule.class);
                    if (module != null) {
                        module.setCurrentRadioIndex(message.index);
                        module.setRadioOn(message.on);
                    }
                }
            });
            return null;
        }
    }
}