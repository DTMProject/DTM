package org.dtmproject.dtm.commands;

import java.util.Optional;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.dtmproject.dtm.DTM;
import org.dtmproject.dtm.logic.DTMLogicHandler;

public class StartCommand implements CommandExecutor {
	private final DTMLogicHandler logic;

	public StartCommand(DTM pl) {
		this.logic = pl.getLogicHandler();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (!sender.isOp()) {
			sender.sendMessage("§cEi permejä.");
			return true;
		}

		switch (logic.getGameState()) {
		case CHANGING_MAP:
			logic.loadNextGame(true, Optional.empty());
			break;
		case PAUSED:
			logic.togglePause();
			break;
		case PRE_START:
			logic.startGame();
			break;
		case RUNNING:
			sender.sendMessage("§ePeli on jo käynnissä.");
			break;
		}
		return true;
	}
}
