package fr.dynamx.addons.immersive.common.modules;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.api.entities.modules.IPhysicsModule;
import fr.dynamx.api.network.sync.EntityVariable;
import fr.dynamx.api.network.sync.SynchronizationRules;
import fr.dynamx.api.network.sync.SynchronizedEntityVariable;
import fr.dynamx.common.contentpack.parts.PartWheel;
import fr.dynamx.common.contentpack.type.vehicle.PartWheelInfo;
import fr.dynamx.common.entities.BaseVehicleEntity;
import fr.dynamx.common.entities.modules.WheelsModule;
import fr.dynamx.common.physics.entities.AbstractEntityPhysicsHandler;
import fr.dynamx.common.physics.entities.modules.WheelsPhysicsHandler;
import fr.dynamx.common.physics.entities.parts.wheel.WheelPhysics;
import fr.dynamx.addons.immersive.common.helpers.WheelTuningHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Field;
import java.util.List;

@SynchronizedEntityVariable.SynchronizedPhysicsModule(modid = ImmersiveAddon.ID)
public class WheelPropertiesModule implements IPhysicsModule<AbstractEntityPhysicsHandler<?, ?>> {
    private final BaseVehicleEntity<?> entity;

    @SynchronizedEntityVariable(name = "wheelModel")
    private final EntityVariable<String> model = new EntityVariable<>((v, val) -> apply(),
            SynchronizationRules.PHYSICS_TO_SPECTATORS, "");

    @SynchronizedEntityVariable(name = "wheelFriction")
    private final EntityVariable<Float> friction = new EntityVariable<>((v, val) -> apply(),
            SynchronizationRules.PHYSICS_TO_SPECTATORS, 1.5f);

    @SynchronizedEntityVariable(name = "wheelBrake")
    private final EntityVariable<Float> brakeForce = new EntityVariable<>((v, val) -> apply(),
            SynchronizationRules.PHYSICS_TO_SPECTATORS, 200f);

    @SynchronizedEntityVariable(name = "wheelRest")
    private final EntityVariable<Float> restLength = new EntityVariable<>((v, val) -> apply(),
            SynchronizationRules.PHYSICS_TO_SPECTATORS, 0.22f);

    @SynchronizedEntityVariable(name = "wheelStiff")
    private final EntityVariable<Float> stiffness = new EntityVariable<>((v, val) -> apply(),
            SynchronizationRules.PHYSICS_TO_SPECTATORS, 30f);

    @SynchronizedEntityVariable(name = "wheelParticle")
    private final EntityVariable<String> skidParticle = new EntityVariable<>((v, val) -> apply(),
            SynchronizationRules.PHYSICS_TO_SPECTATORS, "spit");

    private boolean updating;

    public WheelPropertiesModule(BaseVehicleEntity<?> entity) {
        this.entity = entity;
    }

    public void setModel(String mdl) {
        this.model.set(mdl);
        apply();
    }

    public void setFriction(float f) {
        if (entity.world != null && entity.world.isRemote) {
            return;
        }
        setSynced(friction, WheelTuningHelper.clampFriction(f));
    }

    public void setBrakeForce(float b) {
        if (entity.world != null && entity.world.isRemote) {
            return;
        }
        setSynced(brakeForce, WheelTuningHelper.clampBrake(b));
    }

    public void setRestLength(float l) {
        if (entity.world != null && entity.world.isRemote) {
            return;
        }
        setSynced(restLength, WheelTuningHelper.clampRest(l));
    }

    public void setStiffness(float s) {
        if (entity.world != null && entity.world.isRemote) {
            return;
        }
        setSynced(stiffness, WheelTuningHelper.clampStiff(s));
    }

    public void setSkidParticle(String p) {
        if (entity.world != null && entity.world.isRemote) {
            return;
        }
        if (WheelTuningHelper.isValidParticle(p)) {
            setSynced(skidParticle, p);
        }
    }

        public String getSkidParticle() {
        return skidParticle.get();
    }
    
