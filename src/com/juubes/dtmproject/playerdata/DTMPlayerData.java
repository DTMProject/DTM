package com.juubes.dtmproject.playerdata;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.juubes.nexus.Nexus;
import com.juubes.nexus.data.AbstractPlayerData;

public abstract class DTMPlayerData extends AbstractPlayerData {

	private long lastRespawn;

	public DTMPlayerData(Nexus nexus, UUID uuid, String lastSeenName, String prefix, int emeralds, String nick,
			int killStreak) {
		super(nexus, uuid, lastSeenName, prefix, emeralds, nick, killStreak);
	}

	@Override
	public abstract DTMSeasonStats getTotalStats();

	@Override
	public abstract DTMSeasonStats getSeasonStats();

	@Override
	public abstract DTMSeasonStats getSeasonStats(int season);

	@Override
	public String toString() {
		DTMSeasonStats totalStats = this.getTotalStats();

		String str = "";
		if (Bukkit.getPlayer(uuid) != null)
			str += "�b" + uuid.toString() + ": " + Bukkit.getPlayer(uuid).getName() + "\n";
		else
			str += "�b" + uuid.toString() + ": " + Bukkit.getOfflinePlayer(uuid).getName() + " \n";
		if (prefix != null)
			str += "�bPrefix: " + ChatColor.translateAlternateColorCodes('&', this.getPrefix()) + "\n";
		else
			str += "�bEi prefixi�\n";
		str += "�bTappoputki: " + killStreak + "\n";
		str += "�bK/D-ratio: " + this.getSeasonStats().getKDRatio() + " - " + totalStats.getKDRatio() + "\n";
		str += "  �a�lKausi:\n";
		str += "�b" + this.getSeasonStats().toString();
		str += "  �a�lYhteens�:\n";
		str += "�b" + totalStats.toString();
		str += "�bEmeraldeja: " + emeralds;
		return str;
	}

	public void setLastRespawn(long currentTimeMillis) {
		this.lastRespawn = currentTimeMillis;
	}

	public long getLastRespawn() {
		return lastRespawn;
	}
}
