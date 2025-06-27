package fr.dynamx.addons.immersive.common.items;

import fr.dynamx.common.items.DynamXItemRegistry;
import net.minecraft.item.Item;

public class ItemVehiclePart extends Item {
    private final String slotId;

    public ItemVehiclePart(String name, String slotId) {
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(DynamXItemRegistry.objectTab);
        setMaxStackSize(1);
        this.slotId = slotId;
        ItemsRegister.INSTANCE.getItems().add(this);
    }

    public String getSlotId() {
        return slotId;
    }
}