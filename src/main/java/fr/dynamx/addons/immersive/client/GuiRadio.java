package fr.dynamx.addons.immersive.client;

import fr.dynamx.addons.immersive.common.helpers.ConfigReader;
import fr.dynamx.addons.immersive.common.helpers.RadioFrequency;
import fr.dynamx.addons.immersive.common.modules.RadioModule;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiRadio extends GuiScreen {
    private final RadioModule module;
    private int index;

    private GuiButton powerButton;
    private GuiButton leftButton;
    private GuiButton rightButton;

    public GuiRadio(RadioModule module) {
        this.module = module;
        this.index = module.getCurrentRadioIndex();
    }

    @Override
    public void initGui() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        this.buttonList.clear();
        powerButton = this.addButton(new GuiButton(0, centerX - 40, centerY - 10, 80, 20,
                module.isRadioOn() ? I18n.format("gui.on") : I18n.format("gui.off")));
        leftButton = this.addButton(new GuiButton(1, centerX - 60, centerY + 20, 20, 20, "<"));
        rightButton = this.addButton(new GuiButton(2, centerX + 40, centerY + 20, 20, 20, ">"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == powerButton) {
            module.setRadioOn(!module.isRadioOn());
            powerButton.displayString = module.isRadioOn() ? I18n.format("gui.on") : I18n.format("gui.off");
        } else if (button == leftButton) {
            if (ConfigReader.frequencies != null && !ConfigReader.frequencies.isEmpty()) {
                index = Math.max(0, index - 1);
            }
        } else if (button == rightButton) {
            if (ConfigReader.frequencies != null && !ConfigReader.frequencies.isEmpty()) {
                index = Math.min(ConfigReader.frequencies.size() - 1, index + 1);
            }
        }
    }

    @Override
    public void onGuiClosed() {
        module.setCurrentRadioIndex(index);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        if (ConfigReader.frequencies != null && !ConfigReader.frequencies.isEmpty()) {
            int idx = Math.min(Math.max(index, 0), ConfigReader.frequencies.size() - 1);
            RadioFrequency freq = ConfigReader.frequencies.get(idx);
            String text = I18n.format("%s MHz - %s", freq.getFrequency(), freq.getName());
            drawCenteredString(this.fontRenderer, text, width / 2, height / 2 - 40, 0xFFFFFF);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}