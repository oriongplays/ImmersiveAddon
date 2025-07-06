package fr.dynamx.addons.immersive.client;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.addons.immersive.ImmersiveAddonConfig;
import fr.dynamx.common.entities.BaseVehicleEntity;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import fr.dynamx.addons.immersive.common.modules.VehicleCustomizationModule;
import fr.dynamx.addons.immersive.common.network.ImmersiveAddonPacketHandler;
import fr.dynamx.addons.immersive.common.network.packets.PacketOpenVehicleParts;
import fr.dynamx.addons.immersive.common.items.ItemsRegister;
import fr.dynamx.addons.immersive.client.KeyVehicleInventory;
import fr.dynamx.addons.immersive.client.KeyRadio;
import fr.dynamx.addons.immersive.client.GuiRadio;
import fr.dynamx.addons.immersive.common.modules.DamageModule;
import fr.dynamx.addons.immersive.common.modules.RadioModule;
import fr.dynamx.addons.immersive.client.VehicleDynamicLight;
import fr.dynamx.addons.basics.common.modules.BasicsAddonModule;
import atomicstryker.dynamiclights.client.DynamicLights;
import net.minecraftforge.fml.common.Loader;
import java.util.HashMap;
import java.util.Map;
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
import net.minecraft.world.GameType;

import java.util.concurrent.TimeUnit;


@Mod.EventBusSubscriber(modid = ImmersiveAddon.ID, value = {Side.CLIENT})
public class ClientEventHandler {

    Minecraft mc = Minecraft.getMinecraft();

    public static boolean showNames = false;

    private static long lastJump = 0;

        /**
     * Active dynamic light sources for vehicles on the client.
     */
    private final Map<BaseVehicleEntity<?>, VehicleDynamicLight> vehicleLights = new HashMap<>();

    @SubscribeEvent
    public void handleInventoryKey(TickEvent.ClientTickEvent event) {
        if(event.phase != TickEvent.Phase.END)
            return;
    if(mc.player != null && KeyVehicleInventory.OPEN_INVENTORY != null && KeyVehicleInventory.OPEN_INVENTORY.isPressed()) {
        if (mc.playerController.getCurrentGameType() != GameType.ADVENTURE)
            return;
        if(mc.player.getHeldItemMainhand().getItem() != ItemsRegister.SCANNER &&
           mc.player.getHeldItemOffhand().getItem() != ItemsRegister.SCANNER)
            return;

        if(mc.player.getRidingEntity() instanceof BaseVehicleEntity) {
            BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) mc.player.getRidingEntity();
            fr.dynamx.common.entities.modules.SeatsModule seats = vehicle.getModuleByType(fr.dynamx.common.entities.modules.SeatsModule.class);
            if(seats != null && seats.getControllingPassenger() == mc.player) {
                VehicleCustomizationModule module = vehicle.getModuleByType(VehicleCustomizationModule.class);
                if(module != null) {
                    ImmersiveAddonPacketHandler.getInstance().getNetwork()
                            .sendToServer(new PacketOpenVehicleParts(vehicle.getEntityId()));
                }
                }
            }
        }
    }

    @SubscribeEvent
    public void handleRadioGuiKey(TickEvent.ClientTickEvent event) {
        if(event.phase != TickEvent.Phase.END)
            return;
        if(mc.player != null && KeyRadio.OPEN_RADIO != null && KeyRadio.OPEN_RADIO.isPressed()) {
            if(mc.player.getRidingEntity() instanceof BaseVehicleEntity) {
                BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) mc.player.getRidingEntity();
                fr.dynamx.common.entities.modules.SeatsModule seats = vehicle.getModuleByType(fr.dynamx.common.entities.modules.SeatsModule.class);
                if(seats != null && seats.getControllingPassenger() == mc.player) {
                    RadioModule module = vehicle.getModuleByType(RadioModule.class);
                    if(module != null) {
                        mc.displayGuiScreen(new GuiRadio(module));
                    }
                }
            }
        }
    }



    @SubscribeEvent
    public void updateVehicleLights(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        if (mc.world == null)
            return;

        if (!Loader.isModLoaded("dynamiclights"))
            return;

        mc.world.loadedEntityList.stream()
                .filter(e -> e instanceof BaseVehicleEntity)
                .map(e -> (BaseVehicleEntity<?>) e)
                .forEach(vehicle -> {
                    BasicsAddonModule module = vehicle.getModuleByType(BasicsAddonModule.class);
                    if (module == null)
                        return;

                    boolean active = module.isHeadLightsOn() || module.isBeaconsOn();
                    VehicleDynamicLight light = vehicleLights.get(vehicle);

                    if (active) {
                        if (light == null) {
                            light = new VehicleDynamicLight(vehicle, module);
                            vehicleLights.put(vehicle, light);
                            DynamicLights.addLightSource(light);
                        }
                    } else if (light != null) {
                        DynamicLights.removeLightSource(light);
                        vehicleLights.remove(vehicle);
                    }
                });

        // Clean up lights from dead vehicles
        vehicleLights.entrySet().removeIf(entry -> {
            if (entry.getKey().isDead) {
                DynamicLights.removeLightSource(entry.getValue());
                return true;
            }
            return false;
        });
    }





    // The GUI is opened server‑side via PacketOpenVehicleParts. Opening it
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