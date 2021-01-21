package dtmproject.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dtmproject.DTM;
import dtmproject.data.DTMPlayerData;
import dtmproject.data.DTMTeam;
import dtmproject.logic.GameState;

public class JoinCommand implements CommandExecutor {
    private final DTM pl;

    public JoinCommand(DTM pl) {
	this.pl = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	if (pl.getLogicHandler().getGameState() == GameState.PAUSED) {
	    sender.sendMessage("§ePeli on pysäytetty.");
	    return true;
	}

	if (args.length == 0) {
	    if (!(sender instanceof Player)) {
		sender.sendMessage("§eVoit lähettää muita tiimeihin /join <pelaaja> <tiimi>");
		return true;
	    }
	    Player caller = (Player) sender;
	    DTMPlayerData pd = pl.getDataHandler().getPlayerData(caller.getUniqueId());

	    if (!pd.isSpectator()) {
		caller.sendMessage("§eOlet jo tiimissä " + pd.getTeam().getDisplayName() + "§e.");
		return true;
	    }

	    DTMTeam team = pl.getLogicHandler().setPlayerToSmallestTeam(caller);
	    caller.sendMessage("§eOlet nyt tiimissä " + team.getTeamColor() + team.getDisplayName());
	    return true;
	}

	if (!sender.isOp()) {
	    sender.sendMessage("§eOopeekomento! Ei permejä.");
	    return true;
	}

	if (args.length == 1) {
	    if (!(sender instanceof Player)) {
		sender.sendMessage("§eVoit lähettää muita tiimeihin /join <pelaaja> <tiimi>");
		return true;
	    }

	    DTMTeam team = pl.getLogicHandler().getCurrentMap().getTeamWithName(args[0]);
	    if (team == null) {
		sender.sendMessage("§ePelissä ei ole tiimiä " + args[0] + ".");
		return true;
	    }

	    Player caller = (Player) sender;
	    DTMPlayerData pd = pl.getDataHandler().getPlayerData(caller.getUniqueId());

	    pd.setTeam(team);
	    if (pl.getLogicHandler().getGameState() == GameState.RUNNING)
		pl.getLogicHandler().getCurrentMap().sendPlayerToGame(caller);
	    caller.sendMessage("§eOlet nyt tiimissä " + team.getTeamColor() + team.getDisplayName() + "§e.");
	    return true;
	}

	if (args.length == 2) {
	    Player target = Bukkit.getPlayer(args[0]);
	    if (target == null) {
		sender.sendMessage("§eTämä pelaaja ei ole paikalla.");
		return true;
	    }

	    DTMTeam team = pl.getLogicHandler().getCurrentMap().getTeamWithName(args[1]);
	    if (team == null) {
		sender.sendMessage("§ePelissä ei ole tiimiä " + args[1].toLowerCase() + ".");
		return true;
	    }

	    DTMPlayerData targetPlayerData = pl.getDataHandler().getPlayerData(target.getUniqueId());
	    targetPlayerData.setTeam(team);
	    if (pl.getLogicHandler().getGameState() == GameState.RUNNING)
		pl.getLogicHandler().getCurrentMap().sendPlayerToGame(target);

	    sender.sendMessage("§ePelaaja " + targetPlayerData.getDisplayName() + " §elähetetty tiimiin "
		    + team.getTeamColor() + team.getDisplayName() + "§e.");
	    return true;
	}

	return true;
    }
}
