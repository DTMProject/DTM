package dtmproject.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldsCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (!sender.isOp()) {
			sender.sendMessage("§eSinulla ei ole permejä.");
			return true;
		}

		if (cmd.getName().equals("worlds")) {
			sender.sendMessage("§eLadatut maailmat:");
			for (World world : Bukkit.getWorlds()) {
				sender.sendMessage("§e    " + world.getName());
			}
		} else if (cmd.getName().equals("world")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("§eTämän komennon voi suorittaa vain pelaajana.");
				return true;
			}

			Player p = (Player) sender;
			if (args.length == 0) {
				sender.sendMessage("§eMaailma: " + p.getWorld().getName());
			} else {
				World world = Bukkit.getWorld(args[0]);
				if (world == null) {
					sender.sendMessage("§eMaailmaa " + args[0] + " ei ole ladattu.");
				} else {
					p.teleport(new Location(world, 0, 100, 0));
				}
			}
		}
		return true;

	}
}
