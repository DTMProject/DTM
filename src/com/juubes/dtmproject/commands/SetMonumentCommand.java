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

public class SetMonumentCommand implements CommandExecutor {
	private final DTM dtm;

	public SetMonumentCommand(DTM dtm) {
		this.dtm = dtm;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (!sender.isOp()) {
			sender.sendMessage("�eT�m� komento on operaattoreille.");
			return true;
		}

		if (!(sender instanceof Player)) {
			sender.sendMessage("�eMihi sie oikee katot?");
			return true;
		}

		Player p = (Player) sender;
		String editMode = dtm.getNexus().getEditModeHandler().getEditWorld(p);
		if (args.length == 0) {
			sender.sendMessage("�c/setmonument <team ID> <l|m|r|bl|bm|mr|...> <custom name...>");
		} else if (args.length == 1) {
			sender.sendMessage("�c/setmonument " + args[0].toLowerCase() + " <l|m|r|bl|bm|mr|...> <custom name...>");
		} else if (args.length == 2) {
			sender.sendMessage("�c/setmonument " + args[0].toLowerCase() + " " + args[1].toLowerCase()
					+ " <custom name...>");
		} else if (args.length > 2) {
			String teamID = args[0].toLowerCase();
			String pos = args[1].toLowerCase();
			String customName = Joiner.on(' ').join(args).substring(args[0].length() + 2 + args[1].length());

			if (!dtm.getDatabaseManager().isMapCreated(editMode)) {
				p.sendMessage("�e" + editMode + " ei ole viel� luotu. /createmap");
				return true;
			}

			boolean found = false;
			for (String team : dtm.getDatabaseManager().getTeamList(editMode)) {
				if (team.equals(teamID))
					found = true;
			}
			if (!found) {
				sender.sendMessage("�eTiimi� " + args[0].toLowerCase() + " ei ole olemassa.");
				return true;
			}

			Monument mon = new Monument(p.getTargetBlock((Set<Material>) null, 10), pos, customName);
			dtm.getDatabaseManager().saveMonument(editMode, teamID, mon.position, mon);
			sender.sendMessage("�eMonumentti mapille " + editMode + " tallennettu.");
			dtm.getNexus().getEditModeHandler().getPendingList().add(sender);
		}
		return true;
	}
}
