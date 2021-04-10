package dtmproject.data;

import java.util.LinkedList;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.entity.Player;

import dtmproject.WorldlessLocation;
import net.md_5.bungee.api.ChatColor;

public interface IDTMTeam<M extends IMonument> {
    public String getId();

    public String getDisplayName();

    public void setDisplayName(String name);

    public ChatColor getTeamColor();

    public void setTeamColor(ChatColor color);

    public WorldlessLocation getSpawn();

    public void setSpawn(WorldlessLocation spawn);

    public LinkedList<M> getMonuments();

    public void setMonuments(LinkedList<M> monuments);

    public Set<Player> getPlayers();

    public Color getLeatherColor();
}
