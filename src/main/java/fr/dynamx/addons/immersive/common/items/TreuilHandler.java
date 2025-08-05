package fr.dynamx.addons.immersive.common.items;

import fr.dynamx.common.entities.PhysicsEntity;
import fr.dynamx.common.entities.modules.MovableModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Handles active winch connections and detaches them when the distance or
 * time limit is exceeded.
 */
public class TreuilHandler {
    private static final int MAX_DISTANCE = 20; // blocks
    private static final int MAX_DURATION_TICKS = 20 * 20; // 20 seconds
    private static final List<ActiveTreuil> ACTIVE_TREUILS = new ArrayList<>();

    public static void register(PhysicsEntity<?> truck, PhysicsEntity<?> vehicle, EntityPlayer user) {
        ACTIVE_TREUILS.add(new ActiveTreuil(truck, vehicle, user, truck.world.getTotalWorldTime()));
    }

    public static void tick(World world) {
        Iterator<ActiveTreuil> it = ACTIVE_TREUILS.iterator();
        while (it.hasNext()) {
            ActiveTreuil treuil = it.next();
            if (treuil.truck.world != world || treuil.vehicle.world != world) {
                continue; // processed in its own world tick
            }
            if (treuil.truck.isDead || treuil.vehicle.isDead) {
                it.remove();
                continue;
            }
            long age = world.getTotalWorldTime() - treuil.startTick;
            if (age > MAX_DURATION_TICKS || treuil.truck.getDistance(treuil.vehicle) > MAX_DISTANCE) {
                treuil.truck.getJointsHandler().removeJointsOfType(MovableModule.JOINT_NAME, (byte) 2);
                treuil.vehicle.getJointsHandler().removeJointsOfType(MovableModule.JOINT_NAME, (byte) 2);
                if (!world.isRemote && treuil.user != null) {
                    treuil.user.sendMessage(new TextComponentTranslation("chat.dynamx_immersive.hook_detached"));
                    if (treuil.user instanceof EntityPlayerMP) {
                        EntityPlayerMP mp = (EntityPlayerMP) treuil.user;
                        mp.connection.sendPacket(new SPacketSoundEffect(
                                SoundEvents.ENTITY_ITEM_BREAK,
                                SoundCategory.PLAYERS,
                                mp.posX, mp.posY, mp.posZ,
                                1.0F, 1.0F));
                    }
                }
                it.remove();
            }
        }
    }

    private static class ActiveTreuil {
        final PhysicsEntity<?> truck;
        final PhysicsEntity<?> vehicle;
        final EntityPlayer user;
        final long startTick;

        ActiveTreuil(PhysicsEntity<?> truck, PhysicsEntity<?> vehicle, EntityPlayer user, long startTick) {
            this.truck = truck;
            this.vehicle = vehicle;
            this.user = user;
            this.startTick = startTick;
        }
    }
}