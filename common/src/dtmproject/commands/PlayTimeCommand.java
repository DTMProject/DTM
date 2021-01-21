package dtmproject.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import dtmproject.DTM;

public class PlayTimeCommand implements CommandExecutor {
    private final DTM pl;

    public PlayTimeCommand(DTM nexus) {
	this.pl = nexus;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
	if (args.length == 0) {
	    long minutesPlayed = pl.getLogicHandler().getCurrentMap().getTimePlayed() / 1000 / 60;
	    sender.sendMessage("§ePelattu " + minutesPlayed + " minuuttia");
	    return true;
	}

	if (!sender.isOp()) {
	    sender.sendMessage("§cTämä on liian oopee komento.");
	    return true;
	}

	int num = 0;
	try {
	    num = Integer.parseInt(args[0]);
	} catch (Exception e) {
	    sender.sendMessage("Ei sopiva numero: " + args[0]);
	    return true;
	}

	long newStartTime = (System.currentTimeMillis() - num * 60 * 1000);
	pl.getLogicHandler().getCurrentMap().setStartTime(newStartTime);
	sender.sendMessage("§ePelin uusi aika on " + num + " minuuttia.");
	return true;
    }

}
