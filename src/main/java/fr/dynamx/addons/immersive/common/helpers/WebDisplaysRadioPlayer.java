package fr.dynamx.addons.immersive.common.helpers;

import net.minecraftforge.fml.common.Loader;
import fr.dynamx.addons.immersive.ImmersiveAddon;

import java.lang.reflect.Method;
import java.net.URL;

/**
 * Radio player that uses the WebDisplays mod to play the stream inside
 * an invisible browser. All calls are done through reflection so that the
 * mod remains optional at runtime.
 */
public class WebDisplaysRadioPlayer implements IRadioPlayer {
    private Object browser;
    private Object screen;

    private boolean available() {
        return Loader.isModLoaded("webdisplays") || Loader.isModLoaded("mcef");
    }

    @Override
    public void playRadio(URL radioUrl) {
        if(!available()) {
            ImmersiveAddon.LOGGER.warn("[WebDisplays] playRadio called but mod missing");
            return;
        }
        stopRadio();
        ImmersiveAddon.LOGGER.info("[WebDisplays] playRadio {}", radioUrl);
        try {
            if(Loader.isModLoaded("webdisplays")) {
                try {
                    Class<?> api = Class.forName("montoyo.webdisplays.api.ScreenHelper");
                    Method createScreen;
                    try {
                        createScreen = api.getMethod("createScreen", String.class, int.class, int.class, boolean.class);
                        screen = createScreen.invoke(null, radioUrl.toString(), 1, 1, false);
                        browser = screen; // treat as browser for volume control
                        return;
                    } catch (NoSuchMethodException nsme) {
                        Method create = api.getMethod("createBrowser", String.class, int.class, int.class, boolean.class);
                        browser = create.invoke(null, radioUrl.toString(), 1, 1, false);
                        return;
                    }
                } catch(ClassNotFoundException cnf) {
                    ImmersiveAddon.LOGGER.debug("[WebDisplays] ScreenHelper not found, falling back to MCEF API");
                }
            }

            // Fallback using MCEF directly
            Class<?> api = Class.forName("montoyo.mcef.MCEFApi");
            Method get = api.getMethod("getAPI");
            Object inst = get.invoke(null);
            Method create = inst.getClass().getMethod("createBrowser", String.class, boolean.class);
            browser = create.invoke(inst, radioUrl.toString(), false);
        } catch (Throwable t) {
            ImmersiveAddon.LOGGER.error("[WebDisplays] Failed to start radio", t);
        }
    }

    @Override
    public void stopRadio() {
        ImmersiveAddon.LOGGER.info("[WebDisplays] stopRadio");
        if(browser != null) {
            try {
                Method close = browser.getClass().getMethod("close");
                close.invoke(browser);
            } catch (Throwable t) {
                ImmersiveAddon.LOGGER.error("[WebDisplays] Error closing browser", t);
            }
            browser = null;
        }
        if(screen != null) {
            try {
                Method close = screen.getClass().getMethod("close");
                close.invoke(screen);
            } catch (Throwable ignored) {
                // some versions may not expose close on the screen
            }
            screen = null;
        }
    }

    @Override
    public void setGain(float volume) {
        Object obj = browser != null ? browser : screen;
        if(obj == null)
            return;
        try {
            Method js = obj.getClass().getMethod("executeJavaScript", String.class);
            js.invoke(obj, "try{var a=document.querySelector('video, audio');if(a)a.volume=" + volume + ";}catch(e){}");
        } catch (Throwable t) {
            ImmersiveAddon.LOGGER.error("[WebDisplays] Failed to set volume", t);
        }
    }
}