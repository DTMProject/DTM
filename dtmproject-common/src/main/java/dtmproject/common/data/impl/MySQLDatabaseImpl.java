package dtmproject.common.data.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;
import com.zaxxer.hikari.HikariDataSource;

import dtmproject.api.data.IDTMDataHandler;
import dtmproject.api.logic.GameState;
import dtmproject.common.DTM;
import dtmproject.common.data.DTMMap;
import dtmproject.common.data.DTMPlayerData;
import dtmproject.common.data.DTMSeasonStats;
import lombok.Getter;

public class MySQLDatabaseImpl implements IDTMDataHandler<DTMPlayerData, DTMMap> {
    private static final String LOAD_PLAYERDATA_QUERY = "SELECT * FROM PlayerData WHERE UUID = ?";
    private static final String LOAD_PLAYERDATA_STATS_QUERY = "SELECT * FROM SeasonStats WHERE UUID = ?";
//    private static final String GET_LEADERBOARD_QUERY = "SELECT PlayerData.UUID, LastSeenName, Kills, Deaths, MonumentsDestroyed, Wins, Losses, PlayTimeWon, PlayTimeLost, LongestKillStreak FROM SeasonStats INNER JOIN PlayerData ON PlayerData.UUID = SeasonStats.UUID WHERE Season = ? ORDER BY EloRating DESC LIMIT ?";
    private static final String GET_LEADERBOARD_QUERY = "SELECT PlayerData.UUID, PlayerData.EloRating, LastSeenName, Kills, Deaths, MonumentsDestroyed, Wins, Losses, PlayTimeWon, PlayTimeLost, LongestKillStreak FROM SeasonStats INNER JOIN PlayerData ON PlayerData.UUID = SeasonStats.UUID WHERE Season = ? ORDER BY (Kills *  3 + Deaths + MonumentsDestroyed * 10 + PlayTimeWon/1000/60*5 + PlayTimeLost/1000/60) DESC LIMIT ?";
    private static final String GET_WIN_LOSS_DIST = "SELECT EloRating FROM PlayerData WHERE EloRating != -1 ORDER BY EloRating DESC";

    private final DTM pl;

    private final ConcurrentHashMap<UUID, DTMPlayerData> loadedPlayerdata = new ConcurrentHashMap<>(20);
    private PrintWriter logger;

    /**
     * Loaded maps and active maps are a different thing. Active maps must be loaded
     * and all of them are listed in the config. Loaded maps can be loaded into game
     * with /nextmap.
     */
    private final ConcurrentHashMap<String, DTMMap> loadedMaps = new ConcurrentHashMap<>();

    private Double[] cachedWinLossDistribution;

    @Getter
    private final MySQLQueueDataSaver dataSaver;

    @Getter
    private final HikariDataSource HDS;

    public MySQLDatabaseImpl(DTM pl) {
	this.pl = pl;
	this.dataSaver = new MySQLQueueDataSaver(pl, this);
	this.HDS = new HikariDataSource();

	try {
	    Date date = new Date();
	    String dateStr = date.getYear() + "-" + date.getMonth() + "-" + date.getDay() + ""
		    + System.currentTimeMillis() + ".log";

	    File logFile = new File(pl.getDataFolder(), "logs/" + dateStr);
	    this.logger = new PrintWriter(new FileOutputStream(logFile, true), true);

	    pl.getLogger().info("Initialized logging");
	} catch (Exception e) {
	    pl.getLogger().severe("Couldn't initiate logging: " + e.getLocalizedMessage());
	}
    }

