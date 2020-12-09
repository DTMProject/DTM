package dtmproject.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import com.juubes.nexus.logic.GameState;

import dtmproject.DTM;
import dtmproject.setup.DTMTeam;

public class SpawnProtectionListener implements Listener {
	private final DTM dtm;

	public SpawnProtectionListener(DTM dtm) {
		this.dtm = dtm;
	}

	@EventHandler
	public void spawnProtection(BlockBreakEvent e) {
		if (dtm.getLogicHandler().getGameState() != GameState.RUNNING)
			return;
		Player p = e.getPlayer();

		if (p.getWorld() != dtm.getGameWorldHandler().getCurrentMap().getWorld())
			return;
		// Spawnprotection
		for (DTMTeam team : dtm.getGameWorldHandler().getCurrentMap().getTeams()) {
			if (team.getSpawn() == null)
				continue;
			Location spawn = team.getSpawn().toLocation(p.getWorld()).clone();
			spawn.subtract(new Vector(0.5, 0, 0.5));
			if (spawn.distance(e.getBlock().getLocation()) < 4) {
				e.setCancelled(true);
				p.sendMessage("§eEt voi tuhota " + team.getDisplayName() + "§e spawnia.");
				return;
			}
		}
	}

	@EventHandler
	public void spawnProtection(BlockPlaceEvent e) {
		if (dtm.getLogicHandler().getGameState() != GameState.RUNNING)
			return;
		Player p = e.getPlayer();
		if (p.getWorld() != dtm.getGameWorldHandler().getCurrentMap().getWorld())
			return;
		// Spawnprotection
		for (Team team : dtm.getGameWorldHandler().getCurrentMap().getTeams()) {
			if (team.getSpawn() == null)
				continue;
			Location spawn = team.getSpawn().toLocation(p.getWorld()).clone();
			spawn.subtract(new Vector(0.5, 0, 0.5));
			if (spawn.distance(e.getBlock().getLocation()) < 4) {
				e.setCancelled(true);
				p.sendMessage("§eEt voi rakentaa tiimin " + team.getDisplayName() + "§e spawnille");
				return;
			}
		}
	}

}
