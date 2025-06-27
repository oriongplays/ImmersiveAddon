package fr.dynamx.addons.immersive.common.modules;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.api.entities.modules.IPhysicsModule;
import fr.dynamx.api.network.sync.SynchronizedEntityVariable;
import fr.dynamx.common.entities.BaseVehicleEntity;
import fr.dynamx.common.physics.entities.AbstractEntityPhysicsHandler;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

/**
 * Simple storage module providing an internal inventory for vehicles.
 * Works like a horse or donkey inventory and is not linked to any PartStorage.
 */
@SynchronizedEntityVariable.SynchronizedPhysicsModule(modid = ImmersiveAddon.ID)
public class VehicleStorageModule implements IPhysicsModule<AbstractEntityPhysicsHandler<?, ?>> {
    private final BaseVehicleEntity<?> entity;
    private final InventoryBasic inventory = new InventoryBasic("VehicleStorage", false, 27);
    private boolean dirty;

    public VehicleStorageModule(BaseVehicleEntity<?> entity) {
        this.entity = entity;
    }

    public IInventory getInventory() {
        return inventory;
    }

    /**
     * Marks the storage as modified so it will be saved with the vehicle.
     */
    public void markDirty() {
        dirty = true;
    }

    @Override
    public void initPhysicsEntity(AbstractEntityPhysicsHandler<?, ?> handler) {
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                NBTTagCompound item = new NBTTagCompound();
                item.setByte("Slot", (byte) i);
                stack.writeToNBT(item);
                list.appendTag(item);
            }
        }
        tag.setTag("vehicleInv", list);
        dirty = false;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        inventory.clear();
        if (tag.hasKey("vehicleInv", Constants.NBT.TAG_LIST)) {
            NBTTagList list = tag.getTagList("vehicleInv", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound item = list.getCompoundTagAt(i);
                int slot = item.getByte("Slot") & 255;
                if (slot >= 0 && slot < inventory.getSizeInventory()) {
                    inventory.setInventorySlotContents(slot, new ItemStack(item));
                }
            }
        }
        dirty = false;
    }
}