package fr.dynamx.addons.immersive.server.commands;

import fr.dynamx.addons.immersive.common.modules.DamageCarModule;
import fr.dynamx.addons.immersive.common.modules.DamageModule;
import fr.dynamx.addons.immersive.common.modules.EngineTuningModule;
import fr.dynamx.api.physics.BulletShapeType;
import fr.dynamx.api.physics.EnumBulletShapeType;
import fr.dynamx.common.entities.BaseVehicleEntity;
import fr.dynamx.utils.DynamXUtils;
import fr.dynamx.utils.physics.PhysicsRaycastResult;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.function.Predicate;

public class CommandImmersiveAddon extends CommandBase {
    @Override
    public String getName() {
        return "immersiveaddon";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/immersiveaddon repair <player> <value> | /immersiveaddon <player> lock|unlock | /immersiveaddon mec engine <player> <level>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(args.length < 2) {
            throw new WrongUsageException(getUsage(sender));
        }

        if("repair".equalsIgnoreCase(args[0])) {
            if(args.length != 3) {
                throw new WrongUsageException(getUsage(sender));
            }
            String playerName = args[1];
            EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(playerName);
            if(target == null) {
                throw new CommandException("Player not found");
            }
            int value;
            try {
                value = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                throw new CommandException("Invalid value");
            }
            if(value < 1 || value > 100) {
                throw new CommandException("Value must be between 1 and 100");
            }
            handleRepair(target, value, sender);
            return;
        }

        if("mec".equalsIgnoreCase(args[0])) {
            if(args.length != 4 || !"engine".equalsIgnoreCase(args[1])) {
                throw new WrongUsageException(getUsage(sender));
            }
            String playerName = args[2];
            EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(playerName);
            if(target == null) {
                throw new CommandException("Player not found");
            }
            int level;
            try {
                level = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                throw new CommandException("Invalid level");
            }
            if(level < 1 || level > 5) {
                throw new CommandException("Level must be between 1 and 5");
            }
            handleTuneEngine(target, level, sender);
            return;
        }

        String playerName = args[0];
        String action = args[1].toLowerCase();
        if(!action.equals("lock") && !action.equals("unlock")) {
            throw new WrongUsageException(getUsage(sender));
        }
        EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(playerName);
        if(target == null) {
            throw new CommandException("Player not found");
        }
        boolean lock = action.equals("lock");
        handleLock(target, lock, sender);
    }

    private void handleRepair(EntityPlayerMP player, int value, ICommandSender sender) throws CommandException {
        Predicate<EnumBulletShapeType> pred = p -> !p.isPlayer();
        PhysicsRaycastResult result = DynamXUtils.castRayFromEntity(player, 5f, pred);
        if(result == null) {
            throw new CommandException("No vehicle in sight");
        }
        BulletShapeType<?> shape = (BulletShapeType<?>) result.hitBody.getUserObject();
        if(!shape.getType().isBulletEntity() || !(shape.getObjectIn() instanceof BaseVehicleEntity)) {
            throw new CommandException("No vehicle in sight");
        }
        BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) shape.getObjectIn();
        DamageModule dmg = vehicle.getModuleByType(DamageModule.class);
        if(dmg != null) {
            dmg.repair(value);
        }
        DamageCarModule carDmg = vehicle.getModuleByType(DamageCarModule.class);
        if(carDmg != null) {
            carDmg.removePercentage(value);
        }
        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Vehicle repaired by " + value));
    }

    private void handleLock(EntityPlayerMP player, boolean lock, ICommandSender sender) throws CommandException {
        Predicate<EnumBulletShapeType> pred = p -> !p.isPlayer();
        PhysicsRaycastResult result = DynamXUtils.castRayFromEntity(player, 5f, pred);
        if(result == null) {
            throw new CommandException("No vehicle in sight");
        }
        BulletShapeType<?> shape = (BulletShapeType<?>) result.hitBody.getUserObject();
        if(!shape.getType().isBulletEntity() || !(shape.getObjectIn() instanceof BaseVehicleEntity)) {
            throw new CommandException("No vehicle in sight");
        }
        BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) shape.getObjectIn();
        // Use reflection to avoid direct dependency on BasicsAddon
        try {
            Class<?> moduleClass = Class.forName("fr.dynamx.addons.basics.common.modules.BasicsAddonModule");
            Object module = vehicle.getModuleByType((Class)moduleClass);
            if(module != null) {
                moduleClass.getMethod("setLocked", boolean.class).invoke(module, lock);
            } else {
                throw new CommandException("Vehicle doesn't support locking");
            }
        } catch (Exception e) {
            throw new CommandException("Failed to lock vehicle");
        }
        player.sendMessage(new TextComponentString(TextFormatting.GREEN + (lock ? "Vehicle locked" : "Vehicle unlocked")));
    }

    private void handleTuneEngine(EntityPlayerMP player, int level, ICommandSender sender) throws CommandException {
        Predicate<EnumBulletShapeType> pred = p -> !p.isPlayer();
        PhysicsRaycastResult result = DynamXUtils.castRayFromEntity(player, 5f, pred);
        if(result == null) {
            throw new CommandException("No vehicle in sight");
        }
        BulletShapeType<?> shape = (BulletShapeType<?>) result.hitBody.getUserObject();
        if(!shape.getType().isBulletEntity() || !(shape.getObjectIn() instanceof BaseVehicleEntity)) {
            throw new CommandException("No vehicle in sight");
        }
        BaseVehicleEntity<?> vehicle = (BaseVehicleEntity<?>) shape.getObjectIn();
        EngineTuningModule tuning = vehicle.getModuleByType(EngineTuningModule.class);
        if(tuning == null) {
            throw new CommandException("Vehicle cannot be tuned");
        }
        tuning.setTuningLevel(level);
        player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Engine tuned to level " + level));
    }
}