package com.juubes.dtmproject.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.juubes.dtmproject.DTM;
import com.juubes.nexus.data.AbstractPlayerData;

public class ConnectionListener implements Listener {
	private final DTM dtm;

	public ConnectionListener(DTM dtm) {
		this.dtm = dtm;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.setJoinMessage(null);

		Player p = e.getPlayer();
		p.setScoreboard(dtm.getScoreboardManager().getGlobalScoreboard());
		AbstractPlayerData pd = dtm.getDatabaseManager().getPlayerData(p);

		if (pd.getPrefix() == null)
			pd.setPrefix("&eDTM-Jonne");

		if (Bukkit.getOnlinePlayers().size() <= 15)
			Bukkit.broadcastMessage("�8[�a+�8] �e" + pd.getNick());

		pd.setTeam(null);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		e.setQuitMessage(null);

		Player p = e.getPlayer();
		AbstractPlayerData pd = dtm.getDatabaseManager().getPlayerData(p);
		if (Bukkit.getOnlinePlayers().size() <= 15)
			Bukkit.broadcastMessage("�8[�c-�8] �e" + pd.getNick());

		pd.setTeam(null);
		dtm.getDatabaseManager().savePlayerData(pd);
	}
}
