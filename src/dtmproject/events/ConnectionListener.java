package dtmproject.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.juubes.nexus.data.AbstractPlayerData;

import dtmproject.DTM;
import dtmproject.playerdata.DTMPlayerData;

public class ConnectionListener implements Listener {
	private final DTM dtm;

	public ConnectionListener(DTM dtm) {
		this.dtm = dtm;
	}

	@EventHandler
	public void onAsyncJoin(AsyncPlayerPreLoginEvent e) {
		dtm.getDataHandler().loadPlayerdata(e.getUniqueId(), e.getName());
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.setJoinMessage(null);

		Player p = e.getPlayer();
		DTMPlayerData pd = dtm.getDataHandler().getPlayerData(p);

		if (pd == null) {
			p.kickPlayer("§ewtf, null playerdata");
			return;
		}

		// Clear potion effects
		p.getActivePotionEffects().clear();

		// Setup scoreboard
		p.setScoreboard(dtm.getScoreboardHandler().getGlobalScoreboard());

		// Update LastSeenName
		pd.setLastSeenName(p.getName());

		// Join message
		if (Bukkit.getOnlinePlayers().size() <= 15)
			Bukkit.broadcastMessage("§8[§a+§8] §e" + pd.getNick());

		pd.setTeam(null);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		e.setQuitMessage(null);

		Player p = e.getPlayer();
		p.getActivePotionEffects().clear();
		AbstractPlayerData pd = dtm.getDataHandler().getPlayerData(p);
		if (Bukkit.getOnlinePlayers().size() <= 15)
			Bukkit.broadcastMessage("§8[§c-§8] §e" + pd.getNick());

		dtm.getDeathHandler().clearLastHits(p);
		dtm.getDataHandler().unloadPlayerdata(p.getUniqueId(), true);
	}
}
