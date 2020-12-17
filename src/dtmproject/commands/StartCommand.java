package dtmproject.commands;

import java.util.Optional;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import dtmproject.DTM;
import dtmproject.logic.GameState;

public class StartCommand implements CommandExecutor {
	private final DTM pl;

	public StartCommand(DTM pl) {
		this.pl = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (!sender.isOp()) {
			sender.sendMessage("§cEi permejä.");
			return true;
		}

		if (pl.getLogicHandler().getGameState() == GameState.RUNNING) {
			sender.sendMessage("§ePeli on jo käynnissä.");
			return true;
		}

		if (pl.getLogicHandler().getGameState() == GameState.PAUSED) {
			pl.getLogicHandler().togglePause();
			return true;
		}

		if (pl.getLogicHandler().getGameState() == GameState.COUNTDOWN) {
			if (pl.getLogicHandler().getCurrentMap().isRunning())
				pl.getLogicHandler().loadNextGame(Optional.empty());
			pl.getLogicHandler().startGame();
			return true;
		}

		sender.sendMessage("§ePeli on pysäytetty kokonaan. /dtm status");
		return true;
	}
}
