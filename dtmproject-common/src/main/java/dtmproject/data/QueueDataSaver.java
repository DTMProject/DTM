package dtmproject.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.bukkit.Bukkit;

import dtmproject.DTM;

public class QueueDataSaver {
    private Queue<DTMPlayerData> queuedData = new LinkedBlockingQueue<>();
    private final DTM dtm;
    private Runnable saveTask;

    public static final String SAVE_STATS_SQL = "INSERT INTO `SeasonStats`(`UUID`, `Season`, `Kills`, `Deaths`, `MonumentsDestroyed`, `Wins`, `Losses`, `PlayTimeWon`, `PlayTimeLost`, `LongestKillStreak`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE Kills = VALUES(Kills), Deaths = VALUES(Deaths), MonumentsDestroyed= VALUES(MonumentsDestroyed), Wins = VALUES(Wins), Losses = VALUES(Losses), PlayTimeWon = VALUES(PlayTimeWon), PlayTimeLost = VALUES(PlayTimeLost), LongestKillStreak = VALUES(LongestKillStreak)";
    public static final String SAVE_PLAYERDATA_SQL = "INSERT INTO PlayerData (UUID, LastSeenName, Prefix, Emeralds, KillStreak) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE LastSeenName = VALUES(LastSeenName), Emeralds = VALUES(Emeralds), Prefix = VALUES(Prefix), KillStreak = VALUES(KillStreak)";

    public QueueDataSaver(DTM dtm) {
	this.dtm = dtm;
    }

    public void init() {
	this.saveTask = () -> {
	    if (queuedData.size() == 0)
		return;
	    try (Connection conn = dtm.getDataHandler().getHDS().getConnection()) {
		DTMPlayerData data;
		try (PreparedStatement stmt1 = conn.prepareStatement(SAVE_PLAYERDATA_SQL);
			PreparedStatement stmt2 = conn.prepareStatement(SAVE_STATS_SQL)) {

		    while ((data = queuedData.poll()) != null) {
			stmt1.setString(1, data.getUUID().toString());
			stmt1.setString(2, data.getLastSeenName());
			stmt1.setString(3, data.getPrefix().orElse(null));
			stmt1.setInt(4, data.getEmeralds());
			stmt1.setInt(5, data.getKillStreak());
			stmt1.addBatch();

			// TODO: Only saves current season
			DTMSeasonStats stats = data.seasonStats.get(dtm.getSeason());
			stmt2.setString(1, stats.getUUID().toString());
			stmt2.setInt(2, dtm.getSeason());
			stmt2.setInt(3, stats.getKills());
			stmt2.setInt(4, stats.getDeaths());
			stmt2.setInt(5, stats.getMonumentsDestroyed());
			stmt2.setInt(6, stats.getWins());
			stmt2.setInt(7, stats.getLosses());
			stmt2.setLong(8, stats.getPlayTimeWon());
			stmt2.setLong(9, stats.getPlayTimeLost());
			stmt2.setInt(10, stats.getLongestKillStreak());
			stmt2.addBatch();
		    }
		    stmt1.executeBatch();
		    stmt2.executeBatch();

		}
	    } catch (SQLException e) {
		e.printStackTrace();
	    }
	};
	Bukkit.getScheduler().runTaskTimerAsynchronously(dtm, saveTask, 20, 20);

    }

    public void queue(DTMPlayerData data) {
	queuedData.offer(data);
    }

    public boolean isSavingDataFor(UUID uuid) {
	for (DTMPlayerData data : queuedData) {
	    if (data.getUUID() == uuid)
		return true;
	}
	return false;
    }

    public void emptyQueueSync() {
	this.saveTask.run();
    }

}
