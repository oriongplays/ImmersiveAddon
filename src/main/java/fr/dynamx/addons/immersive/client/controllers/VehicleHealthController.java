package fr.dynamx.addons.immersive.client.controllers;

import fr.aym.acsguis.component.GuiComponent;
import fr.aym.acsguis.component.panel.GuiPanel;
import fr.aym.acsguis.component.textarea.UpdatableGuiLabel;
import fr.dynamx.addons.basics.client.BasicsAddonController;
import fr.dynamx.addons.immersive.common.modules.DamageModule;
import fr.dynamx.api.entities.modules.IVehicleController;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class VehicleHealthController implements IVehicleController {
    private final DamageModule damageModule;

    public VehicleHealthController(DamageModule damageModule) {
        this.damageModule = damageModule;
    }

    @Override
    public void update() {
        // Vehicle health is updated through synchronized variables; nothing to do client-side.
    }

    @Override
    public GuiComponent createHud() {
        GuiPanel panel = new GuiPanel();
        panel.setCssClass("speed_pane");
        panel.setCssId("vehicle_health_panel");

        UpdatableGuiLabel label = new UpdatableGuiLabel("%s",
                (UpdatableGuiLabel.LabelValueFunction) value -> value.set(getFormattedHealth()));
        label.setCssId("vehicle_health");
        panel.add(label);

        return panel;
    }

    private String getFormattedHealth() {
        float health = Math.max(0.0F, Math.min(100.0F, 100.0F - damageModule.getDamage()));
        return String.format(Locale.ROOT, "%d%%", Math.round(health));
    }

    @Override
    public List<ResourceLocation> getHudCssStyles() {
        return Collections.singletonList(BasicsAddonController.STYLE);
    }
}