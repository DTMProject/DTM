package com.juubes.dtmproject.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.juubes.dtmproject.playerdata.DTMPlayerData;
import com.juubes.nexus.playerdata.AbstractPlayerData;
import com.juubes.nexus.playerdata.PlayerDataHandler;

public class TopCommand implements CommandExecutor {

	private DTMPlayerData[] leaderboardCache;

	public TopCommand() {
		// Calculate leaderboard for start
		leaderboardCache = new DTMPlayerData[100];
		List<DTMPlayerData> allData = new ArrayList<>();
		// TODO: do it for offline data
		for (AbstractPlayerData data : PlayerDataHandler.getLoadedData()) {
			allData.add((DTMPlayerData) data);
		}
		allData.sort(new Comparator<DTMPlayerData>() {
			@Override
			public int compare(DTMPlayerData pd, DTMPlayerData pd2) {
				return pd.getSeasonStats().getSum() - pd2.getSeasonStats().getSum();
			}
		});
		for (int i = 0; i < 100; i++) {
			if (i == allData.size())
				break;
			leaderboardCache[i] = allData.get(i);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (args.length == 0) {
			// Just print the current seasons top 10
			for (int i = 0; i < 10; i++) {
				if (leaderboardCache.length == i)
					break;
				DTMPlayerData pd = leaderboardCache[i];
				sender.sendMessage("§e" + (i + 1) + ". " + pd.getName() + ": " + pd.getSeasonStats()
						.getSum());
			}
		} else if (args.length == 1) {

		}
		return true;
	}
}
