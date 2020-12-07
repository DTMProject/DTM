package com.juubes.dtmproject.events;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import com.juubes.dtmproject.DTM;
import com.juubes.dtmproject.playerdata.DTMPlayerData;

public class ArrowsDestroyBlocks implements Listener {

	private final DTM dtm;

	public ArrowsDestroyBlocks(DTM dtm) {
		this.dtm = dtm;
	}

	@EventHandler
	public void onArrowHit2(ProjectileHitEvent e) {
		if (e.getEntity().getType() != EntityType.ARROW)
			return;

		Set<Block> blocksAffected = new HashSet<>();

		Block hitBlock = e.getHitBlock();

		if (hitBlock.getWorld() != dtm.getNexus().getGameLogic().getCurrentGame().getWorld())
			return;

		Player p = (Player) e.getEntity().getShooter();
		DTMPlayerData pd = dtm.getDatabaseManager().getPlayerData(p);

		if (p == null || !(p instanceof Player)) {
			// If shooter can't be found, don't break blocks
			return;
		} else {
			for (Player target : Bukkit.getOnlinePlayers()) {
				// Test if own teammate too close to arrow
				DTMPlayerData targetData = dtm.getDatabaseManager().getPlayerData(target);
				boolean tooClose = (target.getLocation().distance(e.getEntity().getLocation()) < 2);
				boolean ownTeammate = (pd.getTeam() == targetData.getTeam());
				if (tooClose && ownTeammate) {
					return;
				}

				/* A little nerf */
				if (hitBlock.getType() == Material.WOOD) {
					// Give spleefer points
					if (targetData.getTeam() != null && targetData.getTeam() != pd.getTeam()) {
						targetData.setLastDamager(p);
					}
				} else
					return;
			}
		}

		if (hitBlock.getType() != Material.WOOD)
			return;
		hitBlock.breakNaturally();

		// TODO Check for teamspleef

		e.getEntity().remove();
	}

	// @EventHandler
	public void onArrowHit(ProjectileHitEvent e) {
		if (e.getEntity().getType() != EntityType.ARROW)
			return;

		Set<Block> blocksAffected = new HashSet<>();

		Block original = e.getEntity().getLocation().getBlock();

		if (original.getWorld() != dtm.getNexus().getGameLogic().getCurrentGame().getWorld())
			return;

		Player p = (Player) e.getEntity().getShooter();
		DTMPlayerData pd = dtm.getDatabaseManager().getPlayerData(p);

		if (p == null || !(p instanceof Player)) {
			// If shooter can't be found, don't break blocks

			return;
		} else {
			for (Player target : Bukkit.getOnlinePlayers()) {
				// Test if own teammate too close to arrow
				DTMPlayerData targetData = dtm.getDatabaseManager().getPlayerData(target);
				boolean tooClose = (target.getLocation().distance(e.getEntity().getLocation()) < 2);
				boolean ownTeammate = (pd.getTeam() == targetData.getTeam());
				if (tooClose && ownTeammate) {
					System.out.println("4");
					return;
				}

				/* A little nerf */
				if (original.getType() == Material.WOOD)
					// Give spleefer points
					if (targetData.getTeam() != null && targetData.getTeam() != pd.getTeam()) {
						targetData.setLastDamager(p);
					}
			}
		}

		for (Block block : blocksAffected) {
			if (block.getType() != Material.WOOD)
				continue;
			block.breakNaturally();
		}

		// TODO Check for teamspleef

		e.getEntity().remove();
	}
}
