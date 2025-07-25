package fr.dynamx.addons.immersive.api;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.addons.immersive.common.helpers.ConfigReader;
import fr.dynamx.addons.immersive.common.modules.RadioModule;
import fr.dynamx.addons.immersive.common.network.ImmersiveAddonPacketHandler;
import fr.dynamx.addons.immersive.common.network.packets.SendRadioFreqConfig;
import fr.dynamx.api.events.VehicleEntityEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = ImmersiveAddon.ID, value = {Side.CLIENT, Side.SERVER})
public class RadioEvent {

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void leave(VehicleEntityEvent.EntityDismount event) {
        if(ImmersiveAddon.radioPlayer != null) {
            if(event.getEntity().hasModuleOfType(RadioModule.class)) {
                RadioModule module = event.getEntity().getModuleByType(RadioModule.class);
                module.resetCached();
            }
            ImmersiveAddon.LOGGER.debug("Stopping radio on dismount");
            ImmersiveAddon.radioPlayer.stopRadio();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onPlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        if(ImmersiveAddon.radioPlayer != null) {
            ImmersiveAddon.LOGGER.debug("Stopping radio on player logout");
            ImmersiveAddon.radioPlayer.stopRadio();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        if(ImmersiveAddon.radioPlayer != null) {
            ImmersiveAddon.LOGGER.debug("Stopping radio on server disconnect");
            ImmersiveAddon.radioPlayer.stopRadio();
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        try {
            ImmersiveAddonPacketHandler.getInstance().getNetwork().sendTo(new SendRadioFreqConfig(ConfigReader.getFileContent()), (EntityPlayerMP) event.player);
        } catch (IOException e) {
            ImmersiveAddon.LOGGER.debug("Sent radio config to {}", event.player.getName());
            e.printStackTrace();
        }
    }
}