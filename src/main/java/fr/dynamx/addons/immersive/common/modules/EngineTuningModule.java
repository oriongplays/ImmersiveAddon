/**
 * Stores the tuning level of a vehicle engine and applies presets.
 * <p>
 * This class must reside in a file named {@code EngineTuningModule.java}
 * otherwise the Java compiler will fail.
 */
package fr.dynamx.addons.immersive.common.modules;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.addons.immersive.common.helpers.EngineTuningHelper;
import fr.dynamx.addons.immersive.common.helpers.EngineTuningHelper.EngineLevelConfig;
import fr.dynamx.addons.immersive.common.helpers.VehicleOverrideHelper;
import fr.dynamx.addons.immersive.ImmersiveAddonConfig;
import fr.dynamx.api.entities.modules.IPhysicsModule;
import fr.dynamx.api.network.sync.EntityVariable;
import fr.dynamx.api.network.sync.SynchronizationRules;
import fr.dynamx.api.network.sync.SynchronizedEntityVariable;
import fr.dynamx.common.entities.BaseVehicleEntity;
import fr.dynamx.common.entities.modules.engines.CarEngineModule;
import fr.dynamx.common.physics.entities.AbstractEntityPhysicsHandler;
import fr.dynamx.common.physics.entities.parts.engine.Engine;
import fr.dynamx.common.entities.modules.SeatsModule;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.nbt.NBTTagCompound;

@SynchronizedEntityVariable.SynchronizedPhysicsModule(modid = ImmersiveAddon.ID)
public class EngineTuningModule implements IPhysicsModule<AbstractEntityPhysicsHandler<?, ?>> {

    private final BaseVehicleEntity<?> entity;
    @SynchronizedEntityVariable(name = "engineLevel")
    private final EntityVariable<Integer> tuningLevel = new EntityVariable<>((v,val) -> apply(),
            SynchronizationRules.PHYSICS_TO_SPECTATORS, 1);
    @SynchronizedEntityVariable(name = "enginePower")
    private final EntityVariable<Float> power = new EntityVariable<>((v,val) -> apply(),
            SynchronizationRules.PHYSICS_TO_SPECTATORS, -1f);
    @SynchronizedEntityVariable(name = "engineMaxRPM")
    private final EntityVariable<Float> maxRPM = new EntityVariable<>((v,val) -> apply(),
            SynchronizationRules.PHYSICS_TO_SPECTATORS, -1f);
    @SynchronizedEntityVariable(name = "engineBraking")
    private final EntityVariable<Float> braking = new EntityVariable<>((v,val) -> apply(),
            SynchronizationRules.PHYSICS_TO_SPECTATORS, -1f);

    private EngineLevelConfig defaults = new EngineLevelConfig();
    private boolean updating;

    public EngineTuningModule(BaseVehicleEntity<?> entity) {
        this.entity = entity;
    }

    public void setTuningLevel(int lvl) {
        updating = true;
        this.tuningLevel.set(lvl);
        updating = false;
        if (entity.world != null && !entity.world.isRemote) {
            EngineLevelConfig cfg = EngineTuningHelper.loadLevel(lvl);
            if (cfg.power > 0) setSynced(power, cfg.power);
            if (cfg.maxRPM > 0) setSynced(maxRPM, cfg.maxRPM);
            if (cfg.braking > 0) setSynced(braking, cfg.braking);
        }
        apply();
    }

    public int getTuningLevel() {
        return tuningLevel.get();
    }

