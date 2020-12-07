package com.juubes.dtmproject.events;

import org.bukkit.Material;
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
		// Regenerate monuments in case there's some missing
		for (DTMTeam team : dtm.getDatabaseManager().getTeams(e.getMapID()))
			for (Monument mon : team.getMonuments())
				mon.block.getBlock(e.getWorld()).setType(Material.OBSIDIAN);
	}
}
