package dtmproject.playerdata;

import java.text.NumberFormat;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class DTMSeasonStats {
	@Getter
	private final UUID uuid;

	@Getter
	private final int season;

	@Getter
	@Setter
	private int kills, deaths, wins, losses, longestKillStreak;

	@Getter
	@Setter
	private long playTimeWon, playTimeLost;

	@Getter
	@Setter
	private int monuments;

	/**
	 * Default constructor. Set's everything to 0.
	 */
	public DTMSeasonStats(UUID uuid, int season) {
		this(uuid, season, 0, 0, 0, 0, 0, 0, 0, 0);
	}

	public double getKDRatio() {
		NumberFormat f = NumberFormat.getInstance();
		f.setMaximumFractionDigits(2);
		f.setMinimumFractionDigits(2);

		String KD = f.format((double) kills / (double) deaths);
		if (kills < 1 || deaths < 1)
			KD = "0.00";
		return Double.parseDouble(KD);
	}

	@Override
	public String toString() {
		String str = "";
		str += "§bTapot: " + kills + "\n";
		str += "§bKuolemat: " + deaths + "\n";
		str += "§bMonumentteja tuhottu: " + monuments + "\n";
		str += "§bVoitot: " + wins + "\n";
		str += "§bHäviöt: " + losses + "\n";
		str += "§bPelejä voitettu: " + playTimeWon / 1000 / 60 / 60 + " tuntia\n";
		str += "§bPelejä hävitty: " + playTimeLost / 1000 / 60 / 60 + " tuntia\n";
		return str;
	}

	public int getSum() {
		int sum = 0;
		sum += kills * 3;
		sum += deaths;
		sum += monuments * 10;
		sum += playTimeWon / 1000 / 60 * 5;
		sum += playTimeLost / 1000 / 60;
		return sum;
	}

}
