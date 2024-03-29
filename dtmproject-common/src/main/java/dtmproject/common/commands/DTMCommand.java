package dtmproject.common.commands;

import java.text.NumberFormat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import dtmproject.common.DTM;
import dtmproject.common.data.DTMMap;

public class DTMCommand implements CommandExecutor {
    private final DTM pl;

    public DTMCommand(DTM dtm) {
	this.pl = dtm;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
	if (!sender.isOp()) {
	    sender.sendMessage("§3>§b> §8+ §7DTM version " + pl.getDescription().getVersion());
	    sender.sendMessage("§3>§b> §8+ §7Original author: Juubes");
	    return true;
	}

	if (args.length == 0) {
	    sender.sendMessage("§3>§b> §8+ §7/DTM <status>");
	} else if (args.length == 1) {
	    if (args[0].equalsIgnoreCase("status")) {
		DTMMap map = pl.getLogicHandler().getCurrentMap();
		sender.sendMessage("§3>§b> §8+ §7DTM status: " + pl.getLogicHandler().getGameState().name());
		sender.sendMessage("§3>§b> §8+ §7Map ID: " + map.getId());
		sender.sendMessage("§3>§b> §8+ §7Map name: " + map.getDisplayName());
		map.getTeams().forEach(team -> {
		    double cumulativeRating = pl.getLogicHandler().getCumulativeRating(team);

		    NumberFormat format = NumberFormat.getInstance();
		    format.setMaximumFractionDigits(2);
		    format.setMinimumFractionDigits(2);

		    String cum = format.format(cumulativeRating);

		    sender.sendMessage(team.getTeamColor() + team.getDisplayName() + "§7: " + team.getPlayers().size()
			    + " pelaajaa (" + cum + ")");
		    team.getMonuments().forEach(mon -> {
			String intactStr = mon.isBroken() ? "§crikki" : "§aehjä";
			sender.sendMessage("    " + team.getTeamColor() + mon.getCustomName() + ": " + intactStr);
		    });
		});
	    }
	}
	return true;
    }
}
