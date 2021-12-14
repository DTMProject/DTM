package dtmproject.common.data.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.NotImplementedException;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import com.google.common.base.Joiner;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import dtmproject.api.data.IDTMDataHandler;
import dtmproject.api.logic.GameState;
import dtmproject.common.DTM;
import dtmproject.common.data.DTMMap;
import dtmproject.common.data.DTMPlayerData;
import dtmproject.common.data.DTMSeasonStats;

public class MongoDBDatabaseImpl implements IDTMDataHandler<DTMPlayerData, DTMMap> {
    private final DTM pl;
    private MongoCollection<Document> collection;

    private final ConcurrentHashMap<UUID, DTMPlayerData> loadedPlayerdata = new ConcurrentHashMap<>(20);

    /**
     * Loaded maps and active maps are a different thing. Active maps must be loaded
     * and all of them are listed in the config. Loaded maps can be loaded into game
     * with /nextmap.
     */
    private final ConcurrentHashMap<String, DTMMap> loadedMaps = new ConcurrentHashMap<>();

    public MongoDBDatabaseImpl(DTM pl) {
	this.pl = pl;
    }

    @Override
    public void init() throws Exception {
	FileConfiguration conf = pl.getConfig();
	String pw = conf.getString("database.password");
	String user = conf.getString("database.user");
	String url = conf.getString("database.url");
	String connectionString = "mongodb+srv://" + user + ":" + pw + "@" + url;

	// Initialize collection reference
	MongoClient client = MongoClients.create(connectionString);
	this.collection = client.getDatabase("DTM").getCollection("players");

	// Reload winlossdistrcache every 3 minutes
	final int MINUTE_IN_TICKS = 60 * 20;
	Bukkit.getScheduler().runTaskTimerAsynchronously(pl, this::updateWinLossDistributionCache, 3 * MINUTE_IN_TICKS,
		3 * MINUTE_IN_TICKS);

    }

    @Override
    public void loadMaps() {
	// TODO Load maps from MySQL
	// Load default maps
	System.out.println("Loading maps...");
	pl.getDefaultMapLoader().getMaps().forEach(map -> loadedMaps.put(map.getId(), map));
	System.out.println("Loaded maps: "
		+ Joiner.on(", ").join(loadedMaps.entrySet().stream().map(entry -> entry.getKey()).iterator()));

    }

    @Override
    public void loadPlayerData(UUID uuid, String lastSeenName) {
	Document doc = collection.find(Filters.eq("_id", uuid.toString())).first();

	if (doc == null) {
	    loadedPlayerdata.put(uuid, new DTMPlayerData(pl, uuid, lastSeenName, 1000));
	    return;
	}

	int emeralds = doc.getInteger("Emeralds");
	String prefix = doc.getString("Prefix");
	int killStreak = doc.getInteger("KillStreak");
	double eloRating = doc.getDouble("EloRating");

	HashMap<Integer, DTMSeasonStats> stats = new HashMap<>();
	ArrayList<?> seasonStatsArray = (ArrayList<?>) doc.get("SeasonStats");
	for (Object rawDoc : seasonStatsArray) {
	    Document statsDoc = (Document) rawDoc;

	    int season = statsDoc.getInteger("Season");
	    int kills = statsDoc.getInteger("Kills");
	    int deaths = statsDoc.getInteger("Deaths");
	    int monuments = statsDoc.getInteger("MonumentsDestroyed");
	    int wins = statsDoc.getInteger("Wins");
	    int losses = statsDoc.getInteger("Losses");
	    long playTimeWon = statsDoc.getLong("PlayTimeWon");
	    long playTimeLost = statsDoc.getLong("PlayTimeLost");
	    int longestKillStreak = statsDoc.getInteger("LongestKillStreak");

	    stats.put(season, new DTMSeasonStats(uuid, season, kills, deaths, wins, losses, longestKillStreak,
		    playTimeWon, playTimeLost, monuments));
	}

	loadedPlayerdata.put(uuid,
		new DTMPlayerData(pl, uuid, lastSeenName, emeralds, prefix, killStreak, eloRating, stats));
    }

