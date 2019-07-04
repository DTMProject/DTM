package com.juubes.dtmproject.events;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.juubes.dtmproject.DTM;
import com.juubes.dtmproject.setup.DTMTeam;
import com.juubes.dtmproject.setup.Monument;
import com.juubes.nexus.events.PreLoadGameWorldEvent;

public class PreWorldLoadListener implements Listener {
	private final DTM dtm;
	public PreWorldLoadListener(DTM dtm) {
		this.dtm = dtm;
	}

	@EventHandler
	public void onLoad(PreLoadGameWorldEvent e) {
		World w = e.getWorld();

		// Reload locations for the world
		// Assuming all of the locations in a MapSettings have the same world
		dtm.getDatabaseManager().updateLocationWorlds(w);

		for (DTMTeam team : dtm.getDatabaseManager().getTeams(e.getMapID()))
			for (Monument mon : team.getMonuments())
				mon.block.setType(Material.OBSIDIAN);
	}
}
