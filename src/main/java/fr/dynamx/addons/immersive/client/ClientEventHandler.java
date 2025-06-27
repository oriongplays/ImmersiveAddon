package fr.dynamx.addons.immersive.client;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.addons.immersive.ImmersiveAddonConfig;
import fr.dynamx.common.entities.BaseVehicleEntity;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import fr.dynamx.addons.immersive.client.KeyVehicleInventory;
import fr.dynamx.addons.immersive.common.modules.VehicleStorageModule;
import fr.dynamx.addons.immersive.common.network.ImmersiveAddonPacketHandler;
import fr.dynamx.addons.immersive.common.network.packets.PacketOpenVehicleStorage;
import fr.dynamx.addons.immersive.common.modules.DamageModule;
import fr.dynamx.api.events.VehicleEntityEvent;
import fr.dynamx.client.handlers.hud.CarController;
import fr.dynamx.client.handlers.hud.HelicopterController;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.concurrent.TimeUnit;


@Mod.EventBusSubscriber(modid = ImmersiveAddon.ID, value = {Side.CLIENT})
public class ClientEventHandler {

    Minecraft mc = Minecraft.getMinecraft();

    public static boolean showNames = false;

    private static long lastJump = 0;
    
        @SubscribeEvent
    public void handleInventoryKey(TickEvent.ClientTickEvent event) {
        if(event.phase != TickEvent.Phase.END)
            return;
        if(mc.player != null && KeyVehicleInventory.OPEN_INVENTORY != null && KeyVehicleInventory.OPEN_INVENTORY.isPressed()) {
            if(mc.player.getRidingEntity() instanceof BaseVehicleEntity) {
                BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) mc.player.getRidingEntity();
                if(vehicle.hasModuleOfType(fr.dynamx.common.entities.modules.SeatsModule.class)) {
                    fr.dynamx.common.entities.modules.SeatsModule seats = vehicle.getModuleByType(fr.dynamx.common.entities.modules.SeatsModule.class);
                    if(seats.getControllingPassenger() == mc.player) {
                        VehicleStorageModule storage = vehicle.getModuleByType(VehicleStorageModule.class);
                        if(storage != null) {
                            ImmersiveAddonPacketHandler.getInstance().getNetwork()
                                    .sendToServer(new PacketOpenVehicleStorage(vehicle.getEntityId()));
                        }
                    }
                }
            }
        }
    }



    // The GUI is opened server‑side via PacketOpenVehicleStorage. Opening it
    // directly on the client caused desynchronization with the container,
    // preventing inventory changes from being saved. The following handler was
    // therefore removed.

    @SubscribeEvent
    public void updateVehicleController(VehicleEntityEvent.ControllerUpdate event) {
        if(event.getEntity().hasModuleOfType(DamageModule.class)) {
            DamageModule module = event.getEntity().getModuleByType(DamageModule.class);

            if (event.getController() instanceof HelicopterController) {
                float percentDamage = 100 - module.getDamage();
                if (percentDamage < 5) {
                    ((HelicopterController) event.getController()).setEngineStarted(false);
                }
            } else if (event.getController() instanceof CarController){
                float percentDamage = 100 - module.getDamage();
                if (percentDamage <= 15) {
                    ((CarController) event.getController()).setEngineStarted(false);
                }
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void blockOutlineEvent(DrawBlockHighlightEvent event) {
        if (!event.getPlayer().capabilities.isCreativeMode && !ImmersiveAddonConfig.enableBlockOutline) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerJump(LivingEvent.LivingJumpEvent e) {
        if (e.getEntity().world.isRemote && ImmersiveAddonConfig.enableBunnyHop) {
            if (e.getEntity() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) e.getEntity();
                if (player.isCreative() || player.isSpectator()) {
                    return;
                }
                long timeBeforeJump = lastJump - System.currentTimeMillis();
                if (timeBeforeJump <= 0) {
                    if (mc.gameSettings.keyBindSprint.isKeyDown() || player.isSprinting()) {
                        lastJump = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3);
                    } else {
                        lastJump = System.currentTimeMillis() + 650;
                    }
                } else {
                    mc.player.motionY = 0f;
                    mc.player.moveStrafing = 0.0F;
                    mc.player.moveForward = 0.0F;
                    mc.player.randomYawVelocity = 0.0F;
                }
            }
        }
    }


    @SubscribeEvent
    public void renderPlayerNameTag(RenderLivingEvent.Specials.Pre event) {
        if(ImmersiveAddonConfig.enableHideName) {
            if (!mc.player.isCreative() && !mc.player.isSpectator() && !showNames) {
                event.setCanceled(true);
            }
        }
    }
}