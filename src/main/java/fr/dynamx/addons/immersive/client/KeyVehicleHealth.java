package fr.dynamx.addons.immersive.client;

import fr.dynamx.addons.immersive.ImmersiveAddonConfig;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyVehicleHealth {
    public static KeyBinding SHOW_HEALTH;

    public static void register() {
        SHOW_HEALTH = new KeyBinding("key.vehicle_health", ImmersiveAddonConfig.keyShowHealth, "key.categories.gameplay");
        ClientRegistry.registerKeyBinding(SHOW_HEALTH);
    }
}