    private void apply() {
        if (updating) {
            return;
        }
        CarEngineModule engineModule = entity.getModuleByType(CarEngineModule.class);
        if (engineModule == null) {
            return;
        }
        boolean server = entity.world != null && !entity.world.isRemote;
        Engine engine = engineModule.getPhysicsHandler() != null ?
                engineModule.getPhysicsHandler().getEngine() : null;
        if (server) {
            defaults = EngineTuningHelper.loadLevel(tuningLevel.get());
            if (power.get() < 0 && defaults.power > 0) {
                setSynced(power, defaults.power);
            }
            if (maxRPM.get() < 0 && defaults.maxRPM > 0) {
                setSynced(maxRPM, defaults.maxRPM);
            }
            if (braking.get() < 0 && defaults.braking > 0) {
                setSynced(braking, defaults.braking);
            }
            float p = power.get();
            float rpm = maxRPM.get();
            float brk = braking.get();
            VehicleOverrideHelper.VehicleOverride override = VehicleOverrideHelper.getOverride(entity);
            if (p > 0) {
                p *= override.getEnginePowerMultiplier();
            }
            if (rpm > 0) {
                rpm *= override.getEngineMaxRPMMultiplier();
            }
            if (brk > 0) {
                brk *= override.getEngineBrakingMultiplier();
            }
            setSynced(power, p);
            setSynced(maxRPM, rpm);
            setSynced(braking, brk);
            EngineTuningHelper.applyTuning(engine, engineModule, tuningLevel.get(), p, rpm, brk);
            ImmersiveAddon.LOGGER.info("Applying engine tuning for {}: level={} power={} maxRPM={} braking={}",
                    entity.getName(), tuningLevel.get(), p, rpm, brk);
        } else {
            float p = power.get();
            float rpm = maxRPM.get();
            float brk = braking.get();
            if (p < 0 || rpm < 0 || brk < 0) {
                return;
            }
            EngineTuningHelper.applySyncedValues(engine, engineModule, p, rpm, brk);
        }
        if (ImmersiveAddonConfig.debug && entity.hasModuleOfType(SeatsModule.class)) {
            Entity rider = entity.getModuleByType(SeatsModule.class).getControllingPassenger();
            if (rider instanceof EntityPlayer) {
                ((EntityPlayer) rider).sendStatusMessage(new TextComponentString("Debug: engine level=" + tuningLevel.get()), true);
            }
        }
        if (!entity.world.isRemote && engine != null) {
            ImmersiveAddon.LOGGER.info("Engine values now power={} maxRPM={} braking={}",
                    engine.getPower(), engine.getMaxRevs(), engine.getBraking());
        }
    }

    @Override
    public void initPhysicsEntity(AbstractEntityPhysicsHandler<?, ?> handler) {
        apply();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("engineLevel", tuningLevel.get());
        tag.setFloat("enginePower", power.get());
        tag.setFloat("engineMaxRPM", maxRPM.get());
        tag.setFloat("engineBraking", braking.get());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        int lvl = tag.getInteger("engineLevel");
        lvl = Math.max(1, Math.min(lvl, 5));
        updating = true;
        tuningLevel.set(lvl);
        updating = false;
        if (entity.world != null && entity.world.isRemote) {
            power.set(tag.getFloat("enginePower"));
            maxRPM.set(tag.getFloat("engineMaxRPM"));
            braking.set(tag.getFloat("engineBraking"));
        } else {
            defaults = EngineTuningHelper.loadLevel(tuningLevel.get());
            float storedPower = tag.getFloat("enginePower");
            float storedMax = tag.getFloat("engineMaxRPM");
            float storedBrk = tag.getFloat("engineBraking");
            if (storedPower > 0) {
                setSynced(power, storedPower);
            } else if (defaults.power > 0) {
                setSynced(power, defaults.power);
            } else {
                setSynced(power, -1f);
            }
            if (storedMax > 0) {
                setSynced(maxRPM, storedMax);
            } else if (defaults.maxRPM > 0) {
                setSynced(maxRPM, defaults.maxRPM);
            } else {
                setSynced(maxRPM, -1f);
            }
            if (storedBrk > 0) {
                setSynced(braking, storedBrk);
            } else if (defaults.braking > 0) {
                setSynced(braking, defaults.braking);
            } else {
                setSynced(braking, -1f);
            }
        }
        apply();
    }
    
    private void setSynced(EntityVariable<Float> variable, float value) {
        if (Float.isNaN(value)) {
            return;
        }
        Float currentObj = variable.get();
        float current = currentObj != null ? currentObj : Float.NaN;
        if (!Float.isNaN(current) && Math.abs(current - value) < 1.0e-4f) {
            return;
        }
        updating = true;
        variable.set(value);
        updating = false;
    }
}