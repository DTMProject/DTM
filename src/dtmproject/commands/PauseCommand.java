package dtmproject.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import dtmproject.DTM;
import dtmproject.logic.GameState;

public class PauseCommand implements CommandExecutor {
	private final DTM pl;

	public PauseCommand(DTM pl) {
		this.pl = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.isOp()) {
			sender.sendMessage("§eHus! Tämä on liian oopee komento.");
			return true;
		}

		pl.getLogicHandler().togglePause();

		if (pl.getLogicHandler().getGameState() == GameState.PAUSED)
			sender.sendMessage("§ePeli on nyt paussilla. Jatka peliä komennolla /start tai /pause.");

		return true;
	}

}
