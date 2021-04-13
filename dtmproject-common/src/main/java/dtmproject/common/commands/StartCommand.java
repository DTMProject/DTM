package dtmproject.common.commands;

import java.util.Optional;

import dtmproject.common.DTM;
import dtmproject.common.logic.DTMLogicHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StartCommand implements CommandExecutor {
    private final DTMLogicHandler logic;

    public StartCommand(DTM pl) {
	this.logic = pl.getLogicHandler();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
	if (!sender.isOp()) {
	    sender.sendMessage("4>§c> §8- §7Ei permejä.");
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
	    sender.sendMessage("3>§b> §8+ §7Peli on jo käynnissä.");
	    break;
	}
	return true;
    }
}