    public void init() throws SQLException, IOException {
	FileConfiguration conf = pl.getConfig();
	String pw = conf.getString("database.password");
	String user = conf.getString("database.user");
	String url = conf.getString("database.url");

	pl.getLogger().info("Connecting to " + url + " as user " + user);

	// Initialize HikariCP connection pooling
	HDS.setPassword(pw);
	HDS.setUsername(user);
	HDS.setJdbcUrl("jdbc:mysql://" + url);
	HDS.addDataSourceProperty("cachePrepStmts", "true");
	HDS.addDataSourceProperty("prepStmtCacheSize", "250");
	HDS.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
	HDS.addDataSourceProperty("useSSL", "false");
	HDS.addDataSourceProperty("verifyServerCertificate", "false");
	HDS.setConnectionTimeout(10000);
	HDS.setLeakDetectionThreshold(5000);
	HDS.setMinimumIdle(5);
	HDS.setMaximumPoolSize(15);

	// Create tables
	try (Connection conn = HDS.getConnection(); Statement stmt = conn.createStatement()) {
	    String createTables = IOUtils.toString(pl.getResource("create-tables.sql"), Charset.forName("UTF-8"));
	    String[] sqlStatements = createTables.split(";");
	    for (String sql : sqlStatements) {
		stmt.addBatch(sql);
	    }
	    stmt.executeBatch();
	} catch (SQLException | IOException e) {
	    throw e;
	}

	dataSaver.init();

	updateWinLossDistributionCache();

	// Reload winlossdistrcache every 3 minutes
	final int MINUTE_IN_TICKS = 60 * 20;
	Bukkit.getScheduler().runTaskTimerAsynchronously(pl, this::updateWinLossDistributionCache, 3 * MINUTE_IN_TICKS,
		3 * MINUTE_IN_TICKS);

    }

    public void loadMaps() {
	// TODO Load maps from MySQL
	// Load default maps
	System.out.println("Loading maps...");
	pl.getDefaultMapLoader().getMaps().forEach(map -> loadedMaps.put(map.getId(), map));
	System.out.println("Loaded maps: "
		+ Joiner.on(", ").join(loadedMaps.entrySet().stream().map(entry -> entry.getKey()).iterator()));
    }

