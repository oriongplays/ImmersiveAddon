package fr.dynamx.addons.immersive.common.modules;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.api.entities.modules.IPhysicsModule;
import fr.dynamx.api.network.sync.EntityVariable;
import fr.dynamx.api.network.sync.SynchronizationRules;
import fr.dynamx.api.network.sync.SynchronizedEntityVariable;
import fr.dynamx.common.entities.BaseVehicleEntity;
import fr.dynamx.common.physics.entities.AbstractEntityPhysicsHandler;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Stores custom asset identifiers installed on a vehicle.
 */
@SynchronizedEntityVariable.SynchronizedPhysicsModule(modid = ImmersiveAddon.ID)
public class VehicleAssetsModule implements IPhysicsModule<AbstractEntityPhysicsHandler<?, ?>> {
    private final BaseVehicleEntity<?> entity;

    @SynchronizedEntityVariable(name = "asset0")
    private final EntityVariable<String> asset0 = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "");
    @SynchronizedEntityVariable(name = "asset1")
    private final EntityVariable<String> asset1 = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "");
    @SynchronizedEntityVariable(name = "asset2")
    private final EntityVariable<String> asset2 = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "");
    @SynchronizedEntityVariable(name = "asset3")
    private final EntityVariable<String> asset3 = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "");
    @SynchronizedEntityVariable(name = "asset4")
    private final EntityVariable<String> asset4 = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "");
    @SynchronizedEntityVariable(name = "asset5")
    private final EntityVariable<String> asset5 = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "");
    @SynchronizedEntityVariable(name = "asset6")
    private final EntityVariable<String> asset6 = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "");
    @SynchronizedEntityVariable(name = "asset7")
    private final EntityVariable<String> asset7 = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "");
    @SynchronizedEntityVariable(name = "asset8")
    private final EntityVariable<String> asset8 = new EntityVariable<>(SynchronizationRules.PHYSICS_TO_SPECTATORS, "");

    public VehicleAssetsModule(BaseVehicleEntity<?> entity) {
        this.entity = entity;
    }

    public void setAsset(String slot, String name) {
        switch (slot) {
            case "asset0": asset0.set(name); break;
            case "asset1": asset1.set(name); break;
            case "asset2": asset2.set(name); break;
            case "asset3": asset3.set(name); break;
            case "asset4": asset4.set(name); break;
            case "asset5": asset5.set(name); break;
            case "asset6": asset6.set(name); break;
            case "asset7": asset7.set(name); break;
            case "asset8": asset8.set(name); break;
        }
    }

    public String getAsset(String slot) {
        switch (slot) {
            case "asset0": return asset0.get();
            case "asset1": return asset1.get();
            case "asset2": return asset2.get();
            case "asset3": return asset3.get();
            case "asset4": return asset4.get();
            case "asset5": return asset5.get();
            case "asset6": return asset6.get();
            case "asset7": return asset7.get();
            case "asset8": return asset8.get();
        }
        return "";
    }

    @Override
    public void initPhysicsEntity(AbstractEntityPhysicsHandler<?, ?> handler) {}

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setString("asset0", asset0.get());
        tag.setString("asset1", asset1.get());
        tag.setString("asset2", asset2.get());
        tag.setString("asset3", asset3.get());
        tag.setString("asset4", asset4.get());
        tag.setString("asset5", asset5.get());
        tag.setString("asset6", asset6.get());
        tag.setString("asset7", asset7.get());
        tag.setString("asset8", asset8.get());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        if(tag.hasKey("asset0")) asset0.set(tag.getString("asset0"));
        if(tag.hasKey("asset1")) asset1.set(tag.getString("asset1"));
        if(tag.hasKey("asset2")) asset2.set(tag.getString("asset2"));
        if(tag.hasKey("asset3")) asset3.set(tag.getString("asset3"));
        if(tag.hasKey("asset4")) asset4.set(tag.getString("asset4"));
        if(tag.hasKey("asset5")) asset5.set(tag.getString("asset5"));
        if(tag.hasKey("asset6")) asset6.set(tag.getString("asset6"));
        if(tag.hasKey("asset7")) asset7.set(tag.getString("asset7"));
        if(tag.hasKey("asset8")) asset8.set(tag.getString("asset8"));
    }
}