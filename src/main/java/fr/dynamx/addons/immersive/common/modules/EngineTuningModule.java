/**
 * Stores the tuning level of a vehicle engine and applies presets.
 * <p>
 * This class must reside in a file named {@code EngineTuningModule.java}
 * otherwise the Java compiler will fail.
 */
package fr.dynamx.addons.immersive.common.modules;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.addons.immersive.common.helpers.EngineTuningHelper;
import fr.dynamx.api.entities.modules.IPhysicsModule;
import fr.dynamx.api.network.sync.EntityVariable;
import fr.dynamx.api.network.sync.SynchronizationRules;
import fr.dynamx.api.network.sync.SynchronizedEntityVariable;
import fr.dynamx.common.entities.BaseVehicleEntity;
import fr.dynamx.common.entities.modules.engines.CarEngineModule;
import fr.dynamx.common.physics.entities.AbstractEntityPhysicsHandler;
import fr.dynamx.common.physics.entities.parts.engine.Engine;
import net.minecraft.nbt.NBTTagCompound;

@SynchronizedEntityVariable.SynchronizedPhysicsModule(modid = ImmersiveAddon.ID)
public class EngineTuningModule implements IPhysicsModule<AbstractEntityPhysicsHandler<?, ?>> {

    private final BaseVehicleEntity<?> entity;
    @SynchronizedEntityVariable(name = "tuningLevel")
    private final EntityVariable<Integer> tuningLevel = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, 1);

    private float basePower = -1f;
    private float baseMaxRevs = -1f;

    public EngineTuningModule(BaseVehicleEntity<?> entity) {
        this.entity = entity;
    }

    public void setTuningLevel(int level) {
        this.tuningLevel.set(level);
        applyTuning();
    }

    public int getTuningLevel() {
        return tuningLevel.get();
    }

    private void applyTuning() {
        CarEngineModule engineModule = entity.getModuleByType(CarEngineModule.class);
        if(engineModule == null || engineModule.getPhysicsHandler() == null)
            return;
        Engine engine = engineModule.getPhysicsHandler().getEngine();
        if(engine == null)
            return;
        if(basePower < 0)
        {
            basePower = engine.getPower();
            baseMaxRevs = engine.getMaxRevs();
        }
        EngineTuningHelper.applyTuning(engine, tuningLevel.get(), basePower, baseMaxRevs);
    }

    @Override
    public void initPhysicsEntity(AbstractEntityPhysicsHandler<?, ?> handler) {
        applyTuning();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("tuningLevel", tuningLevel.get());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        tuningLevel.set(tag.getInteger("tuningLevel"));
    }
}