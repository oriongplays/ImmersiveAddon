package fr.dynamx.addons.immersive.common.helpers;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import javazoom.jl.player.Player;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple radio player that streams audio from a URL using JLayer's Player
 * Inspired by MinecraftTransportSimulator's radio implementation.
 */
public class RadioPlayer {
    private static final ExecutorService SERVICE = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder().setNameFormat("radio-player-%d").build());

    private Player player;

    /**
     * Plays the given radio stream asynchronously. Any previous stream is stopped first.
     */
    public void playRadio(URL radioUrl) {
        SERVICE.execute(() -> {
            stopRadioInternal();
            try {
                Player p = new Player(radioUrl.openStream());
                synchronized (this) {
                    player = p;
                }
                p.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Stops the current radio stream if one is active.
     */
    public void stopRadio() {
        SERVICE.execute(this::stopRadioInternal);
    }

    private void stopRadioInternal() {
        try {
            Player p;
            synchronized (this) {
                p = player;
                player = null;
            }
            if (p != null) {
                p.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the volume. Value is clamped between 0.0 and 1.0.
     */
    public void setGain(float volume) {
        // Volume control is unsupported in this simple implementation
    }
}