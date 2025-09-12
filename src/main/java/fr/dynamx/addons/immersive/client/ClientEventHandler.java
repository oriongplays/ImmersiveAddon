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
import fr.dynamx.addons.immersive.client.KeyVehicleHealth;
import fr.dynamx.addons.immersive.client.KeyWheelHealth;
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
import fr.dynamx.utils.DynamXUtils;
import fr.dynamx.utils.physics.PhysicsRaycastResult;
import fr.dynamx.api.physics.BulletShapeType;
import fr.dynamx.api.physics.EnumBulletShapeType;
import fr.dynamx.addons.immersive.common.modules.WheelHealthModule;
import fr.dynamx.common.entities.modules.WheelsModule;
import fr.dynamx.common.physics.entities.modules.WheelsPhysicsHandler;
import fr.dynamx.common.physics.entities.parts.wheel.WheelPhysics;
import com.jme3.math.Vector3f;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.world.GameType;
import net.minecraft.util.SoundCategory;

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
    public void handleHealthKey(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        if (mc.player != null) {
            if (KeyVehicleHealth.SHOW_HEALTH != null && KeyVehicleHealth.SHOW_HEALTH.isPressed()) {
                displayVehicleHealth(mc.player);
            }
            if (KeyWheelHealth.SHOW_WHEEL_HEALTH != null && KeyWheelHealth.SHOW_WHEEL_HEALTH.isPressed()) {
                displayWheelHealth(mc.player);
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

    /**
     * Update radio playback each tick so the stream stops when leaving the
     * vehicle even if it unloads.
     */
    @SubscribeEvent
    public void tickRadio(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            RadioModule.tickActive();
        }
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
    
    @SideOnly(Side.CLIENT)
    private void displayVehicleHealth(EntityPlayer player) {
        if (player.getRidingEntity() instanceof BaseVehicleEntity) {
            BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) player.getRidingEntity();
            DamageModule dmg = vehicle.getModuleByType(DamageModule.class);
            if (dmg != null) {
                float health = 100f - dmg.getDamage();
                player.sendStatusMessage(new net.minecraft.util.text.TextComponentString(String.format("%.0f/100", health)), true);
            }
            return;
        }

                java.util.function.Predicate<EnumBulletShapeType> pred = t -> !t.isPlayer();
        PhysicsRaycastResult result = DynamXUtils.castRayFromEntity(player, 5f, pred);
        if (result == null)
            return;

        BulletShapeType<?> shape = (BulletShapeType<?>) result.hitBody.getUserObject();
        if (!shape.getType().isBulletEntity() || !(shape.getObjectIn() instanceof BaseVehicleEntity))
            return;

        BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) shape.getObjectIn();
        DamageModule dmg = vehicle.getModuleByType(DamageModule.class);
        if (dmg != null) {
            float health = 100f - dmg.getDamage();
            player.sendStatusMessage(new net.minecraft.util.text.TextComponentString(String.format("%.0f/100", health)), true);
        }
    }

    @SideOnly(Side.CLIENT)
    private void displayWheelHealth(EntityPlayer player) {

        java.util.function.Predicate<EnumBulletShapeType> pred = t -> !t.isPlayer();
        PhysicsRaycastResult result = DynamXUtils.castRayFromEntity(player, 5f, pred);
        if (result == null)
            return;

        BulletShapeType<?> shape = (BulletShapeType<?>) result.hitBody.getUserObject();
        if (!shape.getType().isBulletEntity() || !(shape.getObjectIn() instanceof BaseVehicleEntity))
            return;

        BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) shape.getObjectIn();
        WheelsModule wheels = vehicle.getModuleByType(WheelsModule.class);
        WheelHealthModule wheelHealth = vehicle.getModuleByType(WheelHealthModule.class);
        if (wheels == null || wheelHealth == null)
            return;

        WheelsPhysicsHandler handler = wheels.getPhysicsHandler();
        if (handler == null)
            return;

        Vector3f hit = result.hitPos;
        float best = Float.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < handler.getNumWheels(); i++) {
            WheelPhysics w = handler.getWheel(i);
            if (w != null) {
                Vector3f wp = w.getPhysicsWheel().getWheelWorldLocation(new Vector3f());
                float dist = wp.distance(hit);
                if (dist < best) {
                    best = dist;
                    idx = i;
                }
            }
        }

        if (idx >= 0 && best < 1.5f) {
            float health = wheelHealth.getHealth(idx);
            player.sendStatusMessage(new net.minecraft.util.text.TextComponentString(String.format("%.0f/100", health)), true);
        }
    }
}