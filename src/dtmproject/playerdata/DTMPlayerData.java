package dtmproject.playerdata;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;

import dtmproject.DTM;
import dtmproject.setup.DTMTeam;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

public class DTMPlayerData {
	@Getter
	private final DTM dtm;

	@Getter
	private final UUID uuid;

	@Getter
	@Setter
	private String lastSeenName, prefix;

	@Getter
	@Setter
	private Optional<UUID> lastDamager, lastMessager;

	@Getter
	@Setter
	private DTMTeam team;

	@Getter
	@Setter
	private int emeralds, killStreak;

	@Getter
	@Setter
	private boolean autoJoin;

	/**
	 * Maps season number to stats.
	 */
	protected final HashMap<Integer, DTMSeasonStats> seasonStats;

	@Getter
	@Setter
	private long lastRespawn;

	public DTMPlayerData(DTM dtm, UUID uuid, String lastSeenName) {
		this(dtm, uuid, lastSeenName, 0, null, 0, new );p
	}

	// TODO: injektaa plugin instanssi
	public DTMPlayerData(DTM dtm, UUID uuid, String lastSeenName, int emeralds, String prefix, int killStreak,
			HashMap<Integer, DTMSeasonStats> seasonStats) {
		this.dtm = dtm;
		this.uuid = uuid;
		this.lastSeenName = lastSeenName;
		this.prefix = prefix;
		this.emeralds = emeralds;
		this.killStreak = killStreak;
		this.seasonStats = seasonStats;
	}

	public DTMSeasonStats getSeasonStats() {
		return seasonStats.get(dtm.getSeason());
	}

	public DTMTotalStats getTotalStats() {
		return new DTMTotalStats(this.uuid, this.seasonStats);
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
		str += " §a§lKausi:\n";
		str += "§b" + this.getSeasonStats().toString();
		str += " §a§lYhteensä:\n";
		str += "§b" + totalStats.toString();
		str += "§bEmeraldeja: " + emeralds;
		return str;
	}

	public boolean isSpectator() {
		return this.team == null;
	}
}
