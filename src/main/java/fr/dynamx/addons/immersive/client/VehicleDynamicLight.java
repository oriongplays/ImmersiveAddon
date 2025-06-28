package fr.dynamx.addons.immersive.client;

import atomicstryker.dynamiclights.client.IDynamicLightSource;
import net.minecraftforge.fml.common.Optional;
import fr.dynamx.addons.basics.common.modules.BasicsAddonModule;
import fr.dynamx.common.entities.BaseVehicleEntity;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * Simple Dynamic Lights implementation for vehicles.
 * Exists only client-side.
 */
@Optional.Interface(iface = "atomicstryker.dynamiclights.client.IDynamicLightSource", modid = "dynamiclights", striprefs = true)
public class VehicleDynamicLight implements IDynamicLightSource {

    private final BaseVehicleEntity<?> vehicle;
    private final BasicsAddonModule module;

    public VehicleDynamicLight(BaseVehicleEntity<?> vehicle, BasicsAddonModule module) {
        this.vehicle = vehicle;
        this.module = module;
    }

    @Override
    public int getLightLevel() {
        return (module.isHeadLightsOn() || module.isBeaconsOn()) ? 15 : 0;
    }

    @Override
    public Entity getAttachmentEntity() {
        return vehicle;
    }

    public double getPosX() {
        return vehicle.posX;
    }

    public double getPosY() {
        return vehicle.posY + vehicle.height * 0.5F;
    }

    public double getPosZ() {
        return vehicle.posZ;
    }

    public World getWorld() {
        return vehicle.world;
    }
}