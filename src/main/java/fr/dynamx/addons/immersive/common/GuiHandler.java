package fr.dynamx.addons.immersive.common;

import fr.dynamx.addons.immersive.client.GuiVehicleParts;
import fr.dynamx.addons.immersive.client.GuiVehicleStorage;
import fr.dynamx.addons.immersive.common.modules.VehicleCustomizationModule;
import fr.dynamx.addons.immersive.common.modules.VehicleStorageModule;
import fr.dynamx.addons.immersive.common.ContainerVehicleStorage;
import fr.dynamx.addons.immersive.common.ContainerVehicleParts;
import fr.dynamx.common.entities.BaseVehicleEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
    public static final int VEHICLE_PARTS = 0;
    public static final int VEHICLE_STORAGE = 1;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if(ID == VEHICLE_PARTS) {
            BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) world.getEntityByID(x);
            if(vehicle != null) {
                VehicleCustomizationModule module = vehicle.getModuleByType(VehicleCustomizationModule.class);
                if(module != null)
                    return new ContainerVehicleParts(player.inventory, vehicle, module);
            }
        }
                if(ID == VEHICLE_STORAGE) {
            BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) world.getEntityByID(x);
            if(vehicle != null) {
                VehicleStorageModule module = vehicle.getModuleByType(VehicleStorageModule.class);
                if(module != null)
                    return new ContainerVehicleStorage(player.inventory, vehicle, module);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if(ID == VEHICLE_PARTS) {
            BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) world.getEntityByID(x);
            if(vehicle != null) {
                VehicleCustomizationModule module = vehicle.getModuleByType(VehicleCustomizationModule.class);
                if(module != null)
                    return new GuiVehicleParts(player.inventory, vehicle, module);
            }
        }
                if(ID == VEHICLE_STORAGE) {
            BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) world.getEntityByID(x);
            if(vehicle != null) {
                VehicleStorageModule module = vehicle.getModuleByType(VehicleStorageModule.class);
                if(module != null)
                    return new GuiVehicleStorage(player.inventory, vehicle, module);
            }
        }
        return null;
    }
}