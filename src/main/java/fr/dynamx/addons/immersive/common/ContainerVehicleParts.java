package fr.dynamx.addons.immersive.common;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.addons.immersive.common.items.ItemVehiclePart;
import fr.dynamx.addons.immersive.common.modules.VehicleCustomizationModule;
import fr.dynamx.common.entities.BaseVehicleEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ContainerVehicleParts extends Container {

    private final InventoryBasic inv = new InventoryBasic("Parts", false, 6);
    private final InventoryPlayer playerInv;
    private final VehicleCustomizationModule module;

    public ContainerVehicleParts(InventoryPlayer playerInv, BaseVehicleEntity<?> entity, VehicleCustomizationModule module) {
        this.playerInv = playerInv;
        this.module = module;
        loadFromModule();
        for(int i=0;i<6;i++) {
            final String slotId = getSlotName(i);
            this.addSlotToContainer(new Slot(inv, i, 8 + i*18, 20) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return stack.getItem() instanceof ItemVehiclePart && ((ItemVehiclePart) stack.getItem()).getSlotId().equals(slotId);
                }
            });
        }
        addPlayerSlots();
    }

    private void addPlayerSlots() {
        for(int row=0; row<3; row++) {
            for(int col=0; col<9; col++) {
                this.addSlotToContainer(new Slot(playerInv, col + row*9 + 9, 8 + col*18, 50 + row*18));
            }
        }
        for(int col=0; col<9; col++) {
            this.addSlotToContainer(new Slot(playerInv, col, 8 + col*18, 108));
        }
    }

    private static String getSlotName(int id) {
        switch (id) {
            case 0: return "front_bumper";
            case 1: return "rear_bumper";
            case 2: return "hood";
            case 3: return "spoiler";
            case 4: return "roof";
            case 5: return "side_skirt";
        }
        return "";
    }

    private void loadFromModule() {
        for(int i=0;i<6;i++) {
            String part = module.getPart(getSlotName(i));
            if(!part.isEmpty()) {
                Item item = Item.REGISTRY.getObject(new ResourceLocation(ImmersiveAddon.ID, part));
                if(item != null) {
                    inv.setInventorySlotContents(i, new ItemStack(item));
                }
            }
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if(!player.world.isRemote) {
            for(int i=0;i<6;i++) {
                ItemStack stack = inv.getStackInSlot(i);
                String name = stack.isEmpty() ? "" : stack.getItem().getRegistryName().getPath();
                module.setPart(getSlotName(i), name);
            }
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if(slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            stack = slotStack.copy();
            if(index < 6) {
                if(!mergeItemStack(slotStack, 6, this.inventorySlots.size(), true))
                    return ItemStack.EMPTY;
            } else {
                boolean moved = false;
                for(int i=0;i<6 && !moved;i++) {
                    Slot target = this.inventorySlots.get(i);
                    if(target.isItemValid(slotStack) && !target.getHasStack()) {
                        if(!mergeItemStack(slotStack, i, i+1, false))
                            return ItemStack.EMPTY;
                        moved = true;
                    }
                }
                if(!moved)
                    return ItemStack.EMPTY;
            }
            if(slotStack.isEmpty())
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();
        }
        return stack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
}