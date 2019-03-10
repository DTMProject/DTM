package com.juubes.dtmproject.commands;

import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.juubes.dtmproject.DTM;
import com.juubes.dtmproject.playerdata.DTMStats;
import com.juubes.nexus.Nexus;

public class TopCommand implements CommandExecutor {

	public TopCommand() {
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {

		int count = 10;
		if (args.length > 0) {
			try {
				count = Integer.parseInt(args[0]);
			} catch (Exception e) {
				sender.sendMessage("/top [määrä] [season]");
			}
		}

		int season = Nexus.CURRENT_SEASON;
		if (args.length > 1) {
			try {
				season = Integer.parseInt(args[1]);
			} catch (Exception e) {
				sender.sendMessage("/top " + count + " [season]");
			}
		}

		sender.sendMessage("§eParhaat pelaajat " + season + ". kaudelta: ");
		LinkedList<DTMStats> topStats = DTM.getDatabaseManager().getLeaderboard(count, season);
		int i = 1;
		for (DTMStats stats : topStats) {
			OfflinePlayer p = Bukkit.getOfflinePlayer(stats.getUUID());
			if (p != null) {
				String name;

				if (p.isOnline())
					name = p.getPlayer().getDisplayName();
				else
					name = p.getName();
				sender.sendMessage("§e" + (i++) + ". " + name + ": §a" + stats.getSum() + " §c" + stats.kills + " §4"
						+ stats.deaths + " §7" + stats.getKDRatio());
			}
		}

		sender.sendMessage("");
		return true;
	}
}
