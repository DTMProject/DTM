package com.juubes.dtmproject.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.juubes.dtmproject.DTM;
import com.juubes.dtmproject.setup.DTMTeam;
import com.juubes.dtmproject.setup.Monument;
import com.juubes.nexus.Nexus;
import com.juubes.nexus.commands.EditModeHandler;
import com.juubes.nexus.logic.GameLogic;
import com.juubes.nexus.logic.Team;

public class DTMCommand implements CommandExecutor {
	private final DTM pl;

	public DTMCommand(DTM pl) {
		this.pl = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (!sender.isOp()) {
			sender.sendMessage("§aDTM §fversion §a" + pl.getDescription().getVersion());
			sender.sendMessage("§fAuthor: §aJuubes");
			return true;
		}

		if (args.length == 0) {
			sender.sendMessage("§c/DTM <reload|status|save>");
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("reload")) {
				reloadConfig();
				sender.sendMessage("§eAsetukset ja configurointi-tiedostot uudelleenladattu.");
			} else if (args[0].equalsIgnoreCase("status")) {
				sender.sendMessage("§bDTM gamestatus: §a" + GameLogic.getGameState().name());
				sender.sendMessage("§b  Teams:");
				for (Team nexusTeam : GameLogic.getCurrentGame().getTeams()) {
					DTMTeam team = (DTMTeam) nexusTeam;
					sender.sendMessage("    " + team.getChatColor() + "§l" + team.getDisplayName());
					for (Monument mon : team.getMonuments()) {
						sender.sendMessage("        " + team.getChatColor() + mon.customName);
					}
					sender.sendMessage("");
				}
			} else if (args[0].equalsIgnoreCase("save")) {
				sender.sendMessage("§eTallennetaan configurointia...");
				for (String mapID : DTM.getDatabaseManager().getMaps()) {
					DTM.getDatabaseManager().saveMapSettings(mapID);
					sender.sendMessage("§eTallennettu configurointi mapille " + mapID);
				}
				sender.sendMessage("§eUudelleenladataan...");
				reloadConfig();
				sender.sendMessage("§eValmis.");

			}
		}
		return true;
	}

	public static void reloadConfig() {
		Nexus.getPlugin().reloadConfig();
		List<String> maps = Nexus.getPlugin().getConfig().getStringList("maps");
		DTM.getDatabaseManager().prepareMapSettings(maps.toArray(new String[maps.size()]));
		EditModeHandler.pendingList.clear();
	}
}
