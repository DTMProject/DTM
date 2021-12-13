package dtmproject.api.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

import dtmproject.api.logic.GameState;

public interface IDTMDataHandler<PD extends IDTMPlayerData<?, ?>, M extends IDTMMap<?>> {
    /**
     * Initializes possible database connections and prepares for loading maps.
     * @throws Exception 
     */
    public void init() throws Exception;

    public void loadMaps();

    /**
     * Don't call this method from the server thread. It blocks. <br>
     * Creates new data if old doesn't exist.
     */
    public void loadPlayerData(UUID uuid, String lastSeenName);

    /**
     * Support for offline playerdata is implementation specific.
     */
    public PD getPlayerData(UUID uuid);

    public void savePlayerData(UUID uuid);

    public void unloadPlayerdata(UUID uuid, boolean save);

    public M createMapIfNotExists(String mapID);

    /**
     * @throws NullPointerException if the map isn't loaded.
     */
    public M getMap(String mapID);

    public void saveMap(M map);

    /**
     * @return a list of unloaded playerdata with the stats ordered.
     */
    public LinkedList<PD> getLeaderboard(int count, int season);

    public boolean mapExists(String req);

    /**
     * @return the ids of the loaded maps.
     */
    public Set<String> getLoadedMaps();

    /**
     * Should be called on plugin disable
     */
    public void shutdown();

    public void updateWinLossDistributionCache();

    public Double[] getWinLossDistribution();

    public void logGameEnd(String mapId, String winnerTeamId, HashMap<String, Integer> teamPlayerCounts);

    public void logGameStart(String mapId, HashMap<String, Integer> teamPlayerCounts);

    public void logMonumentDestroyed(String mapId, String teamId, String monumentPos, UUID player);

    public void logPlayerJoin(UUID playerUUID);

    public void logPlayerLeave(UUID playerUUID, String mapId, long timeAfterStart, GameState gameState);

}
