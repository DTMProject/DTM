package com.juubes.dtmproject.playerdata;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.juubes.nexus.playerdata.AbstractPlayerData;

public abstract class DTMPlayerData extends AbstractPlayerData {

	private long lastRespawn;

	public DTMPlayerData(UUID id, Player player) {
		this.id = id;
		this.p = player;
	}

	@Override
	public abstract DTMStats getTotalStats();

	@Override
	public abstract DTMStats getSeasonStats();

	@Override
	public abstract DTMStats getSeasonStats(int season);

	@Override
	public String toString() {
		DTMStats totalStats = this.getTotalStats();

		String str = "";
		if (Bukkit.getPlayer(id) != null)
			str += "§b" + id.toString() + ": " + Bukkit.getPlayer(id).getName() + "\n";
		else
			str += "§b" + id.toString() + ": " + Bukkit.getOfflinePlayer(id).getName() + " \n";
		if (prefix != null)
			str += "§bPrefix: " + ChatColor.translateAlternateColorCodes('&', this.getPrefix())
					+ "\n";
		else
			str += "§bEi prefixiä\n";
		str += "§bTappoputki: " + killStreak + "\n";
		str += "§bK/D-ratio: " + this.getSeasonStats().getKDRatio() + " - " + totalStats
				.getKDRatio() + "\n";
		str += "  §a§lKausi:\n";
		str += "§b" + this.getSeasonStats().toString();
		str += "  §a§lYhteensä:\n";
		str += "§b" + totalStats.toString();
		str += "§bEmeraldeja: " + emeralds;
		return str;
	}

	public static DTMPlayerData get(Player p) {
		return (DTMPlayerData) AbstractPlayerData.get(p);
	}

	public void setLastRespawn(long currentTimeMillis) {
		this.lastRespawn = currentTimeMillis;
	}

	public long getLastRespawn() {
		return lastRespawn;
	}
}
