package dtmproject.common.data;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "SeasonStats")
public class DTMSeasonStats implements IDTMSeasonStats {

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    private final DTMPlayerData playerData;

    @Id
    @Column(name = "Season", nullable = false)
    @Getter
    private final int season;

    @Getter
    @Column(name = "Kills", nullable = false)
    private int kills;

    @Getter
    @Column(name = "Deaths", nullable = false)
    private int deaths;

    @Getter
    @Column(name = "Wins", nullable = false)
    private int wins;

    @Getter
    @Column(name = "Losses", nullable = false)
    private int losses;

    @Getter
    @Setter
    @Column(name = "LongestKillStreak", nullable = false)
    private int longestKillStreak;

    @Getter
    @Column(name = "PlayTimeWon", nullable = false)
    private long playTimeWon;

    @Getter
    @Column(name = "PlayTimeLost", nullable = false)
    private long playTimeLost;

    @Getter
    @Column(name = "MonumentsDestroyed", nullable = false)
    private int monumentsDestroyed;

    /**
     * Default constructor. Set's everything to 0.
     */
    public DTMSeasonStats(DTMPlayerData data, int season) {
	this(data, season, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public DTMSeasonStats(DTMPlayerData data, int season, int kills, int deaths, int wins, int losses,
	    int longestKillStreak, long playTimeWon, long playTimeLost, int monumentsDestroyed) {
	this.playerData = data;
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

    @Override
    public UUID getUUID() {
	return this.playerData.getUUID();
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof DTMSeasonStats)
	    return false;

	DTMSeasonStats that = (DTMSeasonStats) obj;
	return this.playerData == that.playerData && this.season == that.season;
    }

    @Override
    public int hashCode() {
	// TODO Auto-generated method stub
	return super.hashCode();
    }

}
