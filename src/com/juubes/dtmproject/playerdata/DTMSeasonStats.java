package com.juubes.dtmproject.playerdata;

import java.text.NumberFormat;
import java.util.UUID;

import com.juubes.nexus.data.AbstractStats;

public class DTMSeasonStats extends AbstractStats {

	public int kills, deaths;
	public int wins, losses;
	public int monuments;

	public DTMSeasonStats(int statsID, UUID uuid, int season) {
		super(statsID, uuid, season);
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
		str += "�bTapot: " + kills + "\n";
		str += "�bKuolemat: " + deaths + "\n";
		str += "�bMonumenttej� tuhottu: " + monuments + "\n";
		str += "�bVoitot: " + wins + "\n";
		str += "�bH�vi�t: " + losses + "\n";
		str += "�bPelej� voitettu: " + playTimeWon / 1000 / 60 + " minuuttia\n";
		str += "�bPelej� h�vitty: " + playTimeLost / 1000 / 60 + " minuuttia\n";
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
