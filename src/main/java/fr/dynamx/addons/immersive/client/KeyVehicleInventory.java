package fr.dynamx.addons.immersive.client;

import fr.dynamx.addons.immersive.ImmersiveAddonConfig;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyVehicleInventory {
    public static KeyBinding OPEN_INVENTORY;

    public static void register() {
        OPEN_INVENTORY = new KeyBinding("key.vehicle_inventory", ImmersiveAddonConfig.keyOpenParts, "key.categories.gameplay");
        ClientRegistry.registerKeyBinding(OPEN_INVENTORY);
    }
}