package dtmproject.common.commands;

import dtmproject.common.DTM;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpectateCommand implements CommandExecutor {
    private final DTM pl;

    public SpectateCommand(DTM pl) {
	this.pl = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	if (args.length == 0) {
	    if (!(sender instanceof Player)) {
		sender.sendMessage("3>§b> §8+ §7Bruhh.");
		return true;
	    }

	    Player p = (Player) sender;
	    pl.getLogicHandler().getCurrentMap().sendToSpectate(p);

	    p.sendMessage("3>§b> §8+ §7Olet nyt katsoja.");
	    return true;
	}

	Player target = Bukkit.getPlayer(args[0]);
	if (target == null) {
	    sender.sendMessage("3>§b> §8+ §7Pelaajaa ei löytynyt.");
	    return true;
	}

	pl.getLogicHandler().getCurrentMap().sendToSpectate(target);
	sender.sendMessage("3>§b> §8+ §7Lähetetty " + target.getName() + " katsojaksi.");
	target.sendMessage("3>§b> §8+ §7Sinut on lähetetty katsojaksi.");
	return true;
    }

}
