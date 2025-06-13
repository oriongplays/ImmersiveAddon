package fr.dynamx.addons.immersive.common.modules;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.addons.immersive.common.helpers.VehicleLevelConfig;
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

    public VehiclePropertiesModule(BaseVehicleEntity<?> entity) {
        this.entity = entity;
    }

    public void setWeightType(String newType) {
        this.type.set(newType);
        // Load defaults from the server config when the type changes so clients
        // cannot spoof their own values
        if (entity.world != null && !entity.world.isRemote) {
            VehicleLevelConfig cfg = VehicleLevelConfig.loadDefault(newType);
            if (cfg.emptyMass > 0) {
                this.mass.set(cfg.emptyMass);
            }
            if (cfg.dragCoefficient >= 0) {
                this.drag.set(cfg.dragCoefficient);
            }
            if (!cfg.model.isEmpty()) {
                this.model.set(cfg.model);
            }
        }
        apply();
    }

    private void apply() {
        if (!(entity.getPackInfo() instanceof ModularVehicleInfo)) {
            return;
        }
        ModularVehicleInfo info = (ModularVehicleInfo) entity.getPackInfo();
        defaults = VehicleLevelConfig.loadDefault(type.get());
        // On the server we fill missing values from the official config so
        // clients cannot rely on their own JSON files
        if (entity.world != null && !entity.world.isRemote) {
            if (mass.get() <= 0 && defaults.emptyMass > 0) {
                mass.set(defaults.emptyMass);
            }
            if (drag.get() < 0 && defaults.dragCoefficient >= 0) {
                drag.set(defaults.dragCoefficient);
            }
            if (model.get().isEmpty() && !defaults.model.isEmpty()) {
                model.set(defaults.model);
            }
        }
        ImmersiveAddon.LOGGER.info("Applying vehicle properties for {}: type={} mass={} drag={}",
                entity.getName(), type.get(), mass.get(), drag.get());
        String mdl = model.get();
        if (!mdl.isEmpty()) {
            info.setModel(new ResourceLocation(mdl));
        }
        int m = mass.get();
        if (m <= 0) {
            m = defaults.emptyMass;
        }
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
        if (d < 0) {
            d = defaults.dragCoefficient;
        }
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
        model.set(tag.getString("model"));
        mass.set(tag.getInteger("mass"));
        drag.set(tag.getFloat("drag"));
        if (tag.hasKey("PesoDeVeiculo")) {
            type.set(tag.getString("PesoDeVeiculo"));
        }
        if (entity.world != null && !entity.world.isRemote) {
            defaults = VehicleLevelConfig.loadDefault(type.get());
            if (mass.get() <= 0 && defaults.emptyMass > 0) {
                mass.set(defaults.emptyMass);
            }
            if (drag.get() < 0 && defaults.dragCoefficient >= 0) {
                drag.set(defaults.dragCoefficient);
            }
            if (model.get().isEmpty() && !defaults.model.isEmpty()) {
                model.set(defaults.model);
            }
        }
        apply();
    }
}