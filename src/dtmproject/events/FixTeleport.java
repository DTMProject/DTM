package dtmproject.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class FixTeleport implements Listener {
	@EventHandler
	public void onTeleport(PlayerTeleportEvent e) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.hidePlayer(e.getPlayer());
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.showPlayer(e.getPlayer());
		}
	}
}
