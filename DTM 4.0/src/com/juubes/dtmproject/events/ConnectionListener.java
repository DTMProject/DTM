package com.juubes.dtmproject.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.juubes.dtmproject.DTM;
import com.juubes.dtmproject.ScoreboardManager;
import com.juubes.dtmproject.playerdata.DTMPlayerData;
import com.juubes.nexus.data.AbstractPlayerData;
import com.juubes.nexus.data.PlayerDataHandler;

public class ConnectionListener implements Listener {
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.setJoinMessage(null);

		Player p = e.getPlayer();
		p.setScoreboard(ScoreboardManager.getGlobalScoreboard());
		DTMPlayerData pd = DTM.getDatabaseManager().getPlayerData(p.getUniqueId());
		PlayerDataHandler.setLoaded(pd);

		if (pd.getPrefix() == null)
			pd.setPrefix("&eDTM-Jonne");

		if (Bukkit.getOnlinePlayers().size() <= 15)
			Bukkit.broadcastMessage("§8[§a+§8] §e" + pd.getNick());

		pd.setTeam(null);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		e.setQuitMessage(null);

		Player p = e.getPlayer();
		AbstractPlayerData pd = AbstractPlayerData.get(p);
		if (Bukkit.getOnlinePlayers().size() <= 15)
			Bukkit.broadcastMessage("§8[§c-§8] §e" + pd.getNick());

		pd.setTeam(null);

		PlayerDataHandler.unload(pd);
	}
}
