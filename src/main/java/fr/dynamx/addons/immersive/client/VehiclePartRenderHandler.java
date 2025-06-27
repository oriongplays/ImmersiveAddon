package fr.dynamx.addons.immersive.client;

import fr.dynamx.addons.immersive.ImmersiveAddon;
import fr.dynamx.addons.immersive.common.modules.VehicleCustomizationModule;
import fr.dynamx.api.events.DynamXModelRenderEvent;
import fr.dynamx.api.events.client.DynamXEntityRenderEvent;
import fr.dynamx.common.entities.BaseVehicleEntity;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = ImmersiveAddon.ID, value = Side.CLIENT)
@SideOnly(Side.CLIENT)
public class VehiclePartRenderHandler {

    private static BaseVehicleEntity<?> currentEntity;

    @SubscribeEvent
    public static void onEntityRender(DynamXEntityRenderEvent event) {
        if(event.getRenderType() == DynamXEntityRenderEvent.Type.ENTITY) {
            if(event.getEntity() instanceof BaseVehicleEntity) {
                currentEntity = (BaseVehicleEntity<?>) event.getEntity();
            } else {
                currentEntity = null;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderPart(DynamXModelRenderEvent.RenderPart event) {
        if(currentEntity == null)
            return;
        String name = event.getObjObjectRenderer().getObjObjectData().getName();
        if(!name.startsWith("custom_slotdoveiculo_"))
            return;
        VehicleCustomizationModule module = currentEntity.getModuleByType(VehicleCustomizationModule.class);
        if(module == null)
            return;
        String slot = name.substring("custom_slotdoveiculo_".length());
        String baseSlot = slot.contains("_") ? slot.substring(0, slot.indexOf('_')) : slot;
        String installed = module.getPart(baseSlot);
        if(installed.isEmpty() || !slot.startsWith(installed)) {
            event.setCanceled(true);
        }
    }
}