package com.juubes.dtmproject.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.juubes.dtmproject.DTM;
import com.juubes.dtmproject.EloHandler;
import com.juubes.dtmproject.playerdata.DTMPlayerData;

import net.md_5.bungee.api.ChatColor;

public class ChatHandler implements Listener {
	private final DTM dtm;

	public ChatHandler(DTM dtm) {
		this.dtm = dtm;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onChat(AsyncPlayerChatEvent e) {
		final Player p = e.getPlayer();
		final DTMPlayerData pd = dtm.getDatabaseManager().getPlayerData(p);

		int points = pd.getSeasonStats().getSum();
		String prefix = pd.getPrefix();
		e.setMessage(ChatColor.translateAlternateColorCodes('&', e.getMessage()));

		// Handle null prefixes
		String prefixString = prefix == null ? ""
				: "§8[" + ChatColor.translateAlternateColorCodes('&', prefix) + "§8] ";

		// Add Elo rank
		String eloRank = "§8[" + EloHandler.getEloRank(pd.getEloRating()) + "§8]";

		e.setFormat("§8[§b" + points + "§8]" + " " + eloRank + " " + prefixString + pd.getNick() + "§8: §f%2$s");

	}

}
