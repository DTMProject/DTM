<<<<<<< HEAD:common/src/dtmproject/commands/DTMCommand.java
package dtmproject.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import dtmproject.DTM;
import dtmproject.data.DTMMap;

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
		    sender.sendMessage(team.getTeamColor() + team.getDisplayName() + "§7: " + team.getPlayers().size()
			    + " pelaajaa");
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
=======
package dtmproject.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import dtmproject.DTM;
import dtmproject.data.DTMMap;

public class DTMCommand implements CommandExecutor {
    private final DTM pl;

    public DTMCommand(DTM dtm) {
	this.pl = dtm;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
	if (!sender.isOp()) {
	    sender.sendMessage("§eDTM version " + pl.getDescription().getVersion());
	    sender.sendMessage("§eOriginal author: Juubes");
	    return true;
	}

	if (args.length == 0) {
	    sender.sendMessage("§c/DTM <status>");
	} else if (args.length == 1) {
	    if (args[0].equalsIgnoreCase("status")) {
		DTMMap map = pl.getLogicHandler().getCurrentMap();
		sender.sendMessage("§eDTM status: " + pl.getLogicHandler().getGameState().name());
		sender.sendMessage("§eMap ID: " + map.getId());
		sender.sendMessage("§eMap name: " + map.getDisplayName());
		map.getTeams().forEach(team -> {
		    sender.sendMessage(team.getTeamColor() + team.getDisplayName() + "§e: " + team.getPlayers().size()
			    + " pelaajaa");
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
>>>>>>> d72545a246c4ddde54e7f06999600d3deaeefb0d:dtmproject-common/src/main/java/dtmproject/commands/DTMCommand.java
