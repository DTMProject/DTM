package com.juubes.dtmproject.commands;

import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.juubes.dtmproject.DTM;
import com.juubes.dtmproject.playerdata.DTMPlayerData;
import com.juubes.dtmproject.playerdata.DTMSeasonStats;
import com.juubes.nexus.TopListEntry;

public class TopCommand implements CommandExecutor {

	private final DTM dtm;
	private LinkedList<TopListEntry> topListCache = new LinkedList<>();

	public TopCommand(DTM dtm) {
		this.dtm = dtm;

		// Every 2 minutes, get all data from mysql and sort again
		for (int i = 0; i < dtm.getNexus().getCurrentSeason(); i++) {
			Bukkit.getScheduler().runTaskTimerAsynchronously(dtm, () -> {
				topListCache = dtm.getDatabaseManager().getLeaderboard(100, dtm.getNexus().getCurrentSeason());
			}, 0, 20 * 120);
		}

	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		int count = 10;
		if (args.length > 0) {
			try {
				count = Integer.parseInt(args[0]);
			} catch (Exception e) {
				sender.sendMessage("/top [määrä] [kausi]");
			}
		}

		int season = dtm.getNexus().getCurrentSeason();
		if (args.length > 1) {
			try {
				season = Integer.parseInt(args[1]);
			} catch (Exception e) {
				sender.sendMessage("/top " + count + " [kausi]");
			}
		}

		sender.sendMessage("§eParhaat pelaajat " + season + ". kaudelta: ");
		int i = 1;
		for (TopListEntry entry : topListCache) {
			DTMSeasonStats stats = (DTMSeasonStats) entry.stats;
			Player possiblePlayer = Bukkit.getPlayer(stats.getUUID());
			String name;
			if (possiblePlayer != null && possiblePlayer.isOnline()) {
				DTMPlayerData data = dtm.getDatabaseManager().getPlayerData(possiblePlayer);
				name = data.getNick();
			} else {
				name = entry.name;
			}

			sender.sendMessage("§e" + (i++) + ". " + name + ": §a" + stats.getSum() + " §c" + stats.kills + " §4"
					+ stats.deaths + " §7" + stats.getKDRatio());
			if (i == count + 1)
				break;
		}

		sender.sendMessage("");
		return true;
	}
}
