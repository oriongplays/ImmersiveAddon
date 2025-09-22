package fr.dynamx.addons.immersive.common.helpers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.dynamx.common.contentpack.type.vehicle.ModularVehicleInfo;
import fr.dynamx.common.entities.BaseVehicleEntity;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Reads optional per-vehicle overrides for physics values.
 */
public final class VehicleOverrideHelper {

    private static final VehicleOverride DEFAULT = new VehicleOverride();
    private static Map<String, VehicleOverride> overrides;

    private VehicleOverrideHelper() {
    }

    private static void ensureLoaded() {
        if (overrides != null) {
            return;
        }
        Map<String, VehicleOverride> loaded = Collections.emptyMap();
        try (InputStream in = VehicleOverrideHelper.class.getClassLoader()
                .getResourceAsStream("assets/dynamx_immersive/vehicle_overrides.json")) {
            if (in != null) {
                Type type = new TypeToken<Map<String, VehicleOverride>>() {
                }.getType();
                Map<String, VehicleOverride> parsed = new Gson()
                        .fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
                if (parsed != null) {
                    Map<String, VehicleOverride> normalized = new HashMap<>();
                    for (Map.Entry<String, VehicleOverride> entry : parsed.entrySet()) {
                        if (entry.getKey() == null || entry.getValue() == null) {
                            continue;
                        }
                        normalized.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue());
                    }
                    loaded = normalized;
                }
            }
        } catch (Exception ignored) {
        }
        overrides = loaded;
    }

    private static String normalizeName(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the overrides for the provided vehicle entity.
     */
    public static VehicleOverride getOverride(BaseVehicleEntity<?> entity) {
        if (entity == null || !(entity.getPackInfo() instanceof ModularVehicleInfo)) {
            return DEFAULT;
        }
        ModularVehicleInfo info = (ModularVehicleInfo) entity.getPackInfo();
        Object fullName = info.getFullName();
        if (fullName == null) {
            return DEFAULT;
        }
        return getOverride(fullName.toString());
    }

    /**
     * Returns the overrides for a given vehicle name.
     */
    public static VehicleOverride getOverride(String vehicleName) {
        ensureLoaded();
        if (overrides.isEmpty()) {
            return DEFAULT;
        }
        String normalized = normalizeName(vehicleName);
        VehicleOverride override = overrides.get(normalized);
        if (override != null) {
            return override;
        }
        int split = normalized.indexOf(':');
        if (split != -1) {
            override = overrides.get(normalized.substring(split + 1));
            if (override != null) {
                return override;
            }
        }
        return DEFAULT;
    }

    /** Holds optional overrides for a vehicle. */
    public static class VehicleOverride {
        /** Mass multiplier applied to the vehicle. */
        public Float Weight;
        /** Optional engine overrides. */
        public EngineOverride Engine;

        public float getWeightMultiplier() {
            return Weight != null && Weight > 0 ? Weight : 1f;
        }

        public float getEnginePowerMultiplier() {
            return Engine != null ? Engine.getPowerMultiplier() : 1f;
        }

        public float getEngineMaxRPMMultiplier() {
            return Engine != null ? Engine.getMaxRPMMultiplier() : 1f;
        }

        public float getEngineBrakingMultiplier() {
            return Engine != null ? Engine.getBrakingMultiplier() : 1f;
        }
    }

    /** Holds optional engine overrides for a vehicle. */
    public static class EngineOverride {
        public Float Power;
        public Float MaxRPM;
        public Float Braking;

        public float getPowerMultiplier() {
            return Power != null && Power > 0 ? Power : 1f;
        }

        public float getMaxRPMMultiplier() {
            return MaxRPM != null && MaxRPM > 0 ? MaxRPM : 1f;
        }

        public float getBrakingMultiplier() {
            return Braking != null && Braking > 0 ? Braking : 1f;
        }
    }
}