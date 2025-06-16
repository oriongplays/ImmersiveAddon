package fr.dynamx.addons.immersive.common.helpers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Represents vehicle upgrade properties.
 */
public class VehicleLevelConfig {
    public String model = "";
    public int emptyMass = -1;
    public float dragCoefficient = -1f;

    public static VehicleLevelConfig loadDefault(String type) {
        try (InputStream in = VehicleLevelConfig.class.getClassLoader().getResourceAsStream("assets/dynamx_immersive/level1.json")) {
            if (in == null) {
                return new VehicleLevelConfig();
            }
            Type mapType = new TypeToken<Map<String, VehicleLevelConfig>>() {}.getType();
            Map<String, VehicleLevelConfig> map = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), mapType);
            VehicleLevelConfig cfg = map.get(type);
            if (cfg == null) {
                return new VehicleLevelConfig();
            }
            return cfg;
        } catch (Exception e) {
            return new VehicleLevelConfig();
        }
    }

    public static boolean typeExists(String type) {
        try (InputStream in = VehicleLevelConfig.class.getClassLoader().getResourceAsStream("assets/dynamx_immersive/level1.json")) {
            if (in == null) {
                return false;
            }
            Type mapType = new TypeToken<Map<String, VehicleLevelConfig>>() {}.getType();
            Map<String, VehicleLevelConfig> map = new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), mapType);
            return map.containsKey(type);
        } catch (Exception e) {
            return false;
        }
    }
}
