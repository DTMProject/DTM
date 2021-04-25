package dtmproject.common.data;

import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "SeasonStats")
public class DTMSeasonStats implements IDTMSeasonStats {

    @Getter
    @DatabaseField(columnName = "UUID", canBeNull = false)
    private final UUID UUID;

    @Getter
    @DatabaseField(columnName = "Season", id = true, canBeNull = false)
    private final int season;

    @Getter
    @DatabaseField(columnName = "Kills")
    private int kills;

    @Getter
    @DatabaseField(columnName = "Deaths")
    private int deaths;

    @Getter
    @DatabaseField(columnName = "Wins")
    private int wins;

    @Getter
    @DatabaseField(columnName = "Losses")
    private int losses;

    @Getter
    @Setter
    @DatabaseField(columnName = "LongestKillStreak")
    private int longestKillStreak;

    @Getter
    @DatabaseField(columnName = "PlayTimeWon")
    private long playTimeWon;

    @Getter
    @DatabaseField(columnName = "PlayTimeLost")
    private long playTimeLost;

    @Getter
    @DatabaseField(columnName = "MonumentsDestroyed")
    private int monumentsDestroyed;

    /**
     * Default constructor. Set's everything to 0.
     */
    public DTMSeasonStats(UUID uuid, int season) {
	this(uuid, season, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public DTMSeasonStats(UUID uuid, int season, int kills, int deaths, int wins, int losses, int longestKillStreak,
	    long playTimeWon, long playTimeLost, int monumentsDestroyed) {
	this.UUID = uuid;
	this.season = season;
	this.kills = kills;
	this.deaths = deaths;
	this.wins = wins;
	this.losses = losses;
	this.longestKillStreak = longestKillStreak;
	this.playTimeWon = playTimeWon;
	this.playTimeLost = playTimeLost;
	this.monumentsDestroyed = monumentsDestroyed;
    }

    @Override
    public String toString() {
	String str = "";
	str += "§eTapot: " + kills + "\n";
	str += "§eKuolemat: " + deaths + "\n";
	str += "§eMonumentteja tuhottu: " + monumentsDestroyed + "\n";
	str += "§eVoitot: " + wins + "\n";
	str += "§eHäviöt: " + losses + "\n";
	str += "§ePelejä voitettu: " + playTimeWon / 1000 / 60 / 60 + " tuntia\n";
	str += "§ePelejä hävitty: " + playTimeLost / 1000 / 60 / 60 + " tuntia\n";
	return str;
    }

    public int getSum() {
	int sum = 0;
	sum += kills * 3;
	sum += deaths;
	sum += monumentsDestroyed * 10;
	sum += playTimeWon / 1000 / 60 * 5;
	sum += playTimeLost / 1000 / 60;
	return sum;
    }

    public void increaseKills() {
	kills++;
    }

    public void increaseDeaths() {
	deaths++;
    }

    public void increaseWins() {
	wins++;
    }

    public void increaseLosses() {
	losses++;
    }

    public void increasePlayTimeWon(long time) {
	playTimeWon += time;
    }

    public void increasePlayTimeLost(long time) {
	playTimeLost += time;
    }

    public void increaseMonumentsDestroyed() {
	monumentsDestroyed++;
    }

}
