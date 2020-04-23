package com.juubes.dtmproject.events;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.juubes.dtmproject.DTM;
import com.juubes.dtmproject.playerdata.DTMPlayerData;
import com.juubes.dtmproject.setup.DTMTeam;
import com.juubes.dtmproject.setup.Monument;
import com.juubes.nexus.logic.GameState;
import com.juubes.nexus.logic.Team;

public class DestroyMonumentListener implements Listener {
	private final DTM dtm;

	public DestroyMonumentListener(DTM dtm) {
		this.dtm = dtm;
	}

	@EventHandler
	public void onDestroy(BlockBreakEvent e) {
		Player p = e.getPlayer();
		DTMPlayerData data = dtm.getDatabaseManager().getPlayerData(p);
		if (dtm.getNexus().getGameLogic().getGameState() != GameState.RUNNING) {
			// Can't break if not op, not running, and no creativemode
			if (!p.isOp() && p.getGameMode() != GameMode.CREATIVE)
				e.setCancelled(true);
			return;
		}

		// Destroyer is a spectator
		if (data.getTeam() == null) {
			if (!p.isOp()) {
				p.sendMessage("§eEt voi tuhota monumenttia spectatessa.");
				e.setCancelled(true);
			} else {
				if (p.getGameMode() != GameMode.CREATIVE) {
					p.sendMessage(
							"§eEt ole tiimissä. Ole hyvä§, ja laita gamemode 1, jos haluat muokata mappia, kun peli on käynnissä.");
					e.setCancelled(true);
				}
			}
			return;
		}

		Block b = e.getBlock();
		if (b.getWorld() != dtm.getNexus().getGameLogic().getCurrentGame().getWorld())
			return;

		if (!e.getBlock().getType().equals(Material.OBSIDIAN))
			return;
		for (Team nt : dtm.getNexus().getGameLogic().getCurrentGame().getTeams()) {
			DTMTeam team = (DTMTeam) nt;
			for (Monument mon : team.getMonuments()) {
				if (!e.getBlock().equals(mon.block))
					continue;

				// Monument destroyed
				// Test if own
				if (data.getTeam().equals(team)) {
					p.sendMessage("§eTämä on oman tiimisi monumentti.");
					e.setCancelled(true);
					return;
				}

				if (!mon.broken) {
					Bukkit.broadcastMessage(data.getNick() + " §etuhosi monumentin " + team.getChatColor()
							+ mon.customName);
					DTMPlayerData pd = dtm.getDatabaseManager().getPlayerData(p);
					pd.getSeasonStats().monuments++;
					pd.setEmeralds(pd.getEmeralds() + 5);
					for (Player player : Bukkit.getOnlinePlayers()) {
						player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
					}
					handleBrokenMonument(mon);
				} else {
					p.sendMessage("§eTämä monumentti on jo kerran tuhottu.");
				}
				e.setCancelled(true);
				e.getBlock().setType(Material.AIR);
				return;

			}
		}
	}

	private void handleBrokenMonument(Monument monument) {
		monument.broken = true;
		dtm.getScoreboardManager().updateScoreboard();
		Team winner = getWinner();

		// If two or more teams alive winner != null
		if (winner == null)
			return;
		String winnerList = "";
		List<Player> players = winner.getPlayers();
		if (players.size() > 1) {
			for (int i = 0; i < players.size() - 1; i++) {
				winnerList += players.get(i).getDisplayName() + ", ";
			}
			winnerList = winnerList.substring(0, winnerList.length() - 2);
			winnerList += "§e ja " + players.get(players.size() - 1).getDisplayName();
		} else {
			if (players.size() == 1)
				winnerList += players.get(0).getDisplayName();
		}
		Bukkit.broadcastMessage(winner.getDisplayName() + " §e§lvoitti pelin!");
		for (Player p : Bukkit.getOnlinePlayers())
			p.setGameMode(GameMode.SPECTATOR);

		// 50 points to the winner team, 15 to losers
		for (Team team : dtm.getNexus().getGameLogic().getCurrentGame().getTeams()) {
			for (Player p : team.getPlayers()) {
				DTMPlayerData data = dtm.getDatabaseManager().getPlayerData(p);
				int minutesPlayed = Math.min((int) ((System.currentTimeMillis() - dtm.getNexus().getGameLogic()
						.getCurrentGame().getStartTime()) / 1000 / 60), 60);

				int loserPoints = minutesPlayed * 5;
				int winnerPoints = minutesPlayed * 25;

				if (team == winner) {
					p.sendTitle("§a§lVoitto", "§aSait " + winnerPoints + " pistettä!");
				} else if (data.getTeam() != null) {
					p.sendTitle("§c§lHäviö", "§aSait " + loserPoints + " pistettä!");
				}

				if (team == winner)
					data.getSeasonStats().playTimeWon += loserPoints * 60 * 1000;
				else
					data.getSeasonStats().playTimeLost += loserPoints * 60 * 1000;
			}
		}
		dtm.getNexus().getGameLogic().restartGame();
		dtm.getNexus().getGameLogic().getCurrentGame().setEnded(true);
	}

	private Team getWinner() {
		int teamsAlive = 0;
		Team onlyOneAlive = null;

		// Iterate teams and test for solid monuments
		for (Team t : dtm.getNexus().getGameLogic().getCurrentGame().getTeams()) {
			DTMTeam team = (DTMTeam) t;
			boolean hasMonuments = false;
			for (Monument mon : team.getMonuments()) {
				if (!mon.broken)
					hasMonuments = true;
			}

			if (hasMonuments) {
				teamsAlive++;
				onlyOneAlive = team;
			}
		}
		if (teamsAlive == 1)
			return onlyOneAlive;
		return null;
	}

}
