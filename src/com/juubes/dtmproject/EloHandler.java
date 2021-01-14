package com.juubes.dtmproject;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.juubes.dtmproject.playerdata.DTMPlayerData;
import com.juubes.nexus.logic.Team;

public class EloHandler {
	private final LinkedList<UUID> levelTop = new LinkedList<>();
	private final DTM pl;

	public EloHandler(DTM pl) {
		this.pl = pl;
		Bukkit.getScheduler().runTaskTimerAsynchronously(pl, pl.getDatabaseManager()::getLevelTop, 0, 20 * 60 * 1);
	}

	public static int getEloRank(double eloRating) {
		return (int) (eloRating / 100);
	}

	/*
	 * TODO: Assumes there are only two teams
	 */
	public void updateEloRating(Team winner, Team[] allTeams) {
		Team loser = allTeams[0];
		if (winner == loser)
			loser = allTeams[1];

		List<Player> winnerPlayers = winner.getPlayers();
		double avgEloWinners = 0;
		for (Player p : winnerPlayers) {
			DTMPlayerData pd = pl.getDatabaseManager().getPlayerData(p);
			avgEloWinners += pd.getEloRating();
		}
		avgEloWinners /= winnerPlayers.size();

		List<Player> loserPlayers = loser.getPlayers();
		double avgEloLosers = 0;
		for (Player p : loserPlayers) {
			DTMPlayerData pd = pl.getDatabaseManager().getPlayerData(p);
			avgEloLosers += pd.getEloRating();
		}
		avgEloLosers /= loserPlayers.size();

		double winPossibility = 1.0f * 1.0f / (1 + 1.0f * (float) (Math.pow(10, 1.0f * (avgEloWinners - avgEloLosers)
				/ 400)));
		double lossPossibility = 1.0f * 1.0f / (1 + 1.0f * (float) (Math.pow(10, 1.0f * (avgEloLosers - avgEloWinners)
				/ 400)));
		
		if (winner == winner)
		{
		}

	}

}
