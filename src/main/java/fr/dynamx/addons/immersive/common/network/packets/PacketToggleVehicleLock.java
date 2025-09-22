package fr.dynamx.addons.immersive.common.network.packets;

import fr.dynamx.addons.basics.common.event.BasicsAddonEvent;
import fr.dynamx.addons.basics.common.modules.BasicsAddonModule;
import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.addons.immersive.utils.VehicleLockUtils;
import fr.dynamx.common.entities.BaseVehicleEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketToggleVehicleLock implements IMessage {
    private int entityId;

    public PacketToggleVehicleLock() {
    }

    public PacketToggleVehicleLock(int entityId) {
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

    public static class Handler implements IMessageHandler<PacketToggleVehicleLock, IMessage> {
        @Override
        public IMessage onMessage(PacketToggleVehicleLock message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) player.world.getEntityByID(message.entityId);
                if (vehicle == null) {
                    return;
                }

                if (vehicle.getControllingPassenger() != player) {
                    return;
                }

                BasicsAddonModule module = vehicle.getModuleByType(BasicsAddonModule.class);
                if (module == null) {
                    return;
                }

                if (module.hasLinkedKey() && !VehicleLockUtils.hasLinkedKey(player, vehicle)) {
                    ITextComponent error = new TextComponentTranslation("basadd.key.invalid");
                    error.getStyle().setColor(TextFormatting.DARK_RED);
                    player.sendMessage(error);
                    return;
                }

                boolean lock = !module.isLocked();
                BasicsAddonEvent.EventLockVehicle.EnumLockAction action = lock
                        ? BasicsAddonEvent.EventLockVehicle.EnumLockAction.LOCK
                        : BasicsAddonEvent.EventLockVehicle.EnumLockAction.UNLOCK;
                if (MinecraftForge.EVENT_BUS.post(new BasicsAddonEvent.EventLockVehicle(Side.SERVER, vehicle, player, action))) {
                    ImmersiveAddon.LOGGER.debug("Vehicle lock event cancelled for {}", vehicle);
                    return;
                }

                module.setLocked(lock);

                ITextComponent feedback = new TextComponentTranslation(lock ? "basadd.key.locked" : "basadd.key.unlocked");
                feedback.getStyle().setColor(lock ? TextFormatting.DARK_RED : TextFormatting.DARK_GREEN);
                player.sendMessage(feedback);
            });
            return null;
        }
    }
}