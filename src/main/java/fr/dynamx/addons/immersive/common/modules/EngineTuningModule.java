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

    public EngineTuningModule(BaseVehicleEntity<?> entity) {
        this.entity = entity;
    }

    public void setTuningLevel(int lvl) {
        this.tuningLevel.set(lvl);
        if (entity.world != null && !entity.world.isRemote) {
            EngineLevelConfig cfg = EngineTuningHelper.loadLevel(lvl);
            if (cfg.power > 0) power.set(cfg.power);
            if (cfg.maxRPM > 0) maxRPM.set(cfg.maxRPM);
            if (cfg.braking > 0) braking.set(cfg.braking);
        }
        apply();
    }

    public int getTuningLevel() {
        return tuningLevel.get();
    }

    private void apply() {
        CarEngineModule engineModule = entity.getModuleByType(CarEngineModule.class);
        if (engineModule == null)
            return;
        defaults = EngineTuningHelper.loadLevel(tuningLevel.get());
        if (entity.world != null && !entity.world.isRemote) {
            if (power.get() < 0 && defaults.power > 0) power.set(defaults.power);
            if (maxRPM.get() < 0 && defaults.maxRPM > 0) maxRPM.set(defaults.maxRPM);
            if (braking.get() < 0 && defaults.braking > 0) braking.set(defaults.braking);
            ImmersiveAddon.LOGGER.info("Applying engine tuning for {}: level={}", entity.getName(), tuningLevel.get());
        }
        float p = power.get() >= 0 ? power.get() : defaults.power;
        float rpm = maxRPM.get() >= 0 ? maxRPM.get() : defaults.maxRPM;
        float brk = braking.get() >= 0 ? braking.get() : defaults.braking;
        Engine engine = engineModule.getPhysicsHandler() != null ?
                engineModule.getPhysicsHandler().getEngine() : null;
        EngineTuningHelper.applyTuning(engine, engineModule, tuningLevel.get(), p, rpm, brk);
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
        tuningLevel.set(lvl);
        power.set(tag.getFloat("enginePower"));
        maxRPM.set(tag.getFloat("engineMaxRPM"));
        braking.set(tag.getFloat("engineBraking"));
        if (entity.world != null && !entity.world.isRemote) {
            defaults = EngineTuningHelper.loadLevel(tuningLevel.get());
            if (power.get() < 0 && defaults.power > 0) power.set(defaults.power);
            if (maxRPM.get() < 0 && defaults.maxRPM > 0) maxRPM.set(defaults.maxRPM);
            if (braking.get() < 0 && defaults.braking > 0) braking.set(defaults.braking);
        }
        apply();
    }
}