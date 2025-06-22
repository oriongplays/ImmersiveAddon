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
            SynchronizationRules.PHYSICS_TO_SPECTATORS,
            WheelTuningHelper.clampFriction(1.5f));

    @SynchronizedEntityVariable(name = "wheelBrake")
    private final EntityVariable<Float> brakeForce = new EntityVariable<>((v, val) -> apply(),
            SynchronizationRules.PHYSICS_TO_SPECTATORS,
            WheelTuningHelper.clampBrake(200f));

    @SynchronizedEntityVariable(name = "wheelRest")
    private final EntityVariable<Float> restLength = new EntityVariable<>((v, val) -> apply(),
            SynchronizationRules.PHYSICS_TO_SPECTATORS,
            WheelTuningHelper.clampRest(0.22f));

    @SynchronizedEntityVariable(name = "wheelStiff")
    private final EntityVariable<Float> stiffness = new EntityVariable<>((v, val) -> apply(),
            SynchronizationRules.PHYSICS_TO_SPECTATORS,
            WheelTuningHelper.clampStiff(30f));

    @SynchronizedEntityVariable(name = "wheelParticle")
    private final EntityVariable<String> skidParticle = new EntityVariable<>((v, val) -> apply(),
            SynchronizationRules.PHYSICS_TO_SPECTATORS, "spit");

    public WheelPropertiesModule(BaseVehicleEntity<?> entity) {
        this.entity = entity;
    }

    public void setModel(String mdl) {
        this.model.set(mdl);
        apply();
    }

    public void setFriction(float f) {
        this.friction.set(WheelTuningHelper.clampFriction(f));
        apply();
    }

    public void setBrakeForce(float b) {
        this.brakeForce.set(WheelTuningHelper.clampBrake(b));
        apply();
    }

    public void setRestLength(float l) {
        this.restLength.set(WheelTuningHelper.clampRest(l));
        apply();
    }

    public void setStiffness(float s) {
        this.stiffness.set(WheelTuningHelper.clampStiff(s));
        apply();
    }

    public void setSkidParticle(String p) {
        if (WheelTuningHelper.isValidParticle(p)) {
            this.skidParticle.set(p);
        }
        apply();
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
        model.set(tag.getString("wheelModel"));
        friction.set(WheelTuningHelper.clampFriction(tag.getFloat("wheelFriction")));
        brakeForce.set(WheelTuningHelper.clampBrake(tag.getFloat("wheelBrake")));
        restLength.set(WheelTuningHelper.clampRest(tag.getFloat("wheelRest")));
        stiffness.set(WheelTuningHelper.clampStiff(tag.getFloat("wheelStiff")));
        if (tag.hasKey("wheelParticle")) {
            String p = tag.getString("wheelParticle");
            if (WheelTuningHelper.isValidParticle(p))
                skidParticle.set(p);
        }
        apply();
    }
}