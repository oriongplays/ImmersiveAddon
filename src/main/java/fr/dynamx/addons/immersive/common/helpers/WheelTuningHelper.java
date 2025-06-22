package fr.dynamx.addons.immersive.common.helpers;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Loads allowed wheel tuning ranges from {@code wheel_config.json}
 * and provides clamping/validation helpers.
 */
public class WheelTuningHelper {

    public static class WheelConfig {
        public float frictionMin = 1.5f;
        public float frictionMax = 3f;
        public float brakeMin = 200f;
        public float brakeMax = 1000f;
        public float restMin = 0.001f;
        public float restMax = 1.00f;
        public float stiffMin = 30f;
        public float stiffMax = 60f;
        public String[] particles = new String[0];
    }

    private static WheelConfig config;
    private static Set<String> particleSet;

    private static void load() {
        try (InputStream in = WheelTuningHelper.class.getClassLoader()
                .getResourceAsStream("assets/dynamx_immersive/wheel_config.json")) {
            if (in != null) {
                config = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), WheelConfig.class);
            }
        } catch (Exception ignored) {
        }
        if (config == null) {
            config = new WheelConfig();
        }
        particleSet = new HashSet<>(Arrays.asList(config.particles));
    }

    private static WheelConfig cfg() {
        if (config == null) {
            load();
        }
        return config;
    }

    public static float clampFriction(float f) {
        WheelConfig c = cfg();
        return Math.max(c.frictionMin, Math.min(f, c.frictionMax));
    }

    public static float clampBrake(float b) {
        WheelConfig c = cfg();
        return Math.max(c.brakeMin, Math.min(b, c.brakeMax));
    }

    public static float clampRest(float r) {
        WheelConfig c = cfg();
        return Math.max(c.restMin, Math.min(r, c.restMax));
    }

    public static float clampStiff(float s) {
        WheelConfig c = cfg();
        return Math.max(c.stiffMin, Math.min(s, c.stiffMax));
    }

    public static boolean isValidParticle(String p) {
        cfg();
        return particleSet.contains(p);
    }
}