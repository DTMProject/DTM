package dtmproject.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dtmproject.DTM;
import dtmproject.data.DTMPlayerData;
import dtmproject.logic.GameState;

public class SpectateCommand implements CommandExecutor {
	private final DTM pl;

	public SpectateCommand(DTM pl) {
		this.pl = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§eBruhh.");
			return true;
		}

		Player p = (Player) sender;
		DTMPlayerData pd = pl.getDataHandler().getPlayerData(p);
		
		pl.getLogicHandler().getCurrentMap().sendToSpectate(p);

		p.sendMessage("§eOlet nyt katsoja.");
		return true;
	}

}
