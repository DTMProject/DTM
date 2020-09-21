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
import com.juubes.nexus.data.AbstractSeasonStats;

public class TopCommand implements CommandExecutor {

	private final DTM dtm;

	public TopCommand(DTM dtm) {
		this.dtm = dtm;
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

		int season = dtm.getNexus().getCurrentSeason();
		if (args.length > 1) {
			try {
				season = Integer.parseInt(args[1]);
			} catch (Exception e) {
				sender.sendMessage("/top " + count + " [season]");
			}
		}

		sender.sendMessage("§eParhaat pelaajat " + season + ". kaudelta: ");
		LinkedList<? extends AbstractSeasonStats> topStats = dtm.getDatabaseManager().getLeaderboard(count, season);
		int i = 1;
		for (AbstractSeasonStats s : topStats) {
			DTMSeasonStats stats = (DTMSeasonStats) s;
			DTMPlayerData pd = dtm.getDatabaseManager().getPlayerData(stats.getUUID());
			Player p = Bukkit.getPlayer(pd.getUUID());
			String name;
			if (p != null && p.isOnline())
				name = p.getDisplayName();
			else
				name = pd.getNick();
			sender.sendMessage("§e" + (i++) + ". " + name + ": §a" + stats.getSum() + " §c" + stats.kills + " §4"
					+ stats.deaths + " §7" + stats.getKDRatio());
		}

		sender.sendMessage("");
		return true;
	}
}
