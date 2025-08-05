package fr.dynamx.addons.immersive.server.commands;

import fr.dynamx.addons.immersive.common.items.ItemVehicleTracker;
import fr.dynamx.addons.immersive.common.items.ItemsRegister;
import fr.dynamx.common.entities.BaseVehicleEntity;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;

/**
 * Gives a tracking compass pointing to the vehicle with the given plate.
 * Usage: /rastreio <player> <plate>
 */
public class CommandRastreio extends CommandBase {
    @Override
    public String getName() {
        return "rastreio";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/rastreio <player> <plate>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            throw new WrongUsageException(getUsage(sender));
        }
        String playerName = args[0];
        String plate = args[1];

        EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(playerName);
        if (player == null) {
            throw new CommandException("Player not found");
        }

        BaseVehicleEntity<?> vehicle = findVehicleByPlate(server, plate);
        if (vehicle == null) {
            throw new CommandException("Vehicle not found");
        }

        ItemStack stack = new ItemStack(ItemsRegister.VEHICLE_TRACKER);
        ItemVehicleTracker.setTrackedEntity(stack, vehicle);
        player.addItemStackToInventory(stack);
        player.sendMessage(new TextComponentTranslation("chat.dynamx_immersive.tracker_given", plate));
    }

    private BaseVehicleEntity<?> findVehicleByPlate(MinecraftServer server, String plate) {
        for (WorldServer world : DimensionManager.getWorlds()) {
            for (Entity entity : world.loadedEntityList) {
                if (entity instanceof BaseVehicleEntity) {
                    NBTTagCompound data = new NBTTagCompound();
                    entity.writeToNBT(data);
                    if (data.hasKey("bas_immat_plate", Constants.NBT.TAG_STRING)
                            && plate.equals(data.getString("bas_immat_plate"))) {
                        return (BaseVehicleEntity<?>) entity;
                    }
                }
            }
        }
        return null;
    }
}