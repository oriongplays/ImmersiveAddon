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

    public void setAsset(int slot, String name) {
        switch (slot) {
            case 0: asset0.set(name); break;
            case 1: asset1.set(name); break;
            case 2: asset2.set(name); break;
            case 3: asset3.set(name); break;
            case 4: asset4.set(name); break;
            case 5: asset5.set(name); break;
            case 6: asset6.set(name); break;
            case 7: asset7.set(name); break;
            case 8: asset8.set(name); break;
        }
    }

    public String getAsset(int slot) {
        switch (slot) {
            case 0: return asset0.get();
            case 1: return asset1.get();
            case 2: return asset2.get();
            case 3: return asset3.get();
            case 4: return asset4.get();
            case 5: return asset5.get();
            case 6: return asset6.get();
            case 7: return asset7.get();
            case 8: return asset8.get();
        }
        return "";
    }

    public java.util.List<String> getAssets() {
        return java.util.Arrays.asList(asset0.get(), asset1.get(), asset2.get(), asset3.get(), asset4.get(), asset5.get(), asset6.get(), asset7.get(), asset8.get());
    }

    @Override
    public void initPhysicsEntity(AbstractEntityPhysicsHandler<?, ?> handler) {
    }

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