package dtmproject.commands;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;
import com.juubes.nexus.Lang;

import dtmproject.DTM;
import dtmproject.NexusBlockLocation;
import dtmproject.setup.Monument;
import net.md_5.bungee.api.ChatColor;

public class SetMonumentCommand implements CommandExecutor {
	private final DTM dtm;

	public SetMonumentCommand(DTM dtm) {
		this.dtm = dtm;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (!sender.isOp()) {
			sender.sendMessage(Lang.get("no-permission"));
			return true;
		}

		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.GOLD + "Oof I am so confused now.");
			return true;
		}

		Player p = (Player) sender;
		String editMode = dtm.getEditModeHandler().getEditWorld(p);
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "/setmonument <team ID> <l|m|r|bl|bm|mr|...> <custom name...>");
		} else if (args.length == 1) {
			sender.sendMessage(ChatColor.RED + "/setmonument " + args[0].toLowerCase()
					+ " <l|m|r|bl|bm|mr|...> <custom name...>");
		} else if (args.length == 2) {
			sender.sendMessage(ChatColor.RED + "/setmonument " + args[0].toLowerCase() + " " + args[1].toLowerCase()
					+ " <custom name...>");
		} else if (args.length > 2) {
			String teamID = args[0].toLowerCase();
			String pos = args[1].toLowerCase();
			String customName = Joiner.on(' ').join(args).substring(args[0].length() + 2 + args[1].length());

			if (!dtm.getDataHandler().isMapCreated(editMode)) {
				p.sendMessage(String.format(Lang.get("map-not-created"), editMode));
				return true;
			}

			boolean found = false;
			for (String team : dtm.getDataHandler().getTeamList(editMode)) {
				if (team.equals(teamID))
					found = true;
			}
			if (!found) {
				sender.sendMessage(String.format(Lang.get("team-not-exist"), args[0].toLowerCase()));
				return true;
			}

			Monument mon = new Monument(new NexusBlockLocation(p.getTargetBlock((Set<Material>) null, 10)), pos,
					customName);
			dtm.getDataHandler().saveMonument(editMode, teamID, mon.position, mon);
			sender.sendMessage(Lang.get("monument-saved"));
			dtm.getEditModeHandler().getPendingList().add(sender);
		}
		return true;
	}
}
