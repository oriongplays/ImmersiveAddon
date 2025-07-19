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
    private final EntityVariable<String> frontBumper = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "front_bumper_original");
    @SynchronizedEntityVariable(name = "rear_bumper")
    private final EntityVariable<String> rearBumper = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "rear_bumper_original");
    @SynchronizedEntityVariable(name = "hood")
    private final EntityVariable<String> hood = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "hood_original");
    @SynchronizedEntityVariable(name = "spoiler")
    private final EntityVariable<String> spoiler = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "spoiler_original");
    @SynchronizedEntityVariable(name = "roof")
    private final EntityVariable<String> roof = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "roof_original");
    @SynchronizedEntityVariable(name = "side_skirt")
    private final EntityVariable<String> sideSkirt = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "side_skirt_original");
    @SynchronizedEntityVariable(name = "accessory")
    private final EntityVariable<String> accessory = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "accessory_original");
    @SynchronizedEntityVariable(name = "sound")
    private final EntityVariable<String> sound = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "sound_original");
    @SynchronizedEntityVariable(name = "neon")
    private final EntityVariable<String> neon = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "neon_original");

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
            case "accessory":
                accessory.set(name);
                break;
            case "sound":
                sound.set(name);
                break;
            case "neon":
                neon.set(name);
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
            case "accessory":
                return accessory.get();
            case "sound":
                return sound.get();
            case "neon":
                return neon.get();
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
        tag.setString("accessory", accessory.get());
        tag.setString("sound", sound.get());
        tag.setString("neon", neon.get());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        if(tag.hasKey("front_bumper"))
            frontBumper.set(tag.getString("front_bumper"));
        if(tag.hasKey("rear_bumper"))
            rearBumper.set(tag.getString("rear_bumper"));
        if(tag.hasKey("hood"))
            hood.set(tag.getString("hood"));
        if(tag.hasKey("spoiler"))
            spoiler.set(tag.getString("spoiler"));
        if(tag.hasKey("roof"))
            roof.set(tag.getString("roof"));
        if(tag.hasKey("side_skirt"))
            sideSkirt.set(tag.getString("side_skirt"));
        if(tag.hasKey("accessory"))
            accessory.set(tag.getString("accessory"));
        if(tag.hasKey("sound"))
            sound.set(tag.getString("sound"));
        if(tag.hasKey("neon"))
            neon.set(tag.getString("neon"));
    }
}