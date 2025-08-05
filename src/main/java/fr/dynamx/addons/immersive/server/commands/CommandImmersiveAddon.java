package fr.dynamx.addons.immersive.server.commands;

import fr.dynamx.addons.immersive.common.modules.DamageCarModule;
import fr.dynamx.addons.immersive.common.modules.DamageModule;
import fr.dynamx.addons.immersive.common.modules.EngineTuningModule;
import fr.dynamx.addons.immersive.common.modules.VehiclePropertiesModule;
import fr.dynamx.addons.immersive.common.modules.WheelPropertiesModule;
import fr.dynamx.addons.immersive.common.helpers.WheelTuningHelper;
import fr.dynamx.common.contentpack.type.vehicle.ModularVehicleInfo;
import fr.dynamx.addons.immersive.common.helpers.VehicleLevelConfig;
import fr.dynamx.addons.immersive.common.helpers.EngineTuningHelper;
import fr.dynamx.common.DynamXContext;
import fr.dynamx.common.contentpack.sync.MessagePacksHashs;
import fr.dynamx.common.contentpack.sync.PackSyncHandler;
import fr.dynamx.api.network.EnumPacketTarget;
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
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Map;
import java.util.function.Predicate;

public class CommandImmersiveAddon extends CommandBase {
    @Override
    public String getName() {
        return "immersiveaddon";
    }

    @Override
    public String getUsage(ICommandSender sender) {
           return "/immersiveaddon repair <player> <value> | /immersiveaddon <player> lock|unlock | /immersiveaddon mec vehicle peso <type> <player> | /immersiveaddon mec vehicle engine <level> <player> | /immersiveaddon mec vehicle wheel <option> <value> <player>";
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
            if(args.length < 4 || !"vehicle".equalsIgnoreCase(args[1])) {
                throw new WrongUsageException(getUsage(sender));
            }

            String sub = args[2];
            if("peso".equalsIgnoreCase(sub) && args.length == 5) {
                String type = args[3];
                String playerName = args[4];
                if(!VehicleLevelConfig.typeExists(type)) {
                    throw new CommandException("Invalid type");
                }
                EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(playerName);
                if(target == null) {
                    throw new CommandException("Player not found");
                }
                handleSetPeso(target, type, sender);
                return;
            }

            if("engine".equalsIgnoreCase(sub) && args.length == 5) {
                int level;
                try {
                    level = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    throw new CommandException("Invalid level");
                }
                if(!EngineTuningHelper.levelExists(level)) {
                    throw new CommandException("Invalid level");
                }
                String playerName = args[4];
                EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(playerName);
                if(target == null) {
                    throw new CommandException("Player not found");
                }
                handleVehicleEngine(target, level, sender);
                return;
            }

            if("wheel".equalsIgnoreCase(sub)) {
                if(args.length == 5) {
                    String model = args[3];
                    String playerName = args[4];
                    EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(playerName);
                    if(target == null) {
                        throw new CommandException("Player not found");
                    }
                    handleWheelModel(target, model, sender);
                    return;
                }
                if(args.length == 6) {
                    String option = args[3];
                    String value = args[4];
                    String playerName = args[5];
                    EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(playerName);
                    if(target == null) {
                        throw new CommandException("Player not found");
                    }
                    switch(option.toLowerCase()) {
                        case "friction":
                            handleWheelFriction(target, WheelTuningHelper.clampFriction(parseFloat(value)), sender);
                            return;
                        case "susrestlength":
                            handleWheelRestLength(target, WheelTuningHelper.clampRest(parseFloat(value)), sender);
                            return;
                        case "susstiffness":
                            handleWheelStiffness(target, WheelTuningHelper.clampStiff(parseFloat(value)), sender);
                            return;
                        case "skidparticle":
                            if(!WheelTuningHelper.isValidParticle(value))
                                throw new CommandException("Invalid particle");
                            handleWheelParticle(target, value, sender);
                            return;
                        default:
                            throw new WrongUsageException(getUsage(sender));
                    }
                }
            }

            throw new WrongUsageException(getUsage(sender));
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
        player.sendMessage(new TextComponentTranslation("chat.dynamx_immersive.vehicle_repaired_by", value));
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
        player.sendMessage(new TextComponentTranslation(lock ? "chat.dynamx_immersive.vehicle_locked" : "chat.dynamx_immersive.vehicle_unlocked"));
    }

    private void handleSetPeso(EntityPlayerMP player, String type, ICommandSender sender) throws CommandException {
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
        VehiclePropertiesModule module = vehicle.getModuleByType(VehiclePropertiesModule.class);
        if(module == null) {
            throw new CommandException("Vehicle cannot be updated");
        }
        module.setWeightType(type);
        // Resend the physics variables so clients apply the new mass immediately
        vehicle.getSynchronizer().resyncEntity(player);
        player.sendMessage(new TextComponentTranslation("chat.dynamx_immersive.vehicle_weight_set", type));
    }

