package com.juubes.dtmproject.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.juubes.dtmproject.DTM;
import com.juubes.dtmproject.playerdata.DTMPlayerData;

import net.md_5.bungee.api.ChatColor;

public class ChatHandler implements Listener {
	private final DTM dtm;

	public ChatHandler(DTM dtm) {
		this.dtm = dtm;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onChat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		DTMPlayerData pd = dtm.getDatabaseManager().getPlayerData(p);

		int points = pd.getSeasonStats().getSum();
		String prefix = pd.getPrefix();
		prefix = ChatColor.translateAlternateColorCodes('&', prefix);

		e.setMessage(ChatColor.translateAlternateColorCodes('&', e.getMessage()));

		e.setFormat("§8[§b" + points + "§8] §8[" + prefix + "§8] " + pd.getNick() + "§8: §f" + "%2$s");
	}
}
