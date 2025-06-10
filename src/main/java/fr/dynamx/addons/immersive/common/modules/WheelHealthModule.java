package fr.dynamx.addons.immersive.common.modules;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.api.entities.modules.IPhysicsModule;
import fr.dynamx.api.network.sync.EntityVariable;
import fr.dynamx.api.network.sync.SynchronizationRules;
import fr.dynamx.api.network.sync.SynchronizedEntityVariable;
import fr.dynamx.common.contentpack.parts.PartWheel;
import fr.dynamx.common.entities.BaseVehicleEntity;
import fr.dynamx.common.entities.modules.WheelsModule;
import fr.dynamx.common.physics.entities.AbstractEntityPhysicsHandler;
import fr.dynamx.common.physics.entities.modules.WheelsPhysicsHandler;
import fr.dynamx.common.physics.entities.parts.wheel.WheelPhysics;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;
import java.util.Random;

@SynchronizedEntityVariable.SynchronizedPhysicsModule(modid = ImmersiveAddon.ID)
public class WheelHealthModule implements IPhysicsModule<AbstractEntityPhysicsHandler<?, ?>>, IPhysicsModule.IEntityUpdateListener {

    private final BaseVehicleEntity<?> entity;

    @SynchronizedEntityVariable(name = "wheelHealth")
    private final EntityVariable<float[]> wheelHealth = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, new float[0]);

    public WheelHealthModule(BaseVehicleEntity<?> entity) {
        this.entity = entity;
        List<PartWheel> wheels = entity.getPackInfo().getPartsByType(PartWheel.class);
        float[] health = new float[wheels.size()];
        for(int i = 0; i < health.length; i++)
            health[i] = 100f;
        wheelHealth.set(health);
    }

    public void damageRandomWheel(float amount) {
        if(wheelHealth.get().length == 0)
            return;
        int index = new Random().nextInt(wheelHealth.get().length);
        damageWheel(index, amount);
    }

    public void damageWheel(int index, float amount) {
        float[] arr = wheelHealth.get();
        if(index < 0 || index >= arr.length)
            return;
        arr[index] = Math.max(0, arr[index] - amount);
        if(arr[index] <= 0) {
            punctureWheel(index);
        } else if(new Random().nextInt(10) == 0) {
            punctureWheel(index);
        }
        wheelHealth.set(arr);
    }

    private void punctureWheel(int index) {
        float[] arr = wheelHealth.get();
        if(index < 0 || index >= arr.length)
            return;
        arr[index] = 0f;
        WheelsModule wheelModule = entity.getModuleByType(WheelsModule.class);
        if(wheelModule != null) {
            WheelsPhysicsHandler handler = wheelModule.getPhysicsHandler();
            if(handler != null) {
                WheelPhysics wp = handler.getWheelByPartIndex((byte) index);
                if(wp != null && !wp.isFlattened()) {
                    wp.setFlattened(true);
                }
            }
        }
        wheelHealth.set(arr);
    }

    public void repairWheel(int index) {
        float[] arr = wheelHealth.get();
        if(index < 0 || index >= arr.length)
            return;
        arr[index] = 100f;
    }

    public float getHealth(int index) {
        if(index < 0 || index >= wheelHealth.get().length)
            return 0;
        return wheelHealth.get()[index];
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setIntArray("wheelHealth", toIntArray(wheelHealth.get()));
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        int[] arr = tag.getIntArray("wheelHealth");
        float[] h = new float[arr.length];
        for(int i=0;i<arr.length;i++)
            h[i] = arr[i];
        wheelHealth.set(h);
    }

    private static int[] toIntArray(float[] arr) {
        int[] r = new int[arr.length];
        for(int i=0;i<arr.length;i++)
            r[i] = (int)arr[i];
        return r;
    }

    @Override
    public void updateEntity() {
        //no-op
    }
}