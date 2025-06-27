package fr.dynamx.addons.immersive.common.modules;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.api.entities.modules.IPhysicsModule;
import fr.dynamx.api.network.sync.EntityVariable;
import fr.dynamx.api.network.sync.SynchronizationRules;
import fr.dynamx.api.network.sync.SynchronizedEntityVariable;
import fr.dynamx.common.entities.BaseVehicleEntity;
import fr.dynamx.common.physics.entities.AbstractEntityPhysicsHandler;
import net.minecraft.nbt.NBTTagCompound;

@SynchronizedEntityVariable.SynchronizedPhysicsModule(modid = ImmersiveAddon.ID)
public class VehicleCustomizationModule implements IPhysicsModule<AbstractEntityPhysicsHandler<?, ?>> {

    private final BaseVehicleEntity<?> entity;

    @SynchronizedEntityVariable(name = "front_bumper")
    private final EntityVariable<String> frontBumper = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "");
    @SynchronizedEntityVariable(name = "rear_bumper")
    private final EntityVariable<String> rearBumper = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "");
    @SynchronizedEntityVariable(name = "hood")
    private final EntityVariable<String> hood = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "");
    @SynchronizedEntityVariable(name = "spoiler")
    private final EntityVariable<String> spoiler = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "");
    @SynchronizedEntityVariable(name = "roof")
    private final EntityVariable<String> roof = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "");
    @SynchronizedEntityVariable(name = "side_skirt")
    private final EntityVariable<String> sideSkirt = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "");

    public VehicleCustomizationModule(BaseVehicleEntity<?> entity) {
        this.entity = entity;
    }

    public void setPart(String slot, String name) {
        switch (slot) {
            case "front_bumper":
                frontBumper.set(name);
                break;
            case "rear_bumper":
                rearBumper.set(name);
                break;
            case "hood":
                hood.set(name);
                break;
            case "spoiler":
                spoiler.set(name);
                break;
            case "roof":
                roof.set(name);
                break;
            case "side_skirt":
                sideSkirt.set(name);
                break;
        }
    }

    public String getPart(String slot) {
        switch (slot) {
            case "front_bumper":
                return frontBumper.get();
            case "rear_bumper":
                return rearBumper.get();
            case "hood":
                return hood.get();
            case "spoiler":
                return spoiler.get();
            case "roof":
                return roof.get();
            case "side_skirt":
                return sideSkirt.get();
        }
        return "";
    }

    @Override
    public void initPhysicsEntity(AbstractEntityPhysicsHandler<?, ?> handler) {
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setString("front_bumper", frontBumper.get());
        tag.setString("rear_bumper", rearBumper.get());
        tag.setString("hood", hood.get());
        tag.setString("spoiler", spoiler.get());
        tag.setString("roof", roof.get());
        tag.setString("side_skirt", sideSkirt.get());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        frontBumper.set(tag.getString("front_bumper"));
        rearBumper.set(tag.getString("rear_bumper"));
        hood.set(tag.getString("hood"));
        spoiler.set(tag.getString("spoiler"));
        roof.set(tag.getString("roof"));
        sideSkirt.set(tag.getString("side_skirt"));
    }
}