package dtmproject.common.data;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.google.common.base.Joiner;

import dtmproject.common.DTM;
import dtmproject.common.WorldlessLocation;

public class DTMDataHandler implements IDTMDataHandler<DTMPlayerData, DTMMap> {
//    private static final String GET_LEADERBOARD_QUERY = "SELECT PlayerData.UUID, LastSeenName, Kills, Deaths, MonumentsDestroyed, Wins, Losses, PlayTimeWon, PlayTimeLost, LongestKillStreak FROM SeasonStats INNER JOIN PlayerData ON PlayerData.UUID = SeasonStats.UUID WHERE Season = ? ORDER BY (Kills *  3 + Deaths + MonumentsDestroyed * 10 + PlayTimeWon/1000/60*5 + PlayTimeLost/1000/60) DESC LIMIT ?";
//    private static final String GET_WIN_LOSS_DIST = "SELECT Wins, Losses FROM SeasonStats WHERE Season = ? AND Wins + Losses > 10 ORDER BY Wins / Losses DESC";

    private final DTM pl;

    private final ConcurrentHashMap<UUID, DTMPlayerData> loadedPlayerdata = new ConcurrentHashMap<>(20);

    /**
     * Loaded maps and active maps are a different thing. Active maps must be loaded
     * and all of them are listed in the config. Loaded maps can be loaded into game
     * with /nextmap.
     */
    private final ConcurrentHashMap<String, DTMMap> loadedMaps = new ConcurrentHashMap<>();

    private Double[] cachedWinLossDistribution;

    private EntityManager em;

    public DTMDataHandler(DTM pl) {
	this.pl = pl;
    }

    public void init() {
	FileConfiguration conf = pl.getConfig();
	String pw = conf.getString("mysql.password");
	String user = conf.getString("mysql.user");
	String server = conf.getString("mysql.server");
	String db = conf.getString("mysql.database");

	updateWinLossDistributionCache();

	// Initialize Hibernate
	Configuration hibernateConf = new Configuration();
	Properties prop = new Properties();
	prop.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
	prop.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
	prop.setProperty("hibernate.connection.url", "jdbc:mysql://" + server + "/" + db);
	prop.setProperty("hibernate.connection.username", user);
	prop.setProperty("hibernate.connection.password", pw);
	prop.setProperty("hibernate.hbm2ddl.auto", "create-drop");

	hibernateConf.addProperties(prop);

	hibernateConf.addAnnotatedClass(DTMPlayerData.class);
	hibernateConf.addAnnotatedClass(DTMSeasonStats.class);
	hibernateConf.addAnnotatedClass(DTMMap.class);
	hibernateConf.addAnnotatedClass(DTMTeam.class);
	hibernateConf.addAnnotatedClass(DTMMonument.class);
	hibernateConf.addAnnotatedClass(WorldlessLocation.class);

	try {
	    SessionFactory sf = hibernateConf.buildSessionFactory();
	    this.em = sf.createEntityManager();
	} catch (Exception e) {
	    e.printStackTrace();
	    Bukkit.shutdown();
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

    public void savePlayerData(DTMPlayerData data) {
	em.persist(data);
    }

    public void savePlayerData(UUID uuid) {
	// TODO tx

	em.persist(Objects.requireNonNull(loadedPlayerdata.get(uuid)));
    }

    public void unloadPlayerdata(UUID uuid, boolean save) {
	if (save)
	    savePlayerData(uuid);

	this.loadedPlayerdata.remove(uuid);
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
	// TODO
	return null;
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

    // TODO rewrite and implement properly
    public void updateWinLossDistributionCache() {
	LinkedList<Double> allScores = new LinkedList<>();

	// For all entities
	// ---- allScores.add((double) wins / (double) losses);

	Double[] levels = new Double[] { 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d };

	int size = allScores.size();
	if (size != 0)
	    for (int i = 10; i < 100; i += 10) {
		double levelThreshold = allScores.get(i * size / 100);
		levels[i / 10 - 1] = levelThreshold;
	    }

	this.cachedWinLossDistribution = levels;
    }

    public void close() {
	this.em.close();
    }

}
