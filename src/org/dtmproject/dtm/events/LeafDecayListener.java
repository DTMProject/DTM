package org.dtmproject.dtm.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;

public class LeafDecayListener implements Listener {
	@EventHandler
	public void onLeafDrop(LeavesDecayEvent e) {
		e.setCancelled(true);
	}
}
