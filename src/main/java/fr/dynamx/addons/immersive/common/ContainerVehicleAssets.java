package fr.dynamx.addons.immersive.common;

import fr.dynamx.addons.immersive.common.items.ItemAsset;
import fr.dynamx.addons.immersive.common.modules.VehicleAssetsModule;
import fr.dynamx.common.entities.BaseVehicleEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import fr.dynamx.addons.immersive.ImmersiveAddon;
import net.minecraftforge.common.util.Constants;
import net.minecraft.world.GameType;

public class ContainerVehicleAssets extends Container {
    private final InventoryBasic inv = new InventoryBasic("Assets", false, 9);
    private final InventoryPlayer playerInv;
    private final VehicleAssetsModule module;
    private final BaseVehicleEntity<?> entity;

    public ContainerVehicleAssets(InventoryPlayer playerInv, BaseVehicleEntity<?> entity, VehicleAssetsModule module) {
        this.playerInv = playerInv;
        this.module = module;
        this.entity = entity;
        loadFromModule();
        for(int i=0;i<9;i++) {
            final int slotIndex = i;
            this.addSlotToContainer(new Slot(inv, i, 8 + i*18, 20) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return stack.getItem() instanceof ItemAsset;
                }

                @Override
                public void onSlotChanged() {
                    super.onSlotChanged();
                    if(!playerInv.player.world.isRemote) {
                        updateModuleSlot(slotIndex);
                    }
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
        return "asset" + id;
    }

    private void loadFromModule() {
        for(int i = 0; i < 9; i++) {
            String part = module.getAsset(getSlotName(i));
            if(!part.isEmpty()) {
                String base = part;
                String lore = "";
                int idx = part.lastIndexOf('_');
                if(idx > 0) {
                    base = part.substring(0, idx);
                    lore = part.substring(idx + 1);
                }
                Item item = Item.REGISTRY.getObject(new ResourceLocation(ImmersiveAddon.ID, base));
                if(item != null) {
                    ItemStack stack = new ItemStack(item);
                    if(!lore.isEmpty()) {
                        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
                        NBTTagCompound display = tag.hasKey("display", Constants.NBT.TAG_COMPOUND) ? tag.getCompoundTag("display") : new NBTTagCompound();
                        NBTTagList list = new NBTTagList();
                        list.appendTag(new NBTTagString(lore));
                        display.setTag("Lore", list);
                        tag.setTag("display", display);
                        stack.setTagCompound(tag);
                    }
                    inv.setInventorySlotContents(i, stack);
                }
            }
        }
    }

    private static String stackToValue(ItemStack stack) {
        if(stack.isEmpty())
            return "";
        String name = stack.getItem().getRegistryName().getPath();
        String lore = "";
        if(stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            if(tag.hasKey("display", Constants.NBT.TAG_COMPOUND)) {
                NBTTagCompound display = tag.getCompoundTag("display");
                if(display.hasKey("Lore", Constants.NBT.TAG_LIST)) {
                    NBTTagList list = display.getTagList("Lore", Constants.NBT.TAG_STRING);
                    if(list.tagCount() > 0) {
                        lore = list.getStringTagAt(0);
                    }
                }
            }
        }
        return lore.isEmpty() ? name : name + "_" + lore;
    }

    private void updateModuleSlot(int slot) {
        ItemStack stack = inv.getStackInSlot(slot);
        module.setAsset(getSlotName(slot), stackToValue(stack));
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if(!player.world.isRemote) {
            for(int i=0;i<9;i++) {
                updateModuleSlot(i);
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
            if(index < 9) {
                if(!mergeItemStack(slotStack, 9, this.inventorySlots.size(), true))
                    return ItemStack.EMPTY;
            } else {
                boolean moved = false;
                for(int i=0;i<9 && !moved;i++) {
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
        if (entity == null || playerIn == null) {
            return false;
        }

        NBTTagCompound tag = entity.getEntityData();
        if (!tag.hasKey("garage", Constants.NBT.TAG_COMPOUND)) {
            return false;
        }

        NBTTagCompound garage = tag.getCompoundTag("garage");
        String ownerId = garage.getString("owner");

        if (!playerIn.getUniqueID().toString().equals(ownerId)) {
            return false;
        }

        if (playerIn instanceof EntityPlayerMP) {
            return ((EntityPlayerMP) playerIn).interactionManager.getGameType() == GameType.ADVENTURE;
        }
        return true;
    }
}