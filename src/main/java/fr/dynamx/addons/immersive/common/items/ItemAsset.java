package fr.dynamx.addons.immersive.common.items;

import fr.dynamx.common.items.DynamXItemRegistry;
import net.minecraft.item.Item;

/**
 * Generic asset item that can be installed on vehicles via the assets inventory.
 */
public class ItemAsset extends Item {
    public ItemAsset(String name) {
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(DynamXItemRegistry.objectTab);
        setMaxStackSize(1);
        ItemsRegister.INSTANCE.getItems().add(this);
    }
}