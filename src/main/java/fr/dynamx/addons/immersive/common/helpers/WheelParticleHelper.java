package fr.dynamx.addons.immersive.common.helpers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Stores and retrieves the wheel skid particle chosen by a player.
 */
public class WheelParticleHelper {
    private static final String TAG = "IA_SkidParticle";

    /**
     * Returns the player's selected skid particle, or "spit" if none was set.
     */
    public static String getParticle(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData();
        if (data.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            NBTTagCompound persisted = data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            if (persisted.hasKey(TAG)) {
                return persisted.getString(TAG);
            }
        }
        return "spit";
    }

    /**
     * Stores the player's selected skid particle in their persistent data.
     */
    public static void setParticle(EntityPlayer player, String particle) {
        NBTTagCompound data = player.getEntityData();
        NBTTagCompound persisted;
        if (data.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            persisted = data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        } else {
            persisted = new NBTTagCompound();
            data.setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
        }
        persisted.setString(TAG, particle);
    }
}