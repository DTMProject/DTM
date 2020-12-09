package dtmproject.events;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class AnvilPlaceEvent implements Listener {
	@EventHandler
	public void onAnvilPlace(BlockPlaceEvent e) {
		if (e.getBlock().getType() == Material.ANVIL) {
			e.setCancelled(true);
			e.getPlayer().sendMessage("§eAlasimet on estetty DTM:ssä spawnin tukkimisen vuoksi.");
		}
	}
}
