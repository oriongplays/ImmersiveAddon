package fr.dynamx.addons.immersive.common.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.common.physics.entities.parts.engine.Engine;
import fr.dynamx.common.physics.entities.parts.engine.GearBox;
import fr.dynamx.common.entities.modules.engines.CarEngineModule;
import fr.dynamx.common.contentpack.type.vehicle.CarEngineInfo;
import fr.dynamx.common.contentpack.type.vehicle.BaseEngineInfo;
import fr.dynamx.common.physics.entities.modules.EnginePhysicsHandler;
import com.jme3.math.Vector3f;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Applies engine parameters based on configured levels.
 */
public class EngineTuningHelper {

    /**
     * Loads the configuration for the given level from {@code engine_levels.json}.
     * If the level is not found returns an empty configuration.
     */
    public static EngineLevelConfig loadLevel(int level) {
        try (InputStream in = EngineTuningHelper.class.getClassLoader()
                .getResourceAsStream("assets/dynamx_immersive/engine_levels.json")) {
            if (in == null)
                return new EngineLevelConfig();
            JsonObject root = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
            JsonObject obj = root.getAsJsonObject(String.valueOf(level));
            if (obj == null)
                return new EngineLevelConfig();
            EngineLevelConfig cfg = new EngineLevelConfig();
            if (obj.has("Power")) cfg.power = obj.get("Power").getAsFloat();
            if (obj.has("MaxRPM")) cfg.maxRPM = obj.get("MaxRPM").getAsFloat();
            if (obj.has("Braking")) cfg.braking = obj.get("Braking").getAsFloat();
            for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
                if (e.getKey().startsWith("Point_")) {
                    String[] vals = e.getValue().getAsJsonObject().get("RPMPower").getAsString().split(" ");
                    cfg.points.add(new Vector3f(Float.parseFloat(vals[0]), Float.parseFloat(vals[1]), 0));
                } else if (e.getKey().startsWith("Gear_")) {
                    JsonObject g = e.getValue().getAsJsonObject();
                    String[] sr = g.get("SpeedRange").getAsString().split(" ");
                    String[] rr = g.get("RPMRange").getAsString().split(" ");
                    EngineLevelConfig.Gear gear = new EngineLevelConfig.Gear();
                    gear.minSpeed = Integer.parseInt(sr[0]);
                    gear.maxSpeed = Integer.parseInt(sr[1]);
                    gear.minRPM = Float.parseFloat(rr[0]);
                    gear.maxRPM = Float.parseFloat(rr[1]);
                    cfg.gears.add(gear);
                }
            }
            return cfg;
        } catch (Exception e) {
            return new EngineLevelConfig();
        }
    }

    /**
     * Returns true if the engine level exists in the configuration file.
     */
    public static boolean levelExists(int level) {
        try (InputStream in = EngineTuningHelper.class.getClassLoader()
                .getResourceAsStream("assets/dynamx_immersive/engine_levels.json")) {
            if (in == null)
                return false;
            JsonObject root = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
            return root.has(String.valueOf(level));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Applies tuning values to the given engine.
     */
    public static void applyTuning(Engine engine, CarEngineModule module, int level, float power, float maxRPM, float braking) {
        if (module == null || module.getPhysicsHandler() == null)
            return;
        EnginePhysicsHandler handler = module.getPhysicsHandler();
        if (engine == null)
            engine = handler.getEngine();
        GearBox box = handler.getGearBox();
        CarEngineInfo info = module.getEngineInfo();
        EngineLevelConfig defaults = loadLevel(level);
        float p = power >= 0 ? power : defaults.power;
        float rpm = maxRPM >= 0 ? maxRPM : defaults.maxRPM;
        float brk = braking >= 0 ? braking : defaults.braking;
        if (!defaults.points.isEmpty() && rpm > 0) {
            // ensure the last power point matches the maximum RPM
            Vector3f last = defaults.points.get(defaults.points.size() - 1);
            if (last.x != rpm) {
                defaults.points.set(defaults.points.size() - 1,
                        new Vector3f(rpm, last.y, last.z));
            }
        }
        ImmersiveAddon.LOGGER.info("Applying engine tuning level {} -> power={} maxRPM={} braking={}",
                level, p, rpm, brk);
        if (engine != null) {
            if (p > 0) engine.setPower(p);
            if (rpm > 0) engine.setMaxRevs(rpm);
            if (brk > 0) engine.setBraking(brk);
        }
        if (info != null) {
            try {
                java.lang.reflect.Field f = BaseEngineInfo.class.getDeclaredField("power");
                f.setAccessible(true); f.setFloat(info, p);
                f = BaseEngineInfo.class.getDeclaredField("maxRevs");
                f.setAccessible(true); f.setFloat(info, rpm);
                f = BaseEngineInfo.class.getDeclaredField("braking");
                f.setAccessible(true); f.setFloat(info, brk);
            } catch (Exception ignored) {}
            if (!defaults.points.isEmpty()) {
                info.points.clear();
                info.points.addAll(defaults.points);
            }
        }
        if (box != null && !defaults.gears.isEmpty()) {
                        int gearCount = box.getGearCount();
            for (int i = 0; i < defaults.gears.size(); i++) {
                    if (i >= gearCount) {
                    ImmersiveAddon.LOGGER.warn("Gear {} defined in config but gearbox supports only {} gears", i, gearCount);
                    break;
                }
                EngineLevelConfig.Gear g = defaults.gears.get(i);
                box.setGear(i, g.minSpeed, g.maxSpeed, g.minRPM, g.maxRPM);
            }
        }
        if (engine != null) {
            // Ensure the physics handler keeps using the same engine instance
            // but update the module's info reference so subsequent reloads use
            // the new parameters.
            try {
                java.lang.reflect.Field ef = CarEngineModule.class.getDeclaredField("engineInfo");
                ef.setAccessible(true);
                ef.set(module, info);
            } catch (Exception ignored) {
            }
            if (handler != null) {
                handler.setEngine(engine);
            }
        }
    }
    
        /**
     * Selects the desired gear on the provided engine's gearbox.
     * The value is clamped to the available range and out of bounds
     * requests are logged.
     */
    public static void selectGear(CarEngineModule module, int gear) {
        if (module == null || module.getPhysicsHandler() == null)
            return;

        EnginePhysicsHandler handler = module.getPhysicsHandler();
        GearBox box = handler.getGearBox();
        if (box == null)
            return;

        int gearCount = box.getGearCount();
        int clamped = Math.max(0, Math.min(gear, gearCount - 1));
        if (gear != clamped) {
            ImmersiveAddon.LOGGER.warn("Requested gear {} outside range [0, {}], clamping to {}", gear, gearCount - 1, clamped);
        }

        box.setActiveGearNum(clamped);
        handler.syncActiveGear(clamped);
    }


    /** Configuration values for a vehicle engine level. */
    public static class EngineLevelConfig {
        /** Gear ratios and ranges. */
        public static class Gear {
            public int minSpeed;
            public int maxSpeed;
            public float minRPM;
            public float maxRPM;
        }

        public float power = -1f;
        public float maxRPM = -1f;
        public float braking = -1f;
        public java.util.List<Vector3f> points = new java.util.ArrayList<>();
        public java.util.List<Gear> gears = new java.util.ArrayList<>();
    }
}