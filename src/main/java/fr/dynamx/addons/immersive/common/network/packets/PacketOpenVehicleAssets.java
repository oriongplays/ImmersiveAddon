package fr.dynamx.addons.immersive.common.network.packets;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.addons.immersive.common.GuiHandler;
import fr.dynamx.addons.immersive.common.network.ImmersiveAddonPacketHandler;
import fr.dynamx.common.entities.BaseVehicleEntity;
import fr.dynamx.addons.immersive.common.network.packets.PacketVehicleAssetsResponse;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
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
                ImmersiveAddon.LOGGER.debug("Received vehicle assets request from {} for entity {}", player.getName(), message.entityId);
                BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) player.world.getEntityByID(message.entityId);
                if(vehicle == null)
                    return;
                fr.dynamx.addons.immersive.common.modules.VehicleAssetsModule module = vehicle.getModuleByType(fr.dynamx.addons.immersive.common.modules.VehicleAssetsModule.class);
                if(module == null) {
                    ImmersiveAddonPacketHandler.getInstance().getNetwork().sendTo(new PacketVehicleAssetsResponse("Vehicle lacks assets module"), player);
                    return;
                }
                String reason = GuiHandler.checkAssetAccess(vehicle, player);
                if(reason == null) {
                    player.openGui(ImmersiveAddon.INSTANCE, GuiHandler.VEHICLE_ASSETS, player.world, message.entityId, 0, 0);
                } else {
                    ImmersiveAddonPacketHandler.getInstance().getNetwork().sendTo(new PacketVehicleAssetsResponse(reason), player);
                }
            });
            return null;
        }
    }
}