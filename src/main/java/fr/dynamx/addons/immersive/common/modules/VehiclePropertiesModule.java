package fr.dynamx.addons.immersive.common.modules;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.addons.immersive.common.helpers.VehicleLevelConfig;
import fr.dynamx.addons.immersive.common.helpers.VehicleOverrideHelper;
import fr.dynamx.api.entities.modules.IPhysicsModule;
import fr.dynamx.api.network.sync.EntityVariable;
import fr.dynamx.api.network.sync.SynchronizationRules;
import fr.dynamx.api.network.sync.SynchronizedEntityVariable;
import fr.dynamx.common.entities.BaseVehicleEntity;
import fr.dynamx.common.contentpack.type.vehicle.ModularVehicleInfo;
import fr.dynamx.common.physics.entities.AbstractEntityPhysicsHandler;
import fr.dynamx.common.entities.modules.SeatsModule;
import fr.dynamx.addons.immersive.ImmersiveAddonConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;

/**
 * Allows overriding vehicle physics properties from NBT or upgrades.
 */
@SynchronizedEntityVariable.SynchronizedPhysicsModule(modid = ImmersiveAddon.ID)
public class VehiclePropertiesModule implements IPhysicsModule<AbstractEntityPhysicsHandler<?, ?>> {
    private final BaseVehicleEntity<?> entity;

    @SynchronizedEntityVariable(name = "model")
    private final EntityVariable<String> model = new EntityVariable<>((v, val) -> apply(),
            SynchronizationRules.PHYSICS_TO_SPECTATORS, "");
    @SynchronizedEntityVariable(name = "mass")
    private final EntityVariable<Integer> mass = new EntityVariable<>((v, val) -> apply(),
            SynchronizationRules.PHYSICS_TO_SPECTATORS, -1);
    @SynchronizedEntityVariable(name = "drag")
    private final EntityVariable<Float> drag = new EntityVariable<>((v, val) -> apply(),
            SynchronizationRules.PHYSICS_TO_SPECTATORS, -1f);
    @SynchronizedEntityVariable(name = "pesoType")
    private final EntityVariable<String> type = new EntityVariable<>((v, val) -> apply(),
            SynchronizationRules.PHYSICS_TO_SPECTATORS, "medio");

    private VehicleLevelConfig defaults = new VehicleLevelConfig();
    private boolean updating;

    public VehiclePropertiesModule(BaseVehicleEntity<?> entity) {
        this.entity = entity;
    }

    public void setWeightType(String newType) {
        updating = true;
        this.type.set(newType);
        updating = false;
        apply();
    }

    private void apply() {
        if (updating) {
            return;
        }
        if (!(entity.getPackInfo() instanceof ModularVehicleInfo)) {
            return;
        }
        ModularVehicleInfo info = (ModularVehicleInfo) entity.getPackInfo();
        if (entity.world != null && !entity.world.isRemote) {
            defaults = VehicleLevelConfig.loadDefault(type.get());
            int baseMass = mass.get();
            float dragValue = drag.get();
            String mdl = model.get();
            if (defaults.emptyMass > 0) {
                baseMass = defaults.emptyMass;
            }
            if (defaults.dragCoefficient >= 0) {
                dragValue = defaults.dragCoefficient;
            }
            if (!defaults.model.isEmpty()) {
                mdl = defaults.model;
            }
                        VehicleOverrideHelper.VehicleOverride override = VehicleOverrideHelper.getOverride(entity);
            if (baseMass > 0) {
                float weightMultiplier = override.getWeightMultiplier();
                if (weightMultiplier > 0) {
                    baseMass = Math.round(baseMass * weightMultiplier);
                }
            }
            setSynced(model, mdl);
            setSynced(mass, baseMass);
            setSynced(drag, dragValue);
        }
        ImmersiveAddon.LOGGER.info("Applying vehicle properties for {}: type={} mass={} drag={}",
                entity.getName(), type.get(), mass.get(), drag.get());
        String mdl = model.get();
        if (!mdl.isEmpty()) {
            info.setModel(new ResourceLocation(mdl));
        }
        int m = mass.get();
        if (m > 0) {
            info.setEmptyMass(m);
            AbstractEntityPhysicsHandler<?, ?> handler = entity.getPhysicsHandler();
            if (handler != null) {
                com.jme3.bullet.collision.PhysicsCollisionObject obj = handler.getCollisionObject();
                if (obj instanceof com.jme3.bullet.objects.PhysicsRigidBody) {
                    ((com.jme3.bullet.objects.PhysicsRigidBody) obj).setMass(m);
                    handler.setForceActivation(true);
                    handler.activate();
                }
            }
        }
        float d = drag.get();
        if (d >= 0) {
            info.setDragFactor(d);
        }
        if (ImmersiveAddonConfig.debug && entity.hasModuleOfType(SeatsModule.class)) {
            Entity rider = entity.getModuleByType(SeatsModule.class).getControllingPassenger();
            if (rider instanceof EntityPlayer) {
                ((EntityPlayer) rider).sendStatusMessage(new TextComponentString("Debug: mass=" + m + " type=" + type.get()), true);
            }
        }
    }

    @Override
    public void initPhysicsEntity(AbstractEntityPhysicsHandler<?, ?> handler) {
        apply();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setString("model", model.get());
        tag.setInteger("mass", mass.get());
        tag.setFloat("drag", drag.get());
        tag.setString("PesoDeVeiculo", type.get());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        updating = true;
        model.set(tag.getString("model"));
        mass.set(tag.getInteger("mass"));
        drag.set(tag.getFloat("drag"));
        if (tag.hasKey("PesoDeVeiculo")) {
            type.set(tag.getString("PesoDeVeiculo"));
        }
                updating = false;
        if (entity.world != null && !entity.world.isRemote) {
            defaults = VehicleLevelConfig.loadDefault(type.get());
            int baseMass = mass.get();
            float dragValue = drag.get();
            String mdl = model.get();
            if (defaults.emptyMass > 0) {
                baseMass = defaults.emptyMass;
            }
            if (defaults.dragCoefficient >= 0) {
                dragValue = defaults.dragCoefficient;
            }
            if (!defaults.model.isEmpty()) {
                mdl = defaults.model;
            }
            VehicleOverrideHelper.VehicleOverride override = VehicleOverrideHelper.getOverride(entity);
            if (baseMass > 0) {
                float weightMultiplier = override.getWeightMultiplier();
                if (weightMultiplier > 0) {
                    baseMass = Math.round(baseMass * weightMultiplier);
                }
            }
            setSynced(model, mdl);
            setSynced(mass, baseMass);
            setSynced(drag, dragValue);
        }
        apply();
    }

    private void setSynced(EntityVariable<String> variable, String value) {
        if (value == null) {
            value = "";
        }
        String current = variable.get();
        if (value.equals(current != null ? current : "")) {
            return;
        }
        updating = true;
        variable.set(value);
        updating = false;
    }

    private void setSynced(EntityVariable<Integer> variable, int value) {
        Integer current = variable.get();
        if (current != null && current == value) {
            return;
        }
        updating = true;
        variable.set(value);
        updating = false;
    }

    private void setSynced(EntityVariable<Float> variable, float value) {
        Float current = variable.get();
        if (current != null && Math.abs(current - value) < 1.0e-4f) {
            return;
        }
        updating = true;
        variable.set(value);
        updating = false;
    }
}