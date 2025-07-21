package fr.dynamx.addons.immersive.client;

import fr.dynamx.addons.immersive.ImmersiveAddonConfig;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyWheelHealth {
    public static KeyBinding SHOW_WHEEL_HEALTH;

    public static void register() {
        SHOW_WHEEL_HEALTH = new KeyBinding("key.wheel_health", ImmersiveAddonConfig.keyShowWheelHealth, "key.categories.gameplay");
        ClientRegistry.registerKeyBinding(SHOW_WHEEL_HEALTH);
    }
}