    private void handleVehicleEngine(EntityPlayerMP player, int level, ICommandSender sender) throws CommandException {
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
        EngineTuningModule module = vehicle.getModuleByType(EngineTuningModule.class);
        if(module == null) {
            throw new CommandException("Vehicle cannot be tuned");
        }
        module.setTuningLevel(level);
        vehicle.getSynchronizer().resyncEntity(player);
        player.sendMessage(new TextComponentTranslation("chat.dynamx_immersive.engine_level_set", level));
    }
    private void handleWheelModel(EntityPlayerMP player, String model, ICommandSender sender) throws CommandException {
        BaseVehicleEntity<?> vehicle = getTargetVehicle(player);
        WheelPropertiesModule module = vehicle.getModuleByType(WheelPropertiesModule.class);
        if(module == null) {
            throw new CommandException("Vehicle cannot be updated");
        }
        ModularVehicleInfo info = (ModularVehicleInfo) vehicle.getPackInfo();
        module.setModel("obj/" + info.getPackName() + "/" + model + ".obj");
        vehicle.getSynchronizer().resyncEntity(player);
        player.sendMessage(new TextComponentTranslation("chat.dynamx_immersive.wheel_model_updated"));
    }

    private void handleWheelFriction(EntityPlayerMP player, float value, ICommandSender sender) throws CommandException {
        BaseVehicleEntity<?> vehicle = getTargetVehicle(player);
        WheelPropertiesModule module = vehicle.getModuleByType(WheelPropertiesModule.class);
        if(module == null) {
            throw new CommandException("Vehicle cannot be updated");
        }
        module.setFriction(value);
        vehicle.getSynchronizer().resyncEntity(player);
        player.sendMessage(new TextComponentTranslation("chat.dynamx_immersive.wheel_friction_updated"));
    }

    private void handleWheelRestLength(EntityPlayerMP player, float value, ICommandSender sender) throws CommandException {
        BaseVehicleEntity<?> vehicle = getTargetVehicle(player);
        WheelPropertiesModule module = vehicle.getModuleByType(WheelPropertiesModule.class);
        if(module == null) {
            throw new CommandException("Vehicle cannot be updated");
        }
        module.setRestLength(value);
        vehicle.getSynchronizer().resyncEntity(player);
        player.sendMessage(new TextComponentTranslation("chat.dynamx_immersive.wheel_rest_length_updated"));
    }

    private void handleWheelStiffness(EntityPlayerMP player, float value, ICommandSender sender) throws CommandException {
        BaseVehicleEntity<?> vehicle = getTargetVehicle(player);
        WheelPropertiesModule module = vehicle.getModuleByType(WheelPropertiesModule.class);
        if(module == null) {
            throw new CommandException("Vehicle cannot be updated");
        }
        module.setStiffness(value);
        vehicle.getSynchronizer().resyncEntity(player);
        player.sendMessage(new TextComponentTranslation("chat.dynamx_immersive.wheel_stiffness_updated"));
    }

    private void handleWheelParticle(EntityPlayerMP player, String particle, ICommandSender sender) throws CommandException {
        BaseVehicleEntity<?> vehicle = getTargetVehicle(player);
        WheelPropertiesModule module = vehicle.getModuleByType(WheelPropertiesModule.class);
        if(module == null) {
            throw new CommandException("Vehicle cannot be updated");
        }
        module.setSkidParticle(particle);
        vehicle.getSynchronizer().resyncEntity(player);
        player.sendMessage(new TextComponentTranslation("chat.dynamx_immersive.wheel_particle_updated"));
    }

    private BaseVehicleEntity<?> getTargetVehicle(EntityPlayerMP player) throws CommandException {
        Predicate<EnumBulletShapeType> pred = p -> !p.isPlayer();
        PhysicsRaycastResult result = DynamXUtils.castRayFromEntity(player, 5f, pred);
        if(result == null) {
            throw new CommandException("No vehicle in sight");
        }
        BulletShapeType<?> shape = (BulletShapeType<?>) result.hitBody.getUserObject();
        if(!shape.getType().isBulletEntity() || !(shape.getObjectIn() instanceof BaseVehicleEntity)) {
            throw new CommandException("No vehicle in sight");
        }
        return (BaseVehicleEntity<?>) shape.getObjectIn();
    }

    private float parseFloat(String value) throws CommandException {
        try {
            return Float.parseFloat(value);
        } catch(NumberFormatException e) {
            throw new CommandException("Invalid value");
        }
    }
}