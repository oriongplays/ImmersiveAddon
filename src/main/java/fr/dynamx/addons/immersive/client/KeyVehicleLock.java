package fr.dynamx.addons.immersive.client;

import fr.dynamx.addons.immersive.ImmersiveAddonConfig;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyVehicleLock {
    public static KeyBinding TOGGLE_LOCK;

    private KeyVehicleLock() {
    }

    public static void register() {
        TOGGLE_LOCK = new KeyBinding("key.toggle_vehicle_lock", ImmersiveAddonConfig.keyToggleLock, "key.categories.gameplay");
        ClientRegistry.registerKeyBinding(TOGGLE_LOCK);
    }
}