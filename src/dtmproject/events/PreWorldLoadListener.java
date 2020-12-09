package dtmproject.events;

import org.bukkit.event.Listener;

import dtmproject.DTM;

public class PreWorldLoadListener implements Listener {
	private final DTM dtm;

	public PreWorldLoadListener(DTM dtm) {
		this.dtm = dtm;
	}

	// big TODO
	// @EventHandler
	// public void onLoad(PreLoadGameWorldEvent e) {
	// // Regenerate monuments in case there's some missing
	// for (DTMTeam team : dtm.getDataHandler().getTeams(e.getMapID()))
	// for (Monument mon : team.getMonuments())
	// mon.block.getBlock(e.getWorld()).setType(Material.OBSIDIAN);
	// }
}
