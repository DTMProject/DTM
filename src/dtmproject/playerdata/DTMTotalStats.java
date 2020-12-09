package dtmproject.playerdata;

import java.util.HashMap;
import java.util.UUID;

import com.juubes.nexus.data.AbstractSeasonStats;
import com.juubes.nexus.data.AbstractTotalStats;

public class DTMTotalStats extends AbstractTotalStats {

	public DTMTotalStats(UUID uuid, HashMap<Integer, DTMSeasonStats> allStats) {
		super(uuid, allStats);
	}

	public int getMonuments() {
		int sum = 0;
		for (AbstractSeasonStats s : allStats.values()) {
			sum += ((DTMSeasonStats) s).monuments;
		}
		return sum;
	}

	public int getSum() {
		int sum = 0;
		for (AbstractSeasonStats stats : allStats.values()) {
			sum += ((DTMSeasonStats) stats).getSum();
		}
		return sum;
	}

	@Override
	public String toString() {
		String str = "";
		str += "§bTapot: " + getKills() + "\n";
		str += "§bKuolemat: " + getDeaths() + "\n";
		str += "§bMonumentteja tuhottu: " + getMonuments() + "\n";
		str += "§bVoitot: " + getWins() + "\n";
		str += "§bHäviöt: " + getLosses() + "\n";
		str += "§bPelejä voitettu: " + getPlayTimeWon() / 1000 / 60 / 60 + " tuntia\n";
		str += "§bPelejä hävitty: " + getPlayTimeLost() / 1000 / 60 / 60 + " tuntia\n";
		return str;
	}
}