package fr.dynamx.addons.immersive.common.network.packets;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.addons.immersive.common.GuiHandler;
import fr.dynamx.common.entities.BaseVehicleEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.GameType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketOpenVehicleAssets implements IMessage {
    private int entityId;

    public PacketOpenVehicleAssets() {}

    public PacketOpenVehicleAssets(int entityId) {
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

    public static class Handler implements IMessageHandler<PacketOpenVehicleAssets, IMessage> {
        @Override
        public IMessage onMessage(PacketOpenVehicleAssets message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if(player.interactionManager.getGameType() == GameType.ADVENTURE &&
                   player.world.getEntityByID(message.entityId) instanceof BaseVehicleEntity) {
                    BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) player.world.getEntityByID(message.entityId);
                    NBTTagCompound tag = vehicle.getEntityData();
                    if(tag.hasKey("garage", Constants.NBT.TAG_COMPOUND)) {
                        NBTTagCompound garage = tag.getCompoundTag("garage");
                        String owner = garage.getString("owner");
                        if(player.getUniqueID().toString().equals(owner)) {
                            player.openGui(ImmersiveAddon.INSTANCE, GuiHandler.VEHICLE_ASSETS, player.world, message.entityId, 0, 0);
                        }
                    }
                }
            });
            return null;
        }
    }
}