    @Override
    public DTMPlayerData getPlayerData(UUID uuid) {
	return loadedPlayerdata.get(uuid);
    }

    @Override
    public void savePlayerData(UUID uuid) {
	DTMPlayerData data = getPlayerData(uuid);

	Document doc = new Document();
	doc.put("_id", uuid.toString());
	doc.put("LastSeenName", data.getLastSeenName());
	doc.put("Prefix", data.getPrefix().orElse(null));
	doc.put("Emeralds", data.getEmeralds());
	doc.put("KillStreak", data.getKillStreak());
	doc.put("EloRating", data.getEloRating());

	List<Document> seasonStatsDocs = new ArrayList<>();
	for (Entry<Integer, DTMSeasonStats> e : data.getRawSeasonStats().entrySet()) {
	    Document statsDoc = new Document();
	    DTMSeasonStats stats = e.getValue();

	    statsDoc.put("_id", stats.getSeason());
	    statsDoc.put("Season", stats.getSeason());
	    statsDoc.put("Kills", stats.getKills());
	    statsDoc.put("Deaths", stats.getDeaths());
	    statsDoc.put("MonumentsDestroyed", stats.getMonumentsDestroyed());
	    statsDoc.put("Wins", stats.getWins());
	    statsDoc.put("Losses", stats.getLosses());
	    statsDoc.put("PlayTimeWon", stats.getPlayTimeWon());
	    statsDoc.put("PlayTimeLost", stats.getPlayTimeLost());
	    statsDoc.put("LongestKillStreak", stats.getLongestKillStreak());

	    seasonStatsDocs.add(statsDoc);
	}

	doc.put("SeasonStats", seasonStatsDocs);

	collection.replaceOne(Filters.eq("_id", uuid.toString()), doc);
    }

    @Override
    public void unloadPlayerdata(UUID uuid, boolean save) {
	if (save)
	    this.savePlayerData(uuid);

	this.loadedPlayerdata.remove(uuid);
    }

    @Override
    public DTMMap createMapIfNotExists(String mapID) {
	throw new NotImplementedException();
    }

    @Override
    public DTMMap getMap(String mapID) {
	return Objects.requireNonNull(loadedMaps.get(mapID));
    }

    @Override
    public void saveMap(DTMMap map) {
	throw new NotImplementedException();
    }

    @Override
    public LinkedList<DTMPlayerData> getLeaderboard(int count, int season) {
//	LinkedList<DTMPlayerData> allStats = new LinkedList<>();
//
//	FindIterable<Document> allData = collection.find();
//
//	for (Document doc : allData) {
//
//	}
//
//	return null;

	throw new NotImplementedException();

    }

    @Override
    public boolean mapExists(String req) {
	return loadedMaps.containsKey(req);
    }

    @Override
    public Set<String> getLoadedMaps() {
	return loadedMaps.keySet();
    }

    @Override
    public void shutdown() {
    }

    @Override
    public Double[] getWinLossDistribution() {
	// TODO: implement
	return new Double[] { 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d };
    }

    public void updateWinLossDistributionCache() {
	throw new NotImplementedException();
    }

    @Override
    public void logGameEnd(String mapId, String winnerTeamId, HashMap<String, Integer> teamPlayerCounts) {
	throw new NotImplementedException();
    }

    @Override
    public void logGameStart(String mapId, HashMap<String, Integer> teamPlayerCounts) {
	throw new NotImplementedException();
    }

    @Override
    public void logMonumentDestroyed(String mapId, String teamId, String monumentPos, UUID player) {
	throw new NotImplementedException();
    }

    @Override
    public void logPlayerJoin(UUID playerUUID) {
	throw new NotImplementedException();
    }

    @Override
    public void logPlayerLeave(UUID playerUUID, String mapId, long timeAfterStart, GameState gameState) {
	throw new NotImplementedException();
    }
}
