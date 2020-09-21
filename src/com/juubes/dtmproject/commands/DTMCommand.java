package com.juubes.dtmproject.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.juubes.dtmproject.DTM;
import com.juubes.dtmproject.setup.DTMTeam;
import com.juubes.dtmproject.setup.Monument;
import com.juubes.nexus.Lang;
import com.juubes.nexus.logic.Team;

import net.md_5.bungee.api.ChatColor;

public class DTMCommand implements CommandExecutor {
	private final DTM dtm;

	public DTMCommand(DTM dtm) {
		this.dtm = dtm;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (!sender.isOp()) {
			sender.sendMessage(ChatColor.GREEN + "DTM" + ChatColor.WHITE + " version " + ChatColor.GREEN + dtm
					.getDescription().getVersion());
			sender.sendMessage(ChatColor.WHITE + "Author: " + ChatColor.GREEN + "Juubes");
			return true;
		}

		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "/DTM <reload|status|save>");
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("reload")) {
				reloadConfig();
				sender.sendMessage(Lang.get("settings-reloaded"));
			} else if (args[0].equalsIgnoreCase("status")) {
				sender.sendMessage(ChatColor.AQUA + "DTM gamestatus: " + ChatColor.GREEN + dtm.getNexus().getGameLogic()
						.getGameState().name());
				sender.sendMessage(ChatColor.AQUA + "  Teams:");
				for (Team nexusTeam : dtm.getNexus().getGameLogic().getCurrentGame().getTeams()) {
					DTMTeam team = (DTMTeam) nexusTeam;
					sender.sendMessage("    " + team.getChatColor() + ChatColor.BOLD + team.getDisplayName());
					for (Monument mon : team.getMonuments()) {
						sender.sendMessage("        " + team.getChatColor() + mon.customName);
					}
					sender.sendMessage("");
				}
			} else if (args[0].equalsIgnoreCase("save")) {
				sender.sendMessage(Lang.get("saving-config"));
				for (String mapID : dtm.getDatabaseManager().getMaps()) {
					dtm.getDatabaseManager().saveMapSettings(mapID);
					sender.sendMessage(String.format(Lang.get("config-saved-for-map"), mapID));
				}
				sender.sendMessage("§eUudelleenladataan...");
				reloadConfig();
				sender.sendMessage(Lang.get("ready"));

			}
		}
		return true;
	}

	public void reloadConfig() {
		dtm.getNexus().reloadConfig();
		List<String> maps = dtm.getNexus().getConfig().getStringList("maps");
		dtm.getDatabaseManager().prepareMapSettings(maps.toArray(new String[maps.size()]));
		dtm.getNexus().getEditModeHandler().getPendingList().clear();
	}
}
