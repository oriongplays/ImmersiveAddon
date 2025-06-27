package fr.dynamx.addons.immersive.common;

import fr.dynamx.addons.immersive.common.modules.VehicleStorageModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import fr.dynamx.common.entities.BaseVehicleEntity;

public class ContainerVehicleStorage extends Container {
    private final IInventory inv;
    private final InventoryPlayer playerInv;
    private final VehicleStorageModule module;

    public ContainerVehicleStorage(InventoryPlayer playerInv, BaseVehicleEntity<?> entity, VehicleStorageModule module) {
        this.inv = module.getInventory();
        this.playerInv = playerInv;
        this.module = module;
        this.inv.openInventory(playerInv.player);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlotToContainer(new Slot(inv, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }
        addPlayerSlots();
    }

    private void addPlayerSlots() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 86 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 144));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            stack = slotStack.copy();
            if (index < inv.getSizeInventory()) {
                if (!mergeItemStack(slotStack, inv.getSizeInventory(), this.inventorySlots.size(), true))
                    return ItemStack.EMPTY;
            } else if (!mergeItemStack(slotStack, 0, inv.getSizeInventory(), false)) {
                return ItemStack.EMPTY;
            }
            if (slotStack.isEmpty())
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();
        }
        return stack;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        inv.closeInventory(playerIn);
        module.markDirty();
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
}