package fr.dynamx.addons.immersive.utils;

import fr.dynamx.addons.basics.utils.VehicleKeyUtils;
import fr.dynamx.common.entities.BaseVehicleEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public final class VehicleLockUtils {
    private VehicleLockUtils() {
    }

    public static boolean hasLinkedKey(EntityPlayer player, BaseVehicleEntity<?> vehicle) {
        if (player == null || vehicle == null) {
            return false;
        }

        if (hasKey(player.inventory.mainInventory, vehicle)) {
            return true;
        }

        return hasKey(player.inventory.offHandInventory, vehicle);
    }

    private static boolean hasKey(Iterable<ItemStack> stacks, BaseVehicleEntity<?> vehicle) {
        for (ItemStack stack : stacks) {
            if (isMatchingKey(stack, vehicle)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMatchingKey(ItemStack stack, BaseVehicleEntity<?> vehicle) {
        return !stack.isEmpty()
                && VehicleKeyUtils.isKeyItem(stack)
                && VehicleKeyUtils.hasLinkedVehicle(stack)
                && VehicleKeyUtils.isVehicleLinked(stack, vehicle.getPersistentID());
    }
}