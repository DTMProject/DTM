package dtmproject.data;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;

import dtmproject.DTM;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

public class DTMPlayerData implements IDTMPlayerData<DTMTeam, DTMSeasonStats> {
    @Getter
    private final DTM pl;

    @Getter
    private final UUID UUID;

    @Getter
    @Setter
    private String lastSeenName;

    @Setter
    private String prefix;

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
	this.UUID = uuid;
	this.lastSeenName = lastSeenName;
	this.prefix = prefix;
	this.emeralds = emeralds;
	this.killStreak = killStreak;
	this.seasonStats = seasonStats;
	this.eloRating = eloRating;

	if (getSeasonStats() == null)
	    this.seasonStats.put(dtm.getSeason(), new DTMSeasonStats(uuid, dtm.getSeason()));
    }

    public HashMap<Integer, DTMSeasonStats> getAllSeasonStats() {
	return seasonStats;
    }

    public DTMSeasonStats getSeasonStats() {
	return getSeasonStats(pl.getSeason());
    }

    public DTMSeasonStats getSeasonStats(int season) {
	return seasonStats.get(season);
    }

    public DTMTotalStats getTotalStats() {
	return new DTMTotalStats(this.UUID, this.seasonStats);
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
	if (Bukkit.getPlayer(UUID) != null)
	    str += "§b" + UUID.toString() + ": " + Bukkit.getPlayer(UUID).getName() + "\n";
	else
	    str += "§b" + UUID.toString() + ": " + Bukkit.getOfflinePlayer(UUID).getName() + " \n";
	if (prefix != null)
	    str += "§bPrefix: " + ChatColor.translateAlternateColorCodes('&', prefix) + "\n";
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

    /**
     * Returns 0 if player has played less than 10 games
     */
    public double getWinLossRating() {
	DTMSeasonStats stats = getSeasonStats();
	if (stats.getWins() + stats.getLosses() < 10)
	    return 0;

	return (double) stats.getWins() / (double) stats.getLosses();
    }

    /**
     * @return a number from 1 to 10, indicating the players relative skill level.
     * 
     *         0 indicates unranked -- the player's skill level can't be evaluated.
     */
    public int getRelativeRating() {
	System.out.println("Players elo -- " + this.lastSeenName + " -- " + getWinLossRating());

	if (getWinLossRating() == 0)
	    return 0;

	Double[] winLossdist = pl.getDataHandler().getWinLossDistribution();

	for (int i = 0; i < winLossdist.length; i++) {
	    if (this.getWinLossRating() >= winLossdist[i])
		return 10 - i;
	}

	return 1;
    }

    @Override
    public Optional<String> getPrefix() {
	return Optional.ofNullable(prefix);
    }
}
