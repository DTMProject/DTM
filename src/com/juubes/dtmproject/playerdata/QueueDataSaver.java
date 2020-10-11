package com.juubes.dtmproject.playerdata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bukkit.Bukkit;

import com.juubes.dtmproject.DTM;

public class QueueDataSaver {
	private Queue<DTMPlayerData> queuedData = new LinkedBlockingQueue<>();
	private final DTM dtm;

	public QueueDataSaver(DTM dtm) {
		this.dtm = dtm;
	}

	public void init() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(dtm, () -> {
			if (queuedData.size() == 0)
				return;
			try (Connection conn = dtm.getDatabaseManager().getHDS().getConnection()) {
				DTMPlayerData data;
				while ((data = queuedData.poll()) != null) {
					System.out.println("Saving playerdata for " + data.getLastSeenName());
					try (PreparedStatement stmt = conn.prepareStatement(
							"INSERT INTO PlayerData (UUID, LastSeenName, Emeralds, KillStreak) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE LastSeenName = VALUES(LastSeenName), Emeralds = VALUES(Emeralds), KillStreak = VALUES(KillStreak)")) {
						stmt.setString(1, data.getUUID().toString());
						stmt.setString(2, data.getLastSeenName());
						stmt.setInt(3, data.getEmeralds());
						stmt.setInt(4, data.getKillStreak());
						stmt.execute();
					} catch (SQLException e) {
						e.printStackTrace();
						queuedData.offer(data);
						break;
					}
					DTMSeasonStats stats = data.getSeasonStats();
					try (PreparedStatement stmt = conn.prepareStatement(
							"INSERT INTO `SeasonStats`(`UUID`, `Season`, `Kills`, `Deaths`, `MonumentsDestroyed`, `Wins`, `Losses`, `PlayTimeWon`, `PlayTimeLost`, `LongestKillStreak`)"
									+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
									+ "Kills = VALUES(Kills), Deaths = VALUES(Deaths), MonumentsDestroyed= VALUES(MonumentsDestroyed), Wins = VALUES(Wins), Losses = VALUES(Losses), PlayTimeWon = VALUES(PlayTimeWon), PlayTimeLost = VALUES(PlayTimeLost), LongestKillStreak = VALUES(LongestKillStreak)")) {
						stmt.setString(1, stats.getUUID().toString());
						stmt.setInt(2, dtm.getNexus().getCurrentSeason());
						stmt.setInt(3, stats.kills);
						stmt.setInt(4, stats.deaths);
						stmt.setInt(5, stats.monuments);
						stmt.setInt(6, stats.wins);
						stmt.setInt(7, stats.losses);
						stmt.setLong(8, stats.playTimeWon);
						stmt.setLong(9, stats.playTimeLost);
						stmt.setInt(10, stats.longestKillStreak);
						stmt.execute();
					} catch (SQLException e) {
						e.printStackTrace();
						queuedData.offer(data);
						break;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}, 20, 20);
	}

	public void queue(DTMPlayerData data) {
		queuedData.offer(data);
	}

}
