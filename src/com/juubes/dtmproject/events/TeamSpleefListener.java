package com.juubes.dtmproject.events;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.util.Vector;

import com.juubes.dtmproject.DTM;
import com.juubes.dtmproject.playerdata.DTMPlayerData;
import com.juubes.nexus.events.StartCountdownEvent;

public class TeamSpleefListener implements Listener {

	private HashMap<Block, Long> antiSpleef = new HashMap<>();
	private final DTM dtm;
	public TeamSpleefListener(DTM pl) {
		this.dtm = pl;
	}

	@EventHandler
	public void stopSpleef(BlockBreakEvent e) {
		Player p = e.getPlayer();
		Location blockBroken = e.getBlock().getLocation();
		DTMPlayerData pd = dtm.getDatabaseManager().getPlayerData(p);

		if (pd.getTeam() == null)
			return;
		if (antiSpleef.containsKey(e.getBlock())) {
			if (antiSpleef.get(e.getBlock()) + 500 > System.currentTimeMillis()) {
				p.sendMessage("§cEt voi vielä rikkoa tuota.");
				e.setCancelled(true);
				return;
			} else {
				antiSpleef.remove(e.getBlock());
			}
		}

		for (Player playerOntop : pd.getTeam().getPlayers()) {
			if (p.equals(playerOntop))
				continue;
			Block blockBelowPlayer = playerOntop.getWorld().getBlockAt(playerOntop.getLocation()
					.subtract(new Vector(0, 0.2, 0)));
			if (!blockBroken.getWorld().equals(blockBelowPlayer.getWorld()))
				continue;
			if (blockBroken.distance(blockBelowPlayer.getLocation()) != 0)
				continue;
			e.setCancelled(true);
			p.sendMessage("§cEt voi rikkoa palikoita omien tiimiläisten alta.");
			if (p.getHealth() <= 6) {
				dtm.getDeathHandler().fakeKillPlayer(p);
				Bukkit.broadcastMessage("§e" + p.getDisplayName()
						+ " §ekuoli yrittäessään sabotoida omaa tiimiänsä!");
			} else {
				p.setHealth(p.getHealth() - 5);
				p.damage(-1);
			}
			return;
		}

	}

	@EventHandler
	public void onCountdownStart(StartCountdownEvent e) {
		antiSpleef.clear();
	}
}
