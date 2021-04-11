package dtmproject.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GetposCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
	if (sender instanceof Player) {
	    if (!sender.isOp()) {
		sender.sendMessage("§3>§b> §8+ §7Sinulla ei ole permejä.");
		return true;
	    }

	    Player p = (Player) sender;
	    Location pLoc = p.getLocation();
	    sender.sendMessage("§3>§b> §8+ §7Maailma: " + pLoc.getWorld().getName());
	    sender.sendMessage("§3>§b> §8+ §7" + pLoc.getBlockX() + ", " + pLoc.getBlockY() + ", " + pLoc.getBlockZ());
	}
	return true;
    }
}
