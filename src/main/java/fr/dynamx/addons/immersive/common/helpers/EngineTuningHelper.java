package fr.dynamx.addons.immersive.common.helpers;

import fr.dynamx.common.physics.entities.parts.engine.Engine;

public class EngineTuningHelper {
    private static final float[] POWER_MULTIPLIER = {1.0f, 1.1f, 1.2f, 1.3f, 1.5f};
    private static final float[] REVS_MULTIPLIER = {1.0f, 1.05f, 1.1f, 1.15f, 1.2f};

    public static void applyTuning(Engine engine, int level, float basePower, float baseMaxRevs) {
        if(engine == null)
            return;
        int idx = Math.max(1, Math.min(level, 5)) - 1;
        engine.setPower(basePower * POWER_MULTIPLIER[idx]);
        engine.setMaxRevs(baseMaxRevs * REVS_MULTIPLIER[idx]);
    }
}