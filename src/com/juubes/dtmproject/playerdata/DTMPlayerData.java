package com.juubes.dtmproject.playerdata;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.juubes.nexus.Nexus;
import com.juubes.nexus.data.AbstractPlayerData;

public class DTMPlayerData extends AbstractPlayerData {
	private final HashMap<Integer, DTMSeasonStats> seasonStats = new HashMap<>();
	private final DTMTotalStats totalStats = new DTMTotalStats(uuid, seasonStats);

	private long lastRespawn;

	/**
	 * Creates new playerdata for player, defaults all other values.
	 */
	public DTMPlayerData(Nexus nexus, UUID uuid, String lastSeenName) {
		super(nexus, uuid, lastSeenName, null, 0, null, 0);
	}

	public DTMPlayerData(Nexus nexus, UUID uuid, String lastSeenName, String prefix, int emeralds, String nick,
			int killStreak) {
		super(nexus, uuid, lastSeenName, prefix, emeralds, nick, killStreak);
	}

	@Override
	public DTMTotalStats getTotalStats() {
		return totalStats;
	}

	/**
	 * @return season stats for the current season.
	 */
	@Override
	public DTMSeasonStats getSeasonStats() {
		return seasonStats.get(nexus.getCurrentSeason());
	}

	@Override
	public DTMSeasonStats getSeasonStats(int season) {
		return seasonStats.get(season);
	}

	@Override
	public String toString() {
		DTMTotalStats totalStats = this.getTotalStats();

		String str = "";
		if (Bukkit.getPlayer(uuid) != null)
			str += "§b" + uuid.toString() + ": " + Bukkit.getPlayer(uuid).getName() + "\n";
		else
			str += "§b" + uuid.toString() + ": " + Bukkit.getOfflinePlayer(uuid).getName() + " \n";
		if (prefix != null)
			str += "§bPrefix: " + ChatColor.translateAlternateColorCodes('&', this.getPrefix()) + "\n";
		else
			str += "§bEi prefixiä\n";
		str += "§bTappoputki: " + killStreak + "\n";
		str += "§bK/D-ratio: " + this.getSeasonStats().getKDRatio() + " - " + totalStats.getKDRatio() + "\n";
		str += "  §a§lKausi:\n";
		str += "§b" + this.getSeasonStats().toString();
		str += "  §a§lYhteensä:\n";
		str += "§b" + totalStats.toString();
		str += "§bEmeraldeja: " + emeralds;
		return str;
	}

	public void setLastRespawn(long currentTimeMillis) {
		this.lastRespawn = currentTimeMillis;
	}

	public long getLastRespawn() {
		return lastRespawn;
	}

	public void loadSeasonStats(HashMap<Integer, DTMSeasonStats> loadedSeasonStats) {
		for (Entry<Integer, DTMSeasonStats> e : loadedSeasonStats.entrySet()) {
			this.seasonStats.put(e.getKey(), e.getValue());
		}
		if (!this.seasonStats.containsKey(nexus.getCurrentSeason()))
			this.seasonStats.put(nexus.getCurrentSeason(), new DTMSeasonStats(this.uuid, nexus.getCurrentSeason()));
	}
}
