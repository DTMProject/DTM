package dtmproject.data;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.UUID;

import lombok.Getter;

public class DTMTotalStats {
    @Getter
    private final UUID uuid;

    private final HashMap<Integer, ? extends DTMSeasonStats> allStats;

    public DTMTotalStats(UUID uuid, HashMap<Integer, DTMSeasonStats> allStats) {
	this.uuid = uuid;
	this.allStats = allStats;

    }

    public int getMonuments() {
	int sum = 0;
	for (DTMSeasonStats s : allStats.values()) {
	    sum += s.getMonumentsDestroyed();
	}
	return sum;
    }

    public int getSum() {
	int sum = 0;
	for (DTMSeasonStats stats : allStats.values()) {
	    sum += ((DTMSeasonStats) stats).getSum();
	}
	return sum;
    }

    public int getKills() {
	int sum = 0;
	for (DTMSeasonStats stats : allStats.values()) {
	    sum += stats.getKills();
	}
	return sum;
    }

    public int getBiggestKillStreak() {
	int biggest = 0;
	for (DTMSeasonStats stats : allStats.values()) {
	    biggest = Math.max(biggest, stats.getLongestKillStreak());
	}
	return biggest;
    }

    public int getDeaths() {
	int sum = 0;
	for (DTMSeasonStats stats : allStats.values()) {
	    sum += stats.getDeaths();
	}
	return sum;
    }

    public int getWins() {
	int sum = 0;
	for (DTMSeasonStats stats : allStats.values()) {
	    sum += stats.getWins();
	}
	return sum;
    }

    public int getLosses() {
	int sum = 0;
	for (DTMSeasonStats stats : allStats.values()) {
	    sum += stats.getLosses();
	}
	return sum;
    }

    public long getPlayTimeWon() {
	int sum = 0;
	for (DTMSeasonStats stats : allStats.values()) {
	    sum += stats.getPlayTimeWon();
	}
	return sum;
    }

    public long getPlayTimeLost() {
	int sum = 0;
	for (DTMSeasonStats stats : allStats.values()) {
	    sum += stats.getPlayTimeLost();
	}
	return sum;
    }

    public double getKDRatio() {
	NumberFormat f = NumberFormat.getInstance();
	f.setMaximumFractionDigits(2);
	f.setMinimumFractionDigits(2);

	int kills = this.getKills();
	int deaths = this.getDeaths();

	String KD = f.format((double) kills / (double) deaths);
	if (kills < 1 || deaths < 1)
	    KD = "0.00";
	return Double.parseDouble(KD);
    }

    @Override
    public String toString() {
	String str = "";
	str += "§eTapot: " + getKills() + "\n";
	str += "§eKuolemat: " + getDeaths() + "\n";
	str += "§eMonumentteja tuhottu: " + getMonuments() + "\n";
	str += "§eVoitot: " + getWins() + "\n";
	str += "§eHäviöt: " + getLosses() + "\n";
	str += "§ePelejä voitettu: " + getPlayTimeWon() / 1000 / 60 / 60 + " tuntia\n";
	str += "§ePelejä hävitty: " + getPlayTimeLost() / 1000 / 60 / 60 + " tuntia\n";
	return str;
    }
}