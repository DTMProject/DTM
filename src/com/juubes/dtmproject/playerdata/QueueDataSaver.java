package com.juubes.dtmproject.playerdata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.bukkit.Bukkit;

import com.juubes.dtmproject.DTM;

public class QueueDataSaver {
	private Queue<DTMPlayerData> queuedData = new LinkedBlockingQueue<>();
	private final DTM dtm;

	public static final String SAVE_STATS_SQL = "INSERT INTO `SeasonStats`(`UUID`, `Season`, `Kills`, `Deaths`, `MonumentsDestroyed`, `Wins`, `Losses`, `PlayTimeWon`, `PlayTimeLost`, `LongestKillStreak`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE Kills = VALUES(Kills), Deaths = VALUES(Deaths), MonumentsDestroyed= VALUES(MonumentsDestroyed), Wins = VALUES(Wins), Losses = VALUES(Losses), PlayTimeWon = VALUES(PlayTimeWon), PlayTimeLost = VALUES(PlayTimeLost), LongestKillStreak = VALUES(LongestKillStreak)";
	public static final String SAVE_PLAYERDATA_SQL = "INSERT INTO PlayerData (UUID, LastSeenName, Prefix, Emeralds, KillStreak, EloRating) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE LastSeenName = VALUES(LastSeenName), Emeralds = VALUES(Emeralds), Prefix = VALUES(Prefix), KillStreak = VALUES(KillStreak), EloRating = VALUES(EloRating)";

	public QueueDataSaver(DTM dtm) {
		this.dtm = dtm;
	}

	public void init() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(dtm, () -> {
			if (queuedData.size() == 0)
				return;
			try (Connection conn = dtm.getDataHandler().getHDS().getConnection()) {
				DTMPlayerData data;
				try (PreparedStatement stmt1 = conn.prepareStatement(SAVE_PLAYERDATA_SQL);
						PreparedStatement stmt2 = conn.prepareStatement(SAVE_STATS_SQL)) {

					while ((data = queuedData.poll()) != null) {
						System.out.println("Saving playrerdata for " + data.lastSeenName);
						stmt1.setString(1, data.uuid.toString());
						stmt1.setString(2, data.lastSeenName);
						stmt1.setString(3, data.prefix);
						stmt1.setInt(4, data.emeralds);
						stmt1.setInt(5, data.killStreak);
						stmt1.setDouble(6, data.eloRating);
						stmt1.addBatch();

						// TODO: Only saves current season
						DTMSeasonStats stats = data.seasonStats.get(dtm.getSeason());
						stmt2.setString(1, stats.uuid.toString());
						stmt2.setInt(2, dtm.getSeason());
						stmt2.setInt(3, stats.kills);
						stmt2.setInt(4, stats.deaths);
						stmt2.setInt(5, stats.monuments);
						stmt2.setInt(6, stats.wins);
						stmt2.setInt(7, stats.losses);
						stmt2.setLong(8, stats.playTimeWon);
						stmt2.setLong(9, stats.playTimeLost);
						stmt2.setInt(10, stats.longestKillStreak);
						stmt2.addBatch();
					}
					stmt1.executeBatch();
					stmt2.executeBatch();

				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}, 20, 20);

	}

	public void queue(DTMPlayerData data) {
		queuedData.offer(data);
	}

	public boolean isSavingDataFor(UUID uuid) {
		for (DTMPlayerData data : queuedData) {
			if (data.uuid == uuid)
				return true;
		}
		return false;
	}

}
