package fr.dynamx.addons.immersive.common.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public class PacketVehicleAssetsResponse implements IMessage {
    private String message;

    public PacketVehicleAssetsResponse() {}

    public PacketVehicleAssetsResponse(String message) {
        this.message = message;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        message = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, message);
    }

    public static class Handler implements IMessageHandler<PacketVehicleAssetsResponse, IMessage> {
        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(PacketVehicleAssetsResponse message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                if(Minecraft.getMinecraft().player != null) {
                    Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentString(message.message), true);
                }
            });
            return null;
        }
    }
}