package dtmproject.data;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import dtmproject.DTM;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

public class DTMPlayerData {
	@Getter
	private final DTM pl;

	@Getter
	private final UUID uuid;

	@Getter
	@Setter
	private String lastSeenName, prefix;

	@Setter
	private UUID lastDamager, lastMessager;

	@Getter
	@Setter
	private DTMTeam team;

	@Getter
	private int emeralds, killStreak;

	@Getter
	@Setter
	private boolean autoJoin;

	@Getter
	private int eloRating;

	/**
	 * Maps season number to stats.
	 */
	protected final HashMap<Integer, DTMSeasonStats> seasonStats;

	@Getter
	@Setter
	private long lastRespawn;

	public DTMPlayerData(DTM dtm, UUID uuid, String lastSeenName) {
		this(dtm, uuid, lastSeenName, 0, DTM.DEFAULT_PREFIX, 0, 1000, new HashMap<>());
	}

	// TODO: injektaa plugin instanssi
	public DTMPlayerData(DTM dtm, UUID uuid, String lastSeenName, int emeralds, String prefix, int killStreak,
			int eloRating, HashMap<Integer, DTMSeasonStats> seasonStats) {
		this.pl = dtm;
		this.uuid = uuid;
		this.lastSeenName = lastSeenName;
		this.prefix = prefix;
		this.emeralds = emeralds;
		this.killStreak = killStreak;
		this.seasonStats = seasonStats;
		this.eloRating = eloRating;

		if (getSeasonStats() == null)
			this.seasonStats.put(dtm.getSeason(), new DTMSeasonStats(uuid, dtm.getSeason()));
	}

	public DTMSeasonStats getSeasonStats() {
		return getSeasonStats(pl.getSeason());
	}

	public DTMSeasonStats getSeasonStats(int season) {
		return seasonStats.get(season);
	}

	public DTMTotalStats getTotalStats() {
		return new DTMTotalStats(this.uuid, this.seasonStats);
	}

	public boolean isSpectator() {
		return this.team == null;
	}

	public void increaseEmeralds() {
		increaseEmeralds(1);
	}

	public void increaseEmeralds(int amount) {
		emeralds += amount;
	}

	public void decreaseEmeralds(int amount) {
		this.emeralds -= amount;
	}

	public void adjustEloRating(int amount) {
		this.eloRating += amount;
	}

	public void increaseKillStreak() {
		killStreak++;
	}

	public Player getLastDamager() {
		return Bukkit.getPlayer(lastDamager);
	}

	public Player getLastMessager() {
		return Bukkit.getPlayer(lastMessager);
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

	public String getDisplayName() {
		if (isSpectator())
			return "§7" + lastSeenName + "§e";
		return team.getTeamColor() + lastSeenName + "§e";
	}

}
