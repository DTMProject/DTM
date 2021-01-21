package dtmproject.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dtmproject.DTM;

public class SpectateCommand implements CommandExecutor {
    private final DTM pl;

    public SpectateCommand(DTM pl) {
	this.pl = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	if (args.length == 0) {
	    if (!(sender instanceof Player)) {
		sender.sendMessage("§eBruhh.");
		return true;
	    }

	    Player p = (Player) sender;
	    pl.getLogicHandler().getCurrentMap().sendToSpectate(p);

	    p.sendMessage("§eOlet nyt katsoja.");
	    return true;
	}

	Player target = Bukkit.getPlayer(args[0]);
	if (target == null) {
	    sender.sendMessage("§ePelaajaa ei löytynyt.");
	    return true;
	}

	pl.getLogicHandler().getCurrentMap().sendToSpectate(target);
	sender.sendMessage("§eLähetetty " + target.getName() + " katsojaksi.");
	target.sendMessage("§eSinut on lähetetty katsojaksi.");
	return true;
    }

}
