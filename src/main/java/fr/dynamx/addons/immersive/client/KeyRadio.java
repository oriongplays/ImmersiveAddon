package fr.dynamx.addons.immersive.client;

import fr.dynamx.addons.immersive.ImmersiveAddonConfig;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyRadio {
    public static KeyBinding OPEN_RADIO;

    public static void register() {
        OPEN_RADIO = new KeyBinding("key.open_radio", ImmersiveAddonConfig.keyOpenRadio, "key.categories.gameplay");
        ClientRegistry.registerKeyBinding(OPEN_RADIO);
    }
}