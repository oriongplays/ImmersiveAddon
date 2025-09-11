package fr.dynamx.addons.immersive.client;

import fr.dynamx.addons.immersive.ImmersiveAddonConfig;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyVehicleAssets {
    public static KeyBinding OPEN_ASSETS;

    public static void register() {
        OPEN_ASSETS = new KeyBinding("key.vehicle_assets", ImmersiveAddonConfig.keyOpenAssets, "key.categories.gameplay");
        ClientRegistry.registerKeyBinding(OPEN_ASSETS);
    }
}