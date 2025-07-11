package fr.dynamx.addons.immersive.common.helpers;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.Method;
import java.net.URL;

/**
 * Radio player using the MCEF browser. It creates a tiny hidden browser for
 * each stream so players within 16 blocks can hear the audio. If the MCEF mod
 * isn't installed radio playback is disabled.
 */
public class MCEFRadioPlayer implements IRadioPlayer {
    private Object browser;

    /**
     * @return {@code true} if the MCEF mod is loaded on the client.
     */
    private boolean available() {
        return Loader.isModLoaded("mcef");
    }

    @Override
    public void playRadio(URL radioUrl) {
        if (!available()) {
            ImmersiveAddon.LOGGER.warn("[MCEF] mod not installed; radio disabled");
            return;
        }
        stopRadio();
        ImmersiveAddon.LOGGER.info("[MCEF] playRadio {}", radioUrl);
        try {
            Class<?> api = Class.forName("net.montoyo.mcef.api.MCEFApi");
            Method get = api.getMethod("getAPI");
            Object inst = get.invoke(null);
            Method create = inst.getClass().getMethod("createBrowser", String.class, boolean.class);
            browser = create.invoke(inst, radioUrl.toString(), false);
        } catch (Throwable t) {
            ImmersiveAddon.LOGGER.error("[MCEF] Failed to start radio", t);
        }
    }

    @Override
    public void stopRadio() {
        ImmersiveAddon.LOGGER.info("[MCEF] stopRadio");
        if (browser != null) {
            Object obj = browser;
            Runnable closer = () -> {
                try {
                    Method close = obj.getClass().getMethod("close");
                    close.invoke(obj);
                } catch (Throwable t) {
                    ImmersiveAddon.LOGGER.error("[MCEF] Error closing browser", t);
                }
            };
            if (net.minecraftforge.fml.common.FMLCommonHandler.instance().getSide().isClient()) {
                net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(closer);
            } else {
                closer.run();
            }
            browser = null;
        }
    }

    @Override
    public void setGain(float volume) {
        Object obj = browser;
        if(obj == null)
            return;
        try {
            Method js;
            try {
                // Newer MCEF versions use executeJavaScript(String, String, int)
                js = obj.getClass().getMethod("executeJavaScript", String.class, String.class, int.class);
                js.invoke(obj,
                        "try{var a=document.querySelector('video, audio');if(a)a.volume=" + volume + ";}catch(e){}",
                        "about:blank", 0);
            } catch (NoSuchMethodException ex) {
                // Older versions expose a single-argument method
                js = obj.getClass().getMethod("executeJavaScript", String.class);
                js.invoke(obj,
                        "try{var a=document.querySelector('video, audio');if(a)a.volume=" + volume + ";}catch(e){}");
            }
        } catch (Throwable t) {
            ImmersiveAddon.LOGGER.error("[MCEF] Failed to set volume", t);
        }
    }
}