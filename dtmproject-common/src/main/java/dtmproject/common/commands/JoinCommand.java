package dtmproject.common.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dtmproject.common.DTM;
import dtmproject.common.data.DTMPlayerData;
import dtmproject.common.data.DTMTeam;
import dtmproject.common.logic.GameState;

public class JoinCommand implements CommandExecutor {
    private final DTM pl;

    public JoinCommand(DTM pl) {
	this.pl = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	if (pl.getLogicHandler().getGameState() == GameState.PAUSED) {
	    sender.sendMessage("§3>§b> §8+ §7Peli on pysäytetty.");
	    return true;
	}

	if (args.length == 0) {
	    if (!(sender instanceof Player)) {
		sender.sendMessage("§3>§b> §8+ §7Voit lähettää muita tiimeihin /join <pelaaja> <tiimi>");
		return true;
	    }
	    Player caller = (Player) sender;
	    DTMPlayerData pd = pl.getDataHandler().getPlayerData(caller.getUniqueId());

	    if (!pd.isSpectator()) {
		caller.sendMessage("§3>§b> §8+ §7Olet jo tiimissä " + pd.getTeam().getDisplayName() + "§7.");
		return true;
	    }

	    pl.getLogicHandler().setPlayerToWorstTeam(caller);
	    DTMTeam team = pd.getTeam();

	    caller.sendMessage("§3>§b> §8+ §7Olet nyt tiimissä " + team.getTeamColor() + team.getDisplayName());
	    return true;
	}

	if (!sender.isOp()) {
	    sender.sendMessage("§3>§b> §8+ §7Oopeekomento! Ei permejä.");
	    return true;
	}

	if (args.length == 1) {
	    if (!(sender instanceof Player)) {
		sender.sendMessage("§3>§b> §8+ §7Voit lähettää muita tiimeihin /join <pelaaja> <tiimi>");
		return true;
	    }

	    DTMTeam team = pl.getLogicHandler().getCurrentMap().getTeamWithName(args[0]);
	    if (team == null) {
		sender.sendMessage("§3>§b> §8+ §7Pelissä ei ole tiimiä " + args[0] + ".");
		return true;
	    }

	    Player caller = (Player) sender;
	    DTMPlayerData pd = pl.getDataHandler().getPlayerData(caller.getUniqueId());

	    pd.setTeam(team);
	    pl.getContributionCounter().playerJoined(caller.getUniqueId(), team);

	    if (pl.getLogicHandler().getGameState() == GameState.RUNNING)
		pl.getLogicHandler().getCurrentMap().sendPlayerToGame(caller);
	    caller.sendMessage("§3>§b> §8+ §7Olet nyt tiimissä " + team.getTeamColor() + team.getDisplayName() + "§7.");
	    return true;
	}

	if (args.length == 2) {
	    Player target = Bukkit.getPlayer(args[0]);
	    if (target == null) {
		sender.sendMessage("§3>§b> §8+ §7Tämä pelaaja ei ole paikalla.");
		return true;
	    }

	    DTMTeam team = pl.getLogicHandler().getCurrentMap().getTeamWithName(args[1]);
	    if (team == null) {
		sender.sendMessage("§3>§b> §8+ §7Pelissä ei ole tiimiä " + args[1].toLowerCase() + ".");
		return true;
	    }

	    DTMPlayerData targetPlayerData = pl.getDataHandler().getPlayerData(target.getUniqueId());
	    targetPlayerData.setTeam(team);
	    pl.getContributionCounter().playerJoined(target.getUniqueId(), team);

	    if (pl.getLogicHandler().getGameState() == GameState.RUNNING)
		pl.getLogicHandler().getCurrentMap().sendPlayerToGame(target);

	    sender.sendMessage("§3>§b> §8+ §7Pelaaja " + targetPlayerData.getDisplayName() + " §7lähetetty tiimiin "
		    + team.getTeamColor() + team.getDisplayName() + "§7.");
	    return true;
	}

	return true;
    }
}
