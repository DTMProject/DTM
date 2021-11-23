package dtmproject.common.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;

import dtmproject.api.logic.GameState;
import dtmproject.common.DTM;
import dtmproject.common.data.DTMTeam;

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

	if (p.getWorld() != dtm.getLogicHandler().getCurrentMap().getWorld())
	    return;
	// Spawnprotection
	for (DTMTeam team : dtm.getLogicHandler().getCurrentMap().getTeams()) {
	    if (team.getSpawn() == null)
		continue;
	    Location spawn = team.getSpawn().toLocation(p.getWorld()).clone();
	    spawn.subtract(new Vector(0.5, 0, 0.5));
	    if (spawn.distanceSquared(e.getBlock().getLocation()) < 16) {
		e.setCancelled(true);
		p.sendMessage("§3>§b> §8+ §7Et voi tuhota " + team.getDisplayName() + "§7 spawnia.");
		return;
	    }
	}
    }

    @EventHandler
    public void spawnProtection(BlockPlaceEvent e) {
	if (dtm.getLogicHandler().getGameState() != GameState.RUNNING)
	    return;
	Player p = e.getPlayer();
	if (p.getWorld() != dtm.getLogicHandler().getCurrentMap().getWorld())
	    return;
	// Spawnprotection
	for (DTMTeam team : dtm.getLogicHandler().getCurrentMap().getTeams()) {
	    if (team.getSpawn() == null)
		continue;
	    Location spawn = team.getSpawn().toLocation(p.getWorld()).clone();
	    spawn.subtract(new Vector(0.5, 0, 0.5));
	    if (spawn.distanceSquared(e.getBlock().getLocation()) < 16) {
		e.setCancelled(true);
		p.sendMessage("§3>§b> §8+ §7Et voi rakentaa tiimin " + team.getDisplayName() + "§7 spawnille");
		return;
	    }
	}
    }

}
