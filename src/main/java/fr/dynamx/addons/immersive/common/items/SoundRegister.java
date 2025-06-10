package fr.dynamx.addons.immersive.common.items;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ImmersiveAddon.ID)
public class SoundRegister {
    public static SoundEvent SPRAY;

    @SubscribeEvent
    public static void onRegisterSounds(RegistryEvent.Register<SoundEvent> event) {
        SPRAY = new SoundEvent(new ResourceLocation(ImmersiveAddon.ID, "spray"));
        SPRAY.setRegistryName(new ResourceLocation(ImmersiveAddon.ID, "spray"));
        event.getRegistry().register(SPRAY);
    }
}