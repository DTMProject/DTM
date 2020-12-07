package com.juubes.dtmproject.playerdata;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.juubes.dtmproject.DTM;
import com.juubes.nexus.data.AbstractDataHandler;
import com.juubes.nexus.data.AbstractMap;
import com.juubes.nexus.data.AbstractPlayerData;
import com.zaxxer.hikari.HikariDataSource;

public class DTMDataHandler extends AbstractDataHandler {

	public static final String SETTINGS_PATH = "./settings";
	public static final String KITS_PATH = "./kits.yml";
	private static final String LOAD_PLAYERDATA_QUERY = "SELECT * FROM PlayerData WHERE UUID = ?";
	private static final String LOAD_PLAYERDATA_STATS_QUERY = "SELECT * FROM SeasonStats WHERE UUID = ?";
	private static final String GET_LEADERBOARD_QUERY = "SELECT PlayerData.UUID, LastSeenName, Kills, Deaths, MonumentsDestroyed, Wins, Losses, PlayTimeWon, PlayTimeLost, LongestKillStreak FROM SeasonStats INNER JOIN PlayerData ON PlayerData.UUID = SeasonStats.UUID WHERE Season = ? ORDER BY (Kills *  3 + Deaths + MonumentsDestroyed * 10 + PlayTimeWon/1000/60*5 + PlayTimeLost/1000/60) DESC LIMIT ?";

	private final DTM pl;
	private final ConcurrentHashMap<String, AbstractMap> mapSettings = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<UUID, DTMPlayerData> loadedPlayerdata = new ConcurrentHashMap<>(20);

	private final QueueDataSaver dataSaver;

	private final File mapConfFolder, kitFile;

	private HikariDataSource HDS;

	public DTMDataHandler() {
		this.pl = (DTM) Bukkit.getPluginManager().getPlugin("DTM");
		this.mapConfFolder = new File(pl.getDataFolder(), SETTINGS_PATH);
		this.kitFile = new File(pl.getDataFolder(), KITS_PATH);
		this.dataSaver = new QueueDataSaver(pl);
	}

	public void init() {
		FileConfiguration conf = pl.getConfig();
		String pw = conf.getString("mysql.password");
		String user = conf.getString("mysql.user");
		String server = conf.getString("mysql.server");
		String db = conf.getString("mysql.database");

		System.out.println("Connecting to " + server + "/" + db + " as user " + user);

		// Initialize HikariCP connection pooling
		this.HDS = new HikariDataSource();
		HDS.setPassword(pw);
		HDS.setUsername(user);
		HDS.setJdbcUrl("jdbc:mysql://" + server + "/" + db);
		HDS.addDataSourceProperty("cachePrepStmts", "true");
		HDS.addDataSourceProperty("prepStmtCacheSize", "250");
		HDS.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		HDS.setConnectionTimeout(10000);
		HDS.setLeakDetectionThreshold(5000);
		HDS.setMinimumIdle(1);
		HDS.setMaximumPoolSize(5);
		try {
			Connection conn = HDS.getConnection();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.kickPlayer("§e§lDTM\n§b      Palvelin uudelleenkäynnistyy teknisistä syistä.");
			}
			Bukkit.shutdown();
		}
		// Create tables
		try (Connection conn = HDS.getConnection(); Statement stmt = conn.createStatement()) {
			String createTables = IOUtils.toString(pl.getResource("create-tables.sql"));
			String[] sqlStatements = createTables.split(";");
			for (String sql : sqlStatements) {
				stmt.addBatch(sql);
			}
			stmt.executeBatch();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}

		dataSaver.init();
	}

	public HikariDataSource getHDS() {
		return HDS;
	}

	/**
	 * Don't call this method from the server thread. It blocks.
	 */
	@Override
	public synchronized void loadPlayerData(UUID uuid) {
		try (Connection conn = HDS.getConnection()) {
			String lastSeenName = Bukkit.getPlayer(uuid).getName();

			// Load stats
			HashMap<Integer, DTMSeasonStats> stats = new HashMap<>(1);
			try (PreparedStatement stmt = conn.prepareStatement(LOAD_PLAYERDATA_STATS_QUERY)) {
				stmt.setString(1, uuid.toString());
				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						int season = rs.getInt("Season");
						int kills = rs.getInt("Kills");
						int deaths = rs.getInt("Deaths");
						int monuments = rs.getInt("MonumentsDestroyed");
						int wins = rs.getInt("Wins");
						int losses = rs.getInt("Losses");
						long playTimeWon = rs.getLong("PlayTimeWon");
						long playTimeLost = rs.getLong("PlayTimeLost");
						int longestKillStreak = rs.getInt("LongestKillStreak");

						stats.put(season, new DTMSeasonStats(uuid, season, kills, deaths, monuments, wins, losses,
								playTimeWon, playTimeLost, longestKillStreak));
					}
				}
			}

			try (PreparedStatement stmt = conn.prepareStatement(LOAD_PLAYERDATA_QUERY)) {
				stmt.setString(1, uuid.toString());
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						String prefix = rs.getString("Prefix");
						int emeralds = rs.getInt("Emeralds");
						String nick = rs.getString("Nick");
						int killStreak = rs.getInt("KillStreak");
						double eloRating = rs.getDouble("EloRating");

						loadedPlayerdata.put(uuid, new DTMPlayerData(uuid, lastSeenName, emeralds, prefix, nick,
								killStreak, eloRating, stats));
					} else {
						loadedPlayerdata.put(uuid, new DTMPlayerData(uuid, lastSeenName));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	@Override
	public AbstractPlayerData createIfNotExists(UUID uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DTMPlayerData getPlayerData(UUID uuid) {
		return null;
	}

	@Override
	public void savePlayerData(UUID uuid) {
		// TODO Auto-generated method stub

	}

	public void unloadPlayerdata(UUID uuid, boolean save) {
		if (save)
			savePlayerData(uuid);
		unloadPlayerdata(uuid);
	}

	@Override
	public void unloadPlayerdata(UUID uuid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadMaps() {
		// TODO Auto-generated method stub

	}

	@Override
	public AbstractMap createMapIfNotExists(String mapID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractMap getMap(String mapID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveMap(AbstractMap map) {
		// TODO Auto-generated method stub

	}

	@Override
	public LinkedList<DTMPlayerData> getLeaderboard(int count, int season) {
		LinkedList<DTMPlayerData> allStats = new LinkedList<>();

		try (Connection conn = HDS.getConnection();
				PreparedStatement stmt = conn.prepareStatement(GET_LEADERBOARD_QUERY)) {
			stmt.setInt(1, season);
			stmt.setInt(2, count);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					UUID uuid = UUID.fromString(rs.getString("UUID"));
					int kills = rs.getInt("Kills");
					int deaths = rs.getInt("Deaths");
					int monuments = rs.getInt("MonumentsDestroyed");
					int wins = rs.getInt("Wins");
					int losses = rs.getInt("Losses");
					long playTimeWon = rs.getLong("PlayTimeWon");
					long playTimeLost = rs.getLong("PlayTimeLost");
					int longestKillStreak = rs.getInt("LongestKillStreak");

					String lastSeenName = rs.getString("LastSeenName");
					DTMSeasonStats stats = new DTMSeasonStats(uuid, season, kills, deaths, monuments, wins, losses,
							playTimeWon, playTimeLost, longestKillStreak);

					DTMPlayerData data = new DTMPlayerData(uuid, lastSeenName);
					data.loadSeasonStats(stats);
					allStats.add(data);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return allStats;

	}

}