    public void loadPlayerData(UUID uuid, String lastSeenName) {
	try (Connection conn = HDS.getConnection()) {
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

			stats.put(season, new DTMSeasonStats(uuid, season, kills, deaths, wins, losses,
				longestKillStreak, playTimeWon, playTimeLost, monuments));
		    }
		}
	    }

	    try (PreparedStatement stmt = conn.prepareStatement(LOAD_PLAYERDATA_QUERY)) {
		stmt.setString(1, uuid.toString());
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
		    String prefix = rs.getString("Prefix");
		    int emeralds = rs.getInt("Emeralds");
		    int killStreak = rs.getInt("KillStreak");
		    int eloRating = rs.getInt("EloRating");
		    loadedPlayerdata.put(uuid,
			    new DTMPlayerData(pl, uuid, lastSeenName, emeralds, prefix, killStreak, eloRating, stats));
		} else {
		    loadedPlayerdata.put(uuid, new DTMPlayerData(pl, uuid, lastSeenName, 1000));
		}

	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public DTMPlayerData getPlayerData(Player p) {
	return getPlayerData(p.getUniqueId());
    }

    public DTMPlayerData getPlayerData(UUID uuid) {
	return loadedPlayerdata.get(uuid);
    }

    public void savePlayerData(UUID uuid) {
	dataSaver.queue(this.getPlayerData(uuid));
    }

    public void unloadPlayerdata(UUID uuid, boolean save) {
	if (save)
	    this.savePlayerData(uuid);

	this.loadedPlayerdata.remove(uuid);
    }

    public DTMMap createMapIfNotExists(String mapID) {
	throw new NotImplementedException();
    }

    public DTMMap getMap(String mapID) {
	return Objects.requireNonNull(loadedMaps.get(mapID));
    }

    public void saveMap(DTMMap map) {
	// TODO Auto-generated method stub
	throw new NotImplementedException();
    }

    public LinkedList<DTMPlayerData> getLeaderboard(int count, int season) {
	LinkedList<DTMPlayerData> allStats = new LinkedList<>();

	try (Connection conn = HDS.getConnection();
		PreparedStatement stmt = conn.prepareStatement(GET_LEADERBOARD_QUERY)) {
	    stmt.setInt(1, season);
	    stmt.setInt(2, count);
	    try (ResultSet rs = stmt.executeQuery()) {
		while (rs.next()) {
		    UUID uuid = UUID.fromString(rs.getString("UUID"));
		    double eloRating = rs.getDouble("EloRating");
		    int kills = rs.getInt("Kills");
		    int deaths = rs.getInt("Deaths");
		    int monuments = rs.getInt("MonumentsDestroyed");
		    int wins = rs.getInt("Wins");
		    int losses = rs.getInt("Losses");
		    long playTimeWon = rs.getLong("PlayTimeWon");
		    long playTimeLost = rs.getLong("PlayTimeLost");
		    int longestKillStreak = rs.getInt("LongestKillStreak");
		    String lastSeenName = rs.getString("LastSeenName");

		    DTMSeasonStats stats = new DTMSeasonStats(uuid, season, kills, deaths, wins, losses,
			    longestKillStreak, playTimeWon, playTimeLost, monuments);

		    // Emeralds and such isn't even loaded. We don't need that.
		    DTMPlayerData data = new DTMPlayerData(pl, uuid, lastSeenName, eloRating);
		    data.getRawSeasonStats().put(stats.getSeason(), stats);

		    allStats.add(data);
		}
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return allStats;
    }

    public boolean mapExists(String req) {
	return loadedMaps.containsKey(req);
    }

    public Set<String> getLoadedMaps() {
	return loadedMaps.keySet();
    }

    public Double[] getWinLossDistribution() {
	return cachedWinLossDistribution;
    }

    public void updateWinLossDistributionCache() {
	LinkedList<Double> allScores = new LinkedList<>();
	try (Connection conn = HDS.getConnection(); PreparedStatement stmt = conn.prepareStatement(GET_WIN_LOSS_DIST)) {
	    try (ResultSet rs = stmt.executeQuery()) {
		while (rs.next()) {
		    double rating = rs.getDouble("EloRating");

		    allScores.add(rating);
		}
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	Double[] levels = new Double[] { 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d };

	int size = allScores.size();
	if (size != 0)
	    for (int i = 10; i < 100; i += 10) {
		double levelThreshold = allScores.get(i * size / 100);
		levels[i / 10 - 1] = levelThreshold;
	    }

	this.cachedWinLossDistribution = levels;

    }

    @Override
    public void shutdown() {
	logger.flush();
	logger.close();

	getDataSaver().emptyQueueSync();
    }

    /**
     * @return the delim infront of the string
     */
    private static String playerCountsToString(HashMap<String, Integer> teamPlayerCounts) {
	// Serialize hashmap
	String playerCountsStr = "";
	for (Entry<String, Integer> entry : teamPlayerCounts.entrySet()) {
	    playerCountsStr += "¤" + entry.getKey() + "/" + entry.getValue();
	}
	return playerCountsStr;
    }

    @Override
    public void logGameEnd(String mapId, String winnerTeamId, HashMap<String, Integer> teamPlayerCounts) {

	logger.println(System.currentTimeMillis() + "¤gameEnd¤" + mapId + "¤" + winnerTeamId + "¤"
		+ playerCountsToString(teamPlayerCounts));
    }

    @Override
    public void logGameStart(String mapId, HashMap<String, Integer> teamPlayerCounts) {
	logger.println(
		System.currentTimeMillis() + "¤gameStart¤" + mapId + "¤" + playerCountsToString(teamPlayerCounts));
    }

    @Override
    public void logMonumentDestroyed(String mapId, String teamId, String monumentPos, UUID player) {
	logger.println(System.currentTimeMillis() + "¤monumentDestroyed¤" + mapId + "¤" + teamId + "¤" + monumentPos
		+ "¤" + player);
    }

    @Override
    public void logPlayerJoin(UUID playerUUID) {
	logger.println(System.currentTimeMillis() + "¤playerJoin¤" + playerUUID);
    }

    @Override
    public void logPlayerLeave(UUID playerUUID, String mapId, long timeAfterStart, GameState gameState) {
	logger.println(System.currentTimeMillis() + "¤playerLeave¤" + playerUUID + "¤" + mapId + "¤" + timeAfterStart
		+ "¤" + gameState.name());
    }

}
