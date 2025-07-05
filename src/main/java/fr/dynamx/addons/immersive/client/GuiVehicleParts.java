package fr.dynamx.addons.immersive.client;

import fr.dynamx.addons.immersive.common.modules.VehicleCustomizationModule;
import fr.dynamx.addons.immersive.common.ContainerVehicleParts;
import fr.dynamx.common.entities.BaseVehicleEntity;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiVehicleParts extends GuiContainer {

    // Use a custom texture for the parts inventory
    private static final ResourceLocation TEXTURE = new ResourceLocation("dynamx_immersive", "textures/gui/vehicle_parts.png");
    private final InventoryPlayer playerInv;

    public GuiVehicleParts(InventoryPlayer playerInv, BaseVehicleEntity<?> entity, VehicleCustomizationModule module) {
        super(new ContainerVehicleParts(playerInv, entity, module));
        this.playerInv = playerInv;
        xSize = 176;
        ySize = 166;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1f,1f,1f,1f);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = "Vehicle Parts";
        this.fontRenderer.drawString(name, 8, 6, 4210752);
        this.fontRenderer.drawString(playerInv.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
