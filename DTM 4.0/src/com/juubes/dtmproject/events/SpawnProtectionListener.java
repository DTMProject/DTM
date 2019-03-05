package com.juubes.dtmproject.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;

import com.juubes.nexus.logic.GameLogic;
import com.juubes.nexus.logic.GameState;
import com.juubes.nexus.logic.Team;

public class SpawnProtectionListener implements Listener {
	@EventHandler
	public void spawnProtection(BlockBreakEvent e) {
		if (GameLogic.getGameState() != GameState.RUNNING)
			return;
		Player p = e.getPlayer();

		if (p.getWorld() != GameLogic.getCurrentGame().getWorld())
			return;
		// Spawnprotection
		for (Team team : GameLogic.getCurrentGame().getTeams()) {
			if (team.getSpawn() == null)
				continue;
			Location spawn = team.getSpawn().clone();
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
		if (GameLogic.getGameState() != GameState.RUNNING)
			return;
		Player p = e.getPlayer();
		if (p.getWorld() != GameLogic.getCurrentGame().getWorld())
			return;
		// Spawnprotection
		for (Team team : GameLogic.getCurrentGame().getTeams()) {
			if (team.getSpawn() == null)
				continue;
			Location spawn = team.getSpawn().clone();
			spawn.subtract(new Vector(0.5, 0, 0.5));
			if (spawn.distance(e.getBlock().getLocation()) < 4) {
				e.setCancelled(true);
				p.sendMessage("§eEt voi rakentaa tiimin " + team.getDisplayName() + "§e spawnille");
				return;
			}
		}
	}

}
