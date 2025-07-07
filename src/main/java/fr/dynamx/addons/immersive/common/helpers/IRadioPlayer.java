package fr.dynamx.addons.immersive.common.helpers;

import java.net.URL;

/**
 * Generic radio player interface used by the radio module. Different
 * implementations can play audio using various backends.
 */
public interface IRadioPlayer {
    /**
     * Start playing the provided radio stream.
     * @param radioUrl URL to the audio stream or page providing audio
     */
    void playRadio(URL radioUrl);

    /**
     * Stop any playing stream.
     */
    void stopRadio();

    /**
     * Set the playback volume. Range is 0.0 to 1.0.
     * @param volume desired gain level
     */
    void setGain(float volume);
}