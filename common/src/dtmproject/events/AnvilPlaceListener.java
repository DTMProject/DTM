package dtmproject.events;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class AnvilPlaceListener implements Listener {
    @EventHandler
    public void onAnvilPlace(BlockPlaceEvent e) {
	if (e.getBlock().getType() == Material.ANVIL) {
	    e.setCancelled(true);
	    e.getPlayer().sendMessage("3>§b> §8+ §7Alasimet on estetty DTM:ssä spawnin tukkimisen vuoksi.");
	}
    }
}
