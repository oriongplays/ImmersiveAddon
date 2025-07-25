package fr.dynamx.addons.immersive.client;

import fr.dynamx.addons.immersive.common.ContainerVehicleStorage;
import fr.dynamx.addons.immersive.common.modules.VehicleStorageModule;
import fr.dynamx.common.entities.BaseVehicleEntity;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiVehicleStorage extends GuiContainer {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
    private final InventoryPlayer playerInv;

    public GuiVehicleStorage(InventoryPlayer playerInv, BaseVehicleEntity<?> entity, VehicleStorageModule module) {
        super(new ContainerVehicleStorage(playerInv, entity, module));
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
        String name = "Vehicle Inventory";
        this.fontRenderer.drawString(name, 8, 6, 4210752);
        this.fontRenderer.drawString(playerInv.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);
    }
}