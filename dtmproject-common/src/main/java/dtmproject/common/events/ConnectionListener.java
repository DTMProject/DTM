package dtmproject.common.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.common.util.concurrent.RateLimiter;

import dtmproject.common.DTM;
import dtmproject.common.data.DTMPlayerData;

public class ConnectionListener implements Listener {
    private final DTM pl;
    private final RateLimiter rl = RateLimiter.create(6);

    public ConnectionListener(DTM pl) {
	this.pl = pl;
    }

    @EventHandler
    public void onAsyncJoin(AsyncPlayerPreLoginEvent e) {
	rl.acquire();

	pl.getDataHandler().loadPlayerData(e.getUniqueId(), e.getName());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
	Player p = e.getPlayer();
	DTMPlayerData pd = pl.getDataHandler().getPlayerData(p);

	if (pd == null) {
	    p.kickPlayer("§ewtf, null playerdata");
	    return;
	}

	// Clear potion effects
	p.getActivePotionEffects().clear();

	// Setup scoreboard
	p.setScoreboard(pl.getScoreboardHandler().getGlobalScoreboard());

	// Update LastSeenName
	pd.setLastSeenName(p.getName());

	// Join message
	if (Bukkit.getOnlinePlayers().size() <= 15) {
	    e.setJoinMessage("§8[§a+§8] §e" + pd.getLastSeenName());
	} else {
	    e.setJoinMessage(null);
	}

	pl.getLogicHandler().getCurrentMap().sendToSpectate(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
	Player p = e.getPlayer();
	p.getActivePotionEffects().clear();
	DTMPlayerData pd = pl.getDataHandler().getPlayerData(p);

	if (Bukkit.getOnlinePlayers().size() <= 15) {
	    e.setQuitMessage("§8[§c-§8] §e" + pd.getLastSeenName());
	} else {
	    e.setQuitMessage(null);
	}

	pl.getDeathHandler().clearLastHits(p);
	pl.getDataHandler().unloadPlayerdata(p.getUniqueId(), true);
    }
}
