package dtmproject.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scoreboard.Team;

import dtmproject.DTM;
import dtmproject.setup.DTMTeam;
import dtmproject.setup.Monument;
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

				sender.sendMessage("§eSettings reloaded.");
			} else if (args[0].equalsIgnoreCase("status")) {
				sender.sendMessage(ChatColor.AQUA + "DTM gamestatus: " + ChatColor.GREEN + dtm.getLogicHandler()
						.getGameState().name());
				sender.sendMessage(ChatColor.AQUA + "  Teams:");
				for (Team nexusTeam : dtm.getGameWorldHandler().getCurrentMap().getTeams()) {
					DTMTeam team = (DTMTeam) nexusTeam;
					sender.sendMessage("    " + team.getChatColor() + ChatColor.BOLD + team.getDisplayName());
					for (Monument mon : team.getMonuments()) {
						sender.sendMessage("        " + team.getChatColor() + mon.customName);
					}
					sender.sendMessage("");
				}
			} else if (args[0].equalsIgnoreCase("save")) {
				sender.sendMessage("§eSaving maps...");
				for (String mapID : dtm.getDataHandler().getMaps()) {
					dtm.getDataHandler().saveMapSettings(mapID);
					sender.sendMessage("§eSettings saved for map: " + mapID);
				}
				reloadConfig();
				sender.sendMessage("§eConfig reloaded.");

			}
		}
		return true;
	}

	public void reloadConfig() {
		dtm.reloadConfig();
		List<String> maps = dtm.getConfig().getStringList("maps");
		dtm.getDataHandler().prepareMapSettings(maps.toArray(new String[maps.size()]));
		dtm.getEditModeHandler().getPendingList().clear();
	}
}
