package dtmproject.commands;

import java.util.Optional;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import dtmproject.DTM;

public class NextMapCommand implements CommandExecutor {
	private final DTM pl;

	public NextMapCommand(DTM pl) {
		this.pl = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.isOp()) {
			sender.sendMessage("§cVain operaattorit voivat skipata mapin.");
			return true;
		}

		Optional<String> mapRequest = Optional.empty();
		if (args.length > 0) {
			String req = args[0];

			if (pl.getDataHandler().mapExists(req)) {
				mapRequest = Optional.of(req);
			} else {
				sender.sendMessage("§eMappia ei ole ladattu: " + req);
				return true;
			}
		}

		pl.getLogicHandler().loadNextGame(mapRequest);
		return true;
	}

}
