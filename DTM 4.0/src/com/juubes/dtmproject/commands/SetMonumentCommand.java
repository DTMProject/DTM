package com.juubes.dtmproject.commands;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;
import com.juubes.dtmproject.DTM;
import com.juubes.dtmproject.setup.Monument;
import com.juubes.nexus.Nexus;
import com.juubes.nexus.commands.EditModeHandler;

public class SetMonumentCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (!sender.isOp()) {
			sender.sendMessage("§eTämä komento on operaattoreille.");
			return true;
		}

		if (!(sender instanceof Player)) {
			sender.sendMessage("§eMihi sie oikee katot?");
			return true;
		}

		Player p = (Player) sender;
		String editMode = EditModeHandler.getEditWorld(p);
		if (args.length == 0) {
			sender.sendMessage("§c/setmonument <team ID> <l|m|r|bl|bm|mr|...> <custom name...>");
		} else if (args.length == 1) {
			sender.sendMessage("§c/setmonument " + args[0].toLowerCase()
					+ " <l|m|r|bl|bm|mr|...> <custom name...>");
		} else if (args.length == 2) {
			sender.sendMessage("§c/setmonument " + args[0].toLowerCase() + " " + args[1]
					.toLowerCase() + " <custom name...>");
		} else if (args.length > 2) {
			String teamID = args[0].toLowerCase();
			String pos = args[1].toLowerCase();
			String customName = Joiner.on(' ').join(args).substring(args[0].length() + 2 + args[1]
					.length());

			if (!Nexus.getDatabaseManager().isMapCreated(editMode)) {
				p.sendMessage("§e" + editMode + " ei ole vielä luotu. /createmap");
				return true;
			}

			boolean found = false;
			for (String team : Nexus.getDatabaseManager().getTeamList(editMode)) {
				if (team.equals(teamID))
					found = true;
			}
			if (!found) {
				sender.sendMessage("§eTiimiä " + args[0].toLowerCase() + " ei ole olemassa.");
				return true;
			}

			Monument mon = new Monument(p.getTargetBlock((Set<Material>) null, 10), pos,
					customName);
			DTM.getDatabaseManager().saveMonument(editMode, teamID, mon.position, mon);
			sender.sendMessage("§eMonumentti mapille " + editMode + " tallennettu.");
			EditModeHandler.pendingList.add(sender);
		}
		return true;
	}
}
