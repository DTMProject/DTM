package dtmproject.common.commands;

import dtmproject.common.DTM;
import dtmproject.common.logic.GameState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PauseCommand implements CommandExecutor {
    private final DTM pl;

    public PauseCommand(DTM pl) {
	this.pl = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	if (!sender.isOp()) {
	    sender.sendMessage("§3>§b> §8+ §7Hus! Tämä on liian oopee komento.");
	    return true;
	}

	pl.getLogicHandler().togglePause();

	if (pl.getLogicHandler().getGameState() == GameState.PAUSED)
	    sender.sendMessage("§3>§b> §8+ §7Peli on nyt paussilla. Jatka peliä komennolla /start tai /pause.");

	return true;
    }

}
