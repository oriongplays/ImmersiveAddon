package fr.dynamx.addons.immersive.server.commands;

import fr.dynamx.addons.immersive.ImmersiveAddonConfig;
import fr.dynamx.common.contentpack.parts.BasePartSeat;
import fr.dynamx.common.entities.BaseVehicleEntity;
import fr.dynamx.common.entities.modules.SeatsModule;
import fr.dynamx.utils.DynamXUtils;
import fr.dynamx.api.physics.BulletShapeType;
import fr.dynamx.api.physics.EnumBulletShapeType;
import fr.dynamx.utils.physics.PhysicsRaycastResult;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class CommandCatch extends CommandBase {
    private static final Map<UUID, Integer> selections = new HashMap<>();

    @Override
    public String getName() {
        return "catch";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("pegar");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/catch";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) sender;

        BaseVehicleEntity<?> vehicle = getLookedVehicle(player);
        if (vehicle != null) {
            Integer id = selections.remove(player.getUniqueID());
            if (id == null) {
                player.sendMessage(new TextComponentTranslation("chat.dynamx_immersive.catch_no_entity"));
                return;
            }
            Entity target = player.world.getEntityByID(id);
            if (target == null) {
                player.sendMessage(new TextComponentTranslation("chat.dynamx_immersive.catch_no_entity"));
                return;
            }
            if (target.getDistance(vehicle) > 5) {
                player.sendMessage(new TextComponentTranslation("chat.dynamx_immersive.catch_too_far"));
                return;
            }
            if (!vehicle.hasModuleOfType(SeatsModule.class)) {
                player.sendMessage(new TextComponentTranslation("chat.dynamx_immersive.catch_no_vehicle"));
                return;
            }
            SeatsModule seats = vehicle.getModuleByType(SeatsModule.class);
            boolean mounted = false;
            for (Object part : vehicle.getPackInfo().getPartsByType(BasePartSeat.class)) {
                BasePartSeat seatPart = (BasePartSeat) part;
                if (!seatPart.isDriver()) {
                    Entity seatRider = seats.getSeatToPassengerMap().get(seatPart);
                    if (seatRider == null) {
                        seatPart.mountEntity(vehicle, seats, target);
                        mounted = true;
                        break;
                    }
                }
            }
            player.sendMessage(new TextComponentTranslation(mounted ? "chat.dynamx_immersive.catch_success" : "chat.dynamx_immersive.catch_no_seat"));
            return;
        }

        Entity looked = getLookedEntity(player, 5);
        if (looked == null) {
            player.sendMessage(new TextComponentTranslation("chat.dynamx_immersive.catch_no_vehicle"));
            return;
        }
        if (isDenied(looked)) {
            player.sendMessage(new TextComponentTranslation("chat.dynamx_immersive.catch_denied"));
            return;
        }
        selections.put(player.getUniqueID(), looked.getEntityId());
        player.sendMessage(new TextComponentTranslation("chat.dynamx_immersive.catch_select"));
    }

    private static boolean isDenied(Entity entity) {
        ResourceLocation rl = EntityList.getKey(entity);
        if (rl == null) {
            return true;
        }
        if ("dynamxmod".equals(rl.getNamespace()) && !"entity_prop".equals(rl.getPath())) {
            return true;
        }
        for (String entry : ImmersiveAddonConfig.denylist) {
            if (entry.endsWith(":*")) {
                String namespace = entry.substring(0, entry.length() - 2);
                if (rl.getNamespace().equals(namespace)) {
                    return true;
                }
            } else if (entry.equals(rl.toString())) {
                return true;
            }
        }
        return false;
    }

    private static BaseVehicleEntity<?> getLookedVehicle(EntityPlayerMP player) {
        Predicate<EnumBulletShapeType> pred = p -> !p.isPlayer();
        PhysicsRaycastResult result = DynamXUtils.castRayFromEntity(player, 5f, pred);
        if (result == null) {
            return null;
        }
        BulletShapeType<?> shape = (BulletShapeType<?>) result.hitBody.getUserObject();
        if (!shape.getType().isBulletEntity() || !(shape.getObjectIn() instanceof BaseVehicleEntity)) {
            return null;
        }
        return (BaseVehicleEntity<?>) shape.getObjectIn();
    }

    private static Entity getLookedEntity(EntityPlayerMP player, double distance) {
        Vec3d eye = player.getPositionEyes(1f);
        Vec3d look = player.getLook(1f);
        Vec3d end = eye.add(look.scale(distance));
        Entity closest = null;
        double closestDist = distance;
        List<Entity> list = player.world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().expand(look.x * distance, look.y * distance, look.z * distance).grow(1.0D));
        for (Entity e : list) {
            if (!e.canBeCollidedWith()) {
                continue;
            }
            float border = e.getCollisionBorderSize();
            AxisAlignedBB aabb = e.getEntityBoundingBox().grow(border);
            RayTraceResult rtr = aabb.calculateIntercept(eye, end);
            if (aabb.contains(eye)) {
                if (0.0D < closestDist) {
                    closest = e;
                    closestDist = 0.0D;
                }
            } else if (rtr != null) {
                double dist = eye.distanceTo(rtr.hitVec);
                if (dist < closestDist) {
                    closest = e;
                    closestDist = dist;
                }
            }
        }
        return closest;
    }
}