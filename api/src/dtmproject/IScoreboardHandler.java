package dtmproject;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public interface IScoreboardHandler {

    public void updateScoreboard();

    /**
     * The scoreboard can only be loaded after the first world has been loaded.
     */
    public void loadGlobalScoreboard();

    public void changeNameTag(Player p, ChatColor color);
}
