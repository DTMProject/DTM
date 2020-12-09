package dtmproject.playerdata;

import java.util.HashMap;
import java.util.UUID;

import com.juubes.nexus.data.AbstractPlayerData;

public class DTMPlayerData extends AbstractPlayerData {
	public long lastRespawn;

	public final HashMap<Integer, DTMSeasonStats> seasonStats;

	/**
	 * Creates new playerdata for player, defaults all other values.
	 */
	public DTMPlayerData(UUID uuid, String lastSeenName) {
		super(uuid, lastSeenName, 0, null, 0, 1000, new HashMap<>(5));
		this.seasonStats = new HashMap<>(5);
	}

	public DTMPlayerData(UUID uuid, String lastSeenName, int emeralds, String prefix, int killStreak, double eloRating,
			HashMap<Integer, DTMSeasonStats> seasonStats) {
		super(uuid, lastSeenName, emeralds, prefix, killStreak, eloRating, seasonStats);
		this.seasonStats = new HashMap<>(5);
	}
	//
	// @Override
	// public String toString() {
	// DTMTotalStats totalStats = this.getTotalStats();
	//
	// String str = "";
	// if (Bukkit.getPlayer(uuid) != null)
	// str += "§b" + uuid.toString() + ": " + Bukkit.getPlayer(uuid).getName() +
	// "\n";
	// else
	// str += "§b" + uuid.toString() + ": " +
	// Bukkit.getOfflinePlayer(uuid).getName() + " \n";
	// if (prefix != null)
	// str += "§bPrefix: " + ChatColor.translateAlternateColorCodes('&',
	// this.getPrefix()) + "\n";
	// else
	// str += "§bEi prefixiä\n";
	// str += "§bTappoputki: " + killStreak + "\n";
	// str += "§bK/D-ratio: " + this.getSeasonStats().getKDRatio() + " - " +
	// totalStats.getKDRatio() + "\n";
	// str += " §a§lKausi:\n";
	// str += "§b" + this.getSeasonStats().toString();
	// str += " §a§lYhteensä:\n";
	// str += "§b" + totalStats.toString();
	// str += "§bEmeraldeja: " + emeralds;
	// return str;
	// }
}
