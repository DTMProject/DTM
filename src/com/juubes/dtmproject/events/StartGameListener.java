package com.juubes.dtmproject.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.juubes.dtmproject.DTM;
import com.juubes.dtmproject.setup.DTMTeam;
import com.juubes.dtmproject.setup.Monument;

import com.juubes.nexus.events.StartGameEvent;

public class StartGameListener implements Listener {

	private final DTM dtm;

	public StartGameListener(DTM dtm) {
		this.dtm = dtm;
	}

	@EventHandler
	public void onStart(StartGameEvent e) {
		Bukkit.getScheduler().runTaskLater(dtm, () -> {
			for (DTMTeam team : dtm.getDatabaseManager().getTeams(e.getMapID()))
				for (Monument mon : team.getMonuments())
					mon.block.getBlock(e.getWorld()).setType(Material.OBSIDIAN);
			Bukkit.broadcastMessage("§eMonumentit voi nyt tuhota!");
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);
			}
		}, 20 * 60 * 3);
	}
}
