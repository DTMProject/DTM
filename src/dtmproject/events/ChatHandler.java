package dtmproject.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import dtmproject.DTM;
import dtmproject.data.DTMPlayerData;
import net.md_5.bungee.api.ChatColor;

public class ChatHandler implements Listener {
	private final DTM dtm;

	public ChatHandler(DTM dtm) {
		this.dtm = dtm;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onChat(AsyncPlayerChatEvent e) {
		final Player p = e.getPlayer();
		final DTMPlayerData pd = dtm.getDataHandler().getPlayerData(p.getUniqueId());

		int points = pd.getSeasonStats().getSum();
		e.setMessage(ChatColor.translateAlternateColorCodes('&', e.getMessage()));

		// Handle null prefixes
		String prefixString = pd.getPrefix() == null ? ""
				: "§8[" + ChatColor.translateAlternateColorCodes('&', pd.getPrefix()) + "§8] ";

		e.setFormat("§8[§b" + points + "§8]" + " " + prefixString + pd.getLastSeenName() + "§8: §f%2$s");

	}

}