    private void updateInfo(PartWheelInfo info) {
        try {
            Field f;
            if (!model.get().isEmpty()) {
                f = PartWheelInfo.class.getDeclaredField("model");
                f.setAccessible(true);
                f.set(info, new ResourceLocation(model.get()));
            }
            f = PartWheelInfo.class.getDeclaredField("wheelFriction");
            f.setAccessible(true);
            f.setFloat(info, friction.get());
            f = PartWheelInfo.class.getDeclaredField("wheelBrakeForce");
            f.setAccessible(true);
            f.setFloat(info, brakeForce.get());
            f = PartWheelInfo.class.getDeclaredField("suspensionRestLength");
            f.setAccessible(true);
            f.setFloat(info, restLength.get());
            f = PartWheelInfo.class.getDeclaredField("suspensionStiffness");
            f.setAccessible(true);
            f.setFloat(info, stiffness.get());
            f = PartWheelInfo.class.getDeclaredField("skidParticle");
            f.setAccessible(true);
            f.set(info, EnumParticleTypes.getByName(skidParticle.get()));
        } catch (Exception ignored) {
        }
    }

    private void apply() {
        if (updating) {
            return;
        }
        if (entity.world != null && !entity.world.isRemote) {
            setSynced(friction, WheelTuningHelper.clampFriction(friction.get()));
            setSynced(brakeForce, WheelTuningHelper.clampBrake(brakeForce.get()));
            setSynced(restLength, WheelTuningHelper.clampRest(restLength.get()));
            setSynced(stiffness, WheelTuningHelper.clampStiff(stiffness.get()));
            String particle = skidParticle.get();
            if (!WheelTuningHelper.isValidParticle(particle)) {
                setSynced(skidParticle, "spit");
            }
        }
        WheelsModule wheelModule = entity.getModuleByType(WheelsModule.class);
        if (wheelModule == null)
            return;
        List<PartWheel> parts = entity.getPackInfo().getPartsByType(PartWheel.class);
        for (int i = 0; i < parts.size(); i++) {
            PartWheelInfo info = wheelModule.getWheelInfo((byte) i);
            if (info != null) {
                updateInfo(info);
                wheelModule.setWheelInfo((byte) i, info);
            }
        }
        WheelsPhysicsHandler handler = wheelModule.getPhysicsHandler();
        if (handler != null) {
            for (int i = 0; i < handler.getNumWheels(); i++) {
                WheelPhysics wheel = handler.getWheel(i);
                if (wheel != null) {
                    wheel.setFriction(friction.get());
                    wheel.setBrakeStrength(brakeForce.get());
                    if (wheel.getSuspension() != null) {
                        wheel.getSuspension().setRestLength(restLength.get());
                        wheel.getSuspension().setStiffness(stiffness.get());
                    }
                }
            }
        }
    }

    @Override
    public void initPhysicsEntity(AbstractEntityPhysicsHandler<?, ?> handler) {
        apply();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setString("wheelModel", model.get());
        tag.setFloat("wheelFriction", friction.get());
        tag.setFloat("wheelBrake", brakeForce.get());
        tag.setFloat("wheelRest", restLength.get());
        tag.setFloat("wheelStiff", stiffness.get());
        tag.setString("wheelParticle", skidParticle.get());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        updating = true;
        model.set(tag.getString("wheelModel"));
        friction.set(tag.getFloat("wheelFriction"));
        brakeForce.set(tag.getFloat("wheelBrake"));
        restLength.set(tag.getFloat("wheelRest"));
        stiffness.set(tag.getFloat("wheelStiff"));
        if (tag.hasKey("wheelParticle")) {
            skidParticle.set(tag.getString("wheelParticle"));
        }
        updating = false;
        if (entity.world != null && !entity.world.isRemote) {
            setSynced(friction, WheelTuningHelper.clampFriction(friction.get()));
            setSynced(brakeForce, WheelTuningHelper.clampBrake(brakeForce.get()));
            setSynced(restLength, WheelTuningHelper.clampRest(restLength.get()));
            setSynced(stiffness, WheelTuningHelper.clampStiff(stiffness.get()));
            if (!WheelTuningHelper.isValidParticle(skidParticle.get())) {
                setSynced(skidParticle, "spit");
            }
        }
        apply();
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
}