package fr.dynamx.addons.immersive.common;

import fr.dynamx.addons.immersive.client.GuiVehicleParts;
import fr.dynamx.addons.immersive.client.GuiVehicleStorage;
import fr.dynamx.addons.immersive.client.GuiVehicleAssets;
import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.addons.immersive.common.modules.VehicleCustomizationModule;
import fr.dynamx.addons.immersive.common.modules.VehicleStorageModule;
import fr.dynamx.addons.immersive.common.modules.VehicleAssetsModule;
import fr.dynamx.addons.immersive.common.ContainerVehicleStorage;
import fr.dynamx.addons.immersive.common.ContainerVehicleParts;
import fr.dynamx.addons.immersive.common.ContainerVehicleAssets;
import fr.dynamx.common.entities.BaseVehicleEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraft.nbt.NBTTagCompound;
import java.util.UUID;

public class GuiHandler implements IGuiHandler {
    public static final int VEHICLE_PARTS = 0;
    public static final int VEHICLE_STORAGE = 1;
    public static final int VEHICLE_ASSETS = 2;

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
        if(ID == VEHICLE_ASSETS) {
            BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) world.getEntityByID(x);
            if(vehicle != null) {
                VehicleAssetsModule module = vehicle.getModuleByType(VehicleAssetsModule.class);
                if(module != null) {
                    String reason = checkAssetAccess(vehicle, player);
                    if(reason == null) {
                        ImmersiveAddon.LOGGER.debug("Opening assets container for vehicle {}", x);
                        return new ContainerVehicleAssets(player.inventory, vehicle, module);
                    }
                    ImmersiveAddon.LOGGER.debug("Denied assets container for vehicle {}: {}", x, reason);
                }
                else {
                    ImmersiveAddon.LOGGER.debug("Vehicle {} missing VehicleAssetsModule", x);
                }
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
        if(ID == VEHICLE_ASSETS) {
            BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) world.getEntityByID(x);
            if(vehicle != null) {
                VehicleAssetsModule module = vehicle.getModuleByType(VehicleAssetsModule.class);
                if(module != null)
                    return new GuiVehicleAssets(player.inventory, vehicle, module);
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

    public static String checkAssetAccess(BaseVehicleEntity<?> vehicle, EntityPlayer player) {
        if(!(player instanceof EntityPlayerMP) || ((EntityPlayerMP) player).interactionManager.getGameType() != GameType.ADVENTURE)
            return null;
        fr.dynamx.common.entities.modules.SeatsModule seats = vehicle.getModuleByType(fr.dynamx.common.entities.modules.SeatsModule.class);
        if(seats == null || seats.getControllingPassenger() != player)
            return null;
        NBTTagCompound data = vehicle.serializeNBT();
        String ownerStr = data.getString("garage.owner");
        if(ownerStr.isEmpty())
            return null;
        try {
            UUID owner = UUID.fromString(ownerStr);
            if(!owner.equals(player.getUniqueID()))
                return null;
        } catch(IllegalArgumentException e) {
            return null;
        }
        return null;
    }
}