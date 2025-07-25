package fr.dynamx.addons.immersive.proxy;

import fr.dynamx.addons.immersive.common.items.ItemsRegister;
import fr.dynamx.addons.immersive.client.KeyVehicleInventory;
import fr.dynamx.addons.immersive.client.KeyRadio;
import fr.dynamx.addons.immersive.client.KeyVehicleHealth;
import fr.dynamx.addons.immersive.client.KeyWheelHealth;
import fr.dynamx.addons.immersive.client.VehiclePartRenderHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(ItemsRegister.INSTANCE);
        KeyVehicleInventory.register();
        MinecraftForge.EVENT_BUS.register(new VehiclePartRenderHandler());
        KeyRadio.register();
        KeyVehicleHealth.register();
        KeyWheelHealth.register();
    }
}
