package dtmproject.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dtmproject.DTM;
import dtmproject.logic.GameState;
import dtmproject.playerdata.DTMPlayerData;

public class JoinCommand implements CommandExecutor {
	private final DTM pl;

	public JoinCommand(DTM pl) {
		this.pl = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// TODO implement more args

		if (!(sender instanceof Player))
			return true;

		if (pl.getLogicHandler().getGameState() == GameState.PAUSED) {
			sender.sendMessage("§ePeli on pysäytetty.");
			return true;
		}

		Player p = (Player) sender;
		DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
		if (args.length == 0) {
			if (pd.isSpectator())
				pl.getLogicHandler().setPlayerToSmallestTeam(p);
			else
				p.sendMessage("§eOlet jo tiimissä " + pd.getTeam().getDisplayName());
		}

		return true;
	}
}
