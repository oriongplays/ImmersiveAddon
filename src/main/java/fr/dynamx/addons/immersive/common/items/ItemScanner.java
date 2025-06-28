package fr.dynamx.addons.immersive.common.items;

import fr.dynamx.addons.immersive.common.GuiHandler;
import fr.dynamx.addons.immersive.common.modules.VehicleCustomizationModule;
import fr.dynamx.common.entities.BaseVehicleEntity;
import fr.dynamx.common.items.DynamXItemRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

public class ItemScanner extends Item {
    public ItemScanner(String name) {
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(DynamXItemRegistry.objectTab);
        setMaxStackSize(1);
        ItemsRegister.INSTANCE.getItems().add(this);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if(!worldIn.isRemote) {
            EntityPlayerMP mp = (EntityPlayerMP) playerIn;
            if(mp.interactionManager.getGameType() == GameType.ADVENTURE) {
                if(mp.getRidingEntity() instanceof BaseVehicleEntity) {
                    BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) mp.getRidingEntity();
                    VehicleCustomizationModule module = vehicle.getModuleByType(VehicleCustomizationModule.class);
                    if(module != null && vehicle.getModuleByType(fr.dynamx.common.entities.modules.SeatsModule.class).getControllingPassenger() == mp) {
                        mp.openGui(fr.dynamx.addons.immersive.ImmersiveAddon.INSTANCE,
                                   GuiHandler.VEHICLE_PARTS, mp.world, vehicle.getEntityId(), 0, 0);
                    }
                }
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }
}