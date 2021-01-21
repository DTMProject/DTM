package dtmproject;

import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import dtmproject.data.IDTMMap;
import dtmproject.data.IDTMPlayerData;

public interface IDTMDataHandler<PD extends IDTMPlayerData<?, ?>, M extends IDTMMap<?>> {
    /**
     * Initializes possible database connections and prepares for loading maps.
     */
    public void init();

    public void loadMaps();

    /**
     * Don't call this method from the server thread. It blocks. <br>
     * Creates new data if old doesn't exist.
     */
    public void loadPlayerData(UUID uuid, String lastSeenName);

    public PD getPlayerData(Player p);

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
}
