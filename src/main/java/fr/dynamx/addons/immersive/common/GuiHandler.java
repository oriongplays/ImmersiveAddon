package fr.dynamx.addons.immersive.common;

import fr.dynamx.addons.immersive.client.GuiVehicleParts;
import fr.dynamx.addons.immersive.client.GuiVehicleStorage;
import fr.dynamx.addons.immersive.client.GuiVehicleAssets;
import fr.dynamx.addons.immersive.common.modules.VehicleCustomizationModule;
import fr.dynamx.addons.immersive.common.modules.VehicleStorageModule;
import fr.dynamx.addons.immersive.common.modules.VehicleAssetsModule;
import fr.dynamx.addons.immersive.common.ContainerVehicleStorage;
import fr.dynamx.addons.immersive.common.ContainerVehicleParts;
import fr.dynamx.addons.immersive.common.ContainerVehicleAssets;
import fr.dynamx.common.entities.BaseVehicleEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraft.world.GameType;

public class GuiHandler implements IGuiHandler {
    public static final int VEHICLE_PARTS = 0;
    public static final int VEHICLE_STORAGE = 1;
    public static final int VEHICLE_ASSETS = 2;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if(ID == VEHICLE_STORAGE) {
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
                if(ID == VEHICLE_ASSETS) {
            BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) world.getEntityByID(x);
            if(vehicle != null && player instanceof EntityPlayerMP &&
               ((EntityPlayerMP) player).interactionManager.getGameType() == GameType.ADVENTURE) {
                VehicleAssetsModule module = vehicle.getModuleByType(VehicleAssetsModule.class);
                if(module != null)
                    return new ContainerVehicleAssets(player.inventory, vehicle, module);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if(ID == VEHICLE_STORAGE) {
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
                if(ID == VEHICLE_ASSETS) {
            BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) world.getEntityByID(x);
            if(vehicle != null) {
                VehicleAssetsModule module = vehicle.getModuleByType(VehicleAssetsModule.class);
                if(module != null)
                    return new GuiVehicleAssets(player.inventory, vehicle, module);
            }
        }
        return null;
    }
}