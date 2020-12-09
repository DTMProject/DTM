package dtmproject.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import dtmproject.DTM;
import dtmproject.EloHandler;
import dtmproject.playerdata.DTMPlayerData;
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

		int points = pd.seasonStats.get(dtm.getSeason()).getSum();
		e.setMessage(ChatColor.translateAlternateColorCodes('&', e.getMessage()));

		// Handle null prefixes
		String prefixString = pd.prefix == null ? ""
				: "§8[" + ChatColor.translateAlternateColorCodes('&', pd.prefix) + "§8] ";

		// Add Elo rank
		String eloRank = "§8[" + EloHandler.getEloRank(pd.eloRating) + "§8]";

		e.setFormat("§8[§b" + points + "§8]" + " " + eloRank + " " + prefixString + pd.lastSeenName + "§8: §f%2$s");

	}

}
