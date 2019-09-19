package com.juubes.dtmproject.playerdata;

import java.text.NumberFormat;
import java.util.UUID;

import com.juubes.nexus.data.AbstractSeasonStats;

public class DTMSeasonStats extends AbstractSeasonStats {

	public int monuments;

	public DTMSeasonStats(int statsID, UUID uuid, int season) {
		this(statsID, uuid, season, 0, 0, 0, 0, 0, 0, 0, 0);
	}

	public DTMSeasonStats(int statsID, UUID uuid, int season, int kills, int deaths, int monuments, int wins,
			int losses, int playTimeWon, int playTimeLost, int biggestKillStreak) {
		super(statsID, uuid, season, kills, deaths, wins, losses, playTimeWon, playTimeLost, biggestKillStreak);
		this.monuments = monuments;
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
