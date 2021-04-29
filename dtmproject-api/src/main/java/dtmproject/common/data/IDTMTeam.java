package dtmproject.common.data;

import java.util.Set;

import org.bukkit.Color;
import org.bukkit.entity.Player;

import dtmproject.api.IWorldlessLocation;
import net.md_5.bungee.api.ChatColor;

public interface IDTMTeam<M extends IDTMMonument> {
    public String getId();

    public String getDisplayName();

    public void setDisplayName(String name);

    public ChatColor getTeamColor();

    public void setTeamColor(ChatColor color);

    public IWorldlessLocation getSpawn();

    public void setSpawn(IWorldlessLocation spawn);

    public Set<M> getMonuments();

    public void setMonuments(Set<M> monuments);

    public Set<Player> getPlayers();

    public Color getLeatherColor();
}
