package dtmproject.common.data;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
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
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.zaxxer.hikari.HikariDataSource;

import dtmproject.common.DTM;
import lombok.Getter;

public class DTMDataHandler implements IDTMDataHandler<DTMPlayerData, DTMMap> {
    private static final String GET_LEADERBOARD_QUERY = "SELECT PlayerData.UUID, LastSeenName, Kills, Deaths, MonumentsDestroyed, Wins, Losses, PlayTimeWon, PlayTimeLost, LongestKillStreak FROM SeasonStats INNER JOIN PlayerData ON PlayerData.UUID = SeasonStats.UUID WHERE Season = ? ORDER BY (Kills *  3 + Deaths + MonumentsDestroyed * 10 + PlayTimeWon/1000/60*5 + PlayTimeLost/1000/60) DESC LIMIT ?";
    private static final String GET_WIN_LOSS_DIST = "SELECT Wins, Losses FROM SeasonStats WHERE Season = ? AND Wins + Losses > 10 ORDER BY Wins / Losses DESC";

    private final DTM pl;

    private final ConcurrentHashMap<UUID, DTMPlayerData> loadedPlayerdata = new ConcurrentHashMap<>(20);

    /**
     * Loaded maps and active maps are a different thing. Active maps must be loaded
     * and all of them are listed in the config. Loaded maps can be loaded into game
     * with /nextmap.
     */
    private final ConcurrentHashMap<String, DTMMap> loadedMaps = new ConcurrentHashMap<>();

    private Double[] cachedWinLossDistribution;

    @Getter
    private final QueueDataSaver dataSaver;

    @Getter
    private final HikariDataSource HDS;

    @Getter
    public Dao<DTMPlayerData, UUID> playerDataDAO;

    @Getter
    public Dao<DTMMap, UUID> mapDAO;

    @Getter
    public Dao<DTMSeasonStats, UUID> seasonStatsDAO;

    public DTMDataHandler(DTM pl) {
	this.pl = pl;
	this.dataSaver = new QueueDataSaver(pl);
	this.HDS = new HikariDataSource();
    }

    public void init() {
	FileConfiguration conf = pl.getConfig();
	String pw = conf.getString("mysql.password");
	String user = conf.getString("mysql.user");
	String server = conf.getString("mysql.server");
	String db = conf.getString("mysql.database");

	System.out.println("Connecting to " + server + "/" + db + " as user " + user);

	// Initialize HikariCP connection pooling
	String url = "jdbc:mysql://" + server + "/" + db;

	HDS.setPassword(pw);
	HDS.setUsername(user);
	HDS.setJdbcUrl(url);
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
	    e.printStackTrace();
	    for (Player p : Bukkit.getOnlinePlayers()) {
		p.kickPlayer("§e§lDTM\n§b      Palvelin uudelleenkäynnistyy teknisistä syistä.");
	    }
	    Bukkit.shutdown();
	}

	dataSaver.init();

	updateWinLossDistributionCache();

	JdbcPooledConnectionSource connectionSource = null;
	try {
	    connectionSource = new JdbcPooledConnectionSource(url, user, pw);

	    this.playerDataDAO = DaoManager.createDao(connectionSource, DTMPlayerData.class);
	    this.mapDAO = DaoManager.createDao(connectionSource, DTMMap.class);
	    this.seasonStatsDAO = DaoManager.createDao(connectionSource, DTMSeasonStats.class);

	} catch (Exception e) {
	    System.err.println("DTM failed to connect to database.");
	    Bukkit.shutdown();
	    return;
	}

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
	this.unloadPlayerdata(uuid);
    }

    public void unloadPlayerdata(UUID uuid) {
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
		    DTMPlayerData data = new DTMPlayerData(pl, uuid, lastSeenName);
		    data.seasonStats.put(stats.getSeason(), stats);

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
	    stmt.setInt(1, pl.getSeason());
	    try (ResultSet rs = stmt.executeQuery()) {
		while (rs.next()) {
		    int wins = rs.getInt("Wins");
		    int losses = rs.getInt("Losses");

		    allScores.add((double) wins / (double) losses);
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

}
