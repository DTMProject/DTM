package dtmproject.common.data;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
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
import com.zaxxer.hikari.HikariDataSource;

import dtmproject.api.WorldlessBlockLocation;
import dtmproject.api.WorldlessLocation;
import dtmproject.common.DTM;
import lombok.Getter;

public class DTMDataHandler implements IDTMDataHandler<DTMPlayerData, DTMMap> {
    private static final String LOAD_PLAYERDATA_QUERY = "SELECT * FROM PlayerData WHERE UUID = ?";
    private static final String LOAD_PLAYERDATA_STATS_QUERY = "SELECT * FROM SeasonStats WHERE UUID = ?";

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
	HDS.setPassword(pw);
	HDS.setUsername(user);
	HDS.setJdbcUrl("jdbc:mysql://" + server + "/" + db);
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
		    loadedPlayerdata.put(uuid, new DTMPlayerData(pl, uuid, lastSeenName));
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

    private static final String SAVE_MAP_SQL = "INSERT INTO Maps (MapID, DisplayName, LobbyX, LobbyY, LobbyZ, LobbyYaw, LobbyPitch, Ticks, KitContents), VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE SET DisplayName = VALUES(DisplayName), LobbyX = VALUES(LobbyX), LobbyY = VALUES(LobbyY), LobbyZ = VALUES(LobbyZ), LobbyYaw = VALUES(LobbyYaw), LobbyPitch = VALUES(LobbyPitch), Ticks = VALUES(Ticks), KitContents = VALUES(KitContents)";
    private static final String SAVE_TEAMS_SQL = "INSERT INTO Teams (MapID, TeamID, DisplayName, TeamColor, SpawnX, SpawnY, SpawnZ, SpawnYaw, SpawnPitch), VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE SET DisplayName = VALUES(DisplayName), TeamColor = VALUES(TeamColor), SpawnX = VALUES(SpawnX), SpawnY = VALUES(SpawnY), SpawnZ = VALUES(SpawnZ), SpawnYaw = VALUES(SpawnYaw), SpawnPitch = VALUES(SpawnPitch)";
    private static final String SAVE_MONUMENTS_SQL = "INSERT INTO Monuments (MapID, TeamID, Position, CustomName, LocationX, LocationY, LocationZ, LocationYaw, LocationPitch), VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE SET Position = VALUES(Position), CustomName = VALUES(CustomName), LocationX = VALUES(LocationX), LocationY = VALUES(LocationY), LocationZ = VALUES(LocationZ), LocationYaw = VALUES(LocationYaw), LocationPitch = VALUES(LocationPitch)";

    public void saveMap(DTMMap map) {
	try (Connection conn = HDS.getConnection()) {
	    conn.setAutoCommit(false);

	    // Maps
	    try (PreparedStatement stmt = conn.prepareStatement(SAVE_MAP_SQL)) {
		stmt.setString(1, map.getId());
		stmt.setString(2, map.getDisplayName());
		stmt.setDouble(3, map.getLobby().get().getX());
		stmt.setDouble(4, map.getLobby().get().getY());
		stmt.setDouble(5, map.getLobby().get().getZ());
		stmt.setDouble(6, map.getLobby().get().getYaw());
		stmt.setDouble(7, map.getLobby().get().getPitch());
		stmt.setInt(8, map.getTicks());
		// TODO Also save the kit -- if exists
		stmt.setBlob(9, (Blob) null);
		stmt.execute();
	    } catch (Exception e) {
		e.printStackTrace();
		conn.rollback();
		return;
	    }

	    // Teams
	    try (PreparedStatement stmt = conn.prepareStatement(SAVE_TEAMS_SQL)) {
		for (DTMTeam team : map.getTeams()) {
		    stmt.setString(1, map.getId());
		    stmt.setString(2, team.getId());
		    stmt.setString(3, team.getDisplayName());
		    stmt.setString(4, team.getTeamColor().name());

		    WorldlessLocation spawn = team.getSpawn();
		    stmt.setDouble(5, spawn.getX());
		    stmt.setDouble(6, spawn.getY());
		    stmt.setDouble(7, spawn.getZ());
		    stmt.setDouble(8, spawn.getYaw());
		    stmt.setDouble(9, spawn.getPitch());

		    stmt.addBatch();
		}
		stmt.executeBatch();
	    } catch (Exception e) {
		e.printStackTrace();
		conn.rollback();
		return;
	    }

	    // Monuments
	    try (PreparedStatement stmt = conn.prepareStatement(SAVE_MONUMENTS_SQL)) {
		for (DTMTeam team : map.getTeams()) {
		    for (DTMMonument mon : team.getMonuments()) {
			stmt.setString(1, map.getId());
			stmt.setString(2, team.getId());
			stmt.setString(3, mon.getPosition());

			WorldlessBlockLocation loc = mon.getBlock();
			stmt.setDouble(4, loc.getX());
			stmt.setDouble(5, loc.getY());
			stmt.setDouble(6, loc.getZ());
			stmt.addBatch();
		    }
		}
		stmt.executeBatch();
	    } catch (Exception e) {
		e.printStackTrace();
		conn.rollback();
		return;
	    }
	    conn.commit();
	} catch (Exception e) {
	    e.printStackTrace();
	}
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
