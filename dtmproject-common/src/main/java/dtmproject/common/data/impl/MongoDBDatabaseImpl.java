package dtmproject.common.data.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.NotImplementedException;
import org.bson.Document;
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
	Document doc = collection.find(Filters.eq("_id", uuid)).first();

	if (doc == null) {
	    loadedPlayerdata.put(uuid, new DTMPlayerData(pl, uuid, lastSeenName, 1000));
	    return;
	}

	int emeralds = doc.getInteger("Emeralds");
	String prefix = doc.getString("Prefix");
	int killStreak = doc.getInteger("KillStreak");
	int eloRating = doc.getInteger("EloRating");

	@SuppressWarnings("unchecked")
	HashMap<Integer, DTMSeasonStats> seasonStats = (HashMap<Integer, DTMSeasonStats>) doc.get("SeasonStats");

	loadedPlayerdata.put(uuid,
		new DTMPlayerData(pl, uuid, lastSeenName, emeralds, prefix, killStreak, eloRating, seasonStats));
    }

    @Override
    public DTMPlayerData getPlayerData(UUID uuid) {
	Document data = collection.find(Filters.eq("_id", uuid)).first();

	if (data == null)
	    throw new NullPointerException("Cannot load playerdata for UUID: " + uuid.toString());

	String name = data.getString("LastSeenName");
	Double eloRating = data.getDouble("EloRating");

	return new DTMPlayerData(pl, uuid, name, eloRating);
    }

    @Override
    public void savePlayerData(UUID uuid) {
	DTMPlayerData data = getPlayerData(uuid);

	Document doc = new Document();
	doc.put("_id", uuid);
	doc.put("LastSeenName", data.getLastSeenName());
	doc.put("Prefix", data.getPrefix());
	doc.put("Emeralds", data.getEmeralds());
	doc.put("KillStreak", data.getKillStreak());
	doc.put("EloRating", data.getEloRating());

	doc.put("SeasonStats", data.getRawSeasonStats());

	collection.insertOne(doc);
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
	LinkedList<DTMPlayerData> allStats = new LinkedList<>();

	FindIterable<Document> allData = collection.find();

	for (Document doc : allData) {

	}

	return null;
    }

    @Override
    public boolean mapExists(String req) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public Set<String> getLoadedMaps() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void shutdown() {
	// TODO Auto-generated method stub

    }

    @Override
    public Double[] getWinLossDistribution() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void updateWinLossDistributionCache() {
	// TODO Auto-generated method stub

    }

    @Override
    public void logGameEnd(String mapId, String winnerTeamId, HashMap<String, Integer> teamPlayerCounts) {
	// TODO Auto-generated method stub

    }

    @Override
    public void logGameStart(String mapId, HashMap<String, Integer> teamPlayerCounts) {
	// TODO Auto-generated method stub

    }

    @Override
    public void logMonumentDestroyed(String mapId, String teamId, String monumentPos, UUID player) {
	// TODO Auto-generated method stub

    }

    @Override
    public void logPlayerJoin(UUID playerUUID) {
	// TODO Auto-generated method stub

    }

    @Override
    public void logPlayerLeave(UUID playerUUID, String mapId, long timeAfterStart, GameState gameState) {
	// TODO Auto-generated method stub

    }
}
