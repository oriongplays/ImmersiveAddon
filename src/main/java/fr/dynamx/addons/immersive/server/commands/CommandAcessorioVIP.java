package fr.dynamx.addons.immersive.server.commands;

import fr.dynamx.addons.immersive.common.helpers.WheelParticleHelper;
import fr.dynamx.addons.immersive.common.helpers.WheelTuningHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * Command allowing players to choose a wheel skid particle that will be
 * applied to any vehicle they drive.
 */
public class CommandAcessorioVIP extends CommandBase {
    @Override
    public String getName() {
        return "AcessorioVIP";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/AcessorioVIP <player> Rodas skidparticle <particle>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 4 || !"Rodas".equalsIgnoreCase(args[1]) || !"skidparticle".equalsIgnoreCase(args[2])) {
            throw new WrongUsageException(getUsage(sender));
        }
        if (sender instanceof EntityPlayerMP) {
            throw new CommandException("Console only");
        }
        EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(args[0]);
        if (player == null) {
            throw new CommandException("Player not found");
        }
        String particle = args[3];
        if (!WheelTuningHelper.isValidParticle(particle)) {
            throw new CommandException("Invalid particle");
        }
        WheelParticleHelper.setParticle(player, particle);
        sender.sendMessage(new TextComponentTranslation("chat.dynamx_immersive.wheel_particle_updated"));
    }
}