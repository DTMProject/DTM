package dtmproject.api.data;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.entity.Player;

public interface IDTMPlayerData<T extends IDTMTeam<?>, SS extends IDTMSeasonStats> {
    public UUID getUUID();

    public String getLastSeenName();

    public void setLastSeenName(String lastSeenName);

    public Optional<String> getPrefix();

    public void setPrefix(String prefix);

    public Player getLastDamager();

    public void setLastDamager(UUID lastDamager);

    public Player getLastMessager();

    public void setLastMessager(UUID lastMessager);

    public T getTeam();

    public void setTeam(T team);

    public boolean isAutoJoin();

    public void setAutoJoin(boolean autoJoin);

    public HashMap<Integer, SS> getAllSeasonStats();

    public SS getSeasonStats();

    public SS getSeasonStats(int season);

}
