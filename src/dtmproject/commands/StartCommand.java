package dtmproject.commands;

import java.util.Optional;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import dtmproject.DTM;
import dtmproject.logic.DTMLogicHandler;
import dtmproject.logic.GameState;

public class StartCommand implements CommandExecutor {
	private final DTM pl;
	private final DTMLogicHandler logic;

	public StartCommand(DTM pl) {
		this.pl = pl;
		this.logic = pl.getLogicHandler();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (!sender.isOp()) {
			sender.sendMessage("§cEi permejä.");
			return true;
		}

		if (logic.getGameState() == GameState.RUNNING) {
			sender.sendMessage("§ePeli on jo käynnissä.");
			return true;
		}

		if (logic.getGameState() == GameState.PAUSED) {
			logic.togglePause();
			return true;
		}

		if (logic.getGameState() == GameState.COUNTDOWN) {
			if (logic.getCurrentMap().isRunning())
				logic.loadNextGame(Optional.empty());
			logic.startGame();
			return true;
		}

		sender.sendMessage("§ePeli on pysäytetty kokonaan. /dtm status");
		return true;
	}
}
