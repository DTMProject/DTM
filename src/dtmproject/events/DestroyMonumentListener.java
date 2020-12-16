package dtmproject.events;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scoreboard.Team;

import dtmproject.DTM;
import dtmproject.logic.GameState;
import dtmproject.playerdata.DTMPlayerData;
import dtmproject.playerdata.DTMSeasonStats;
import dtmproject.setup.DTMTeam;
import dtmproject.setup.Monument;

public class DestroyMonumentListener implements Listener {
	private final DTM dtm;

	public DestroyMonumentListener(DTM dtm) {
		this.dtm = dtm;
	}

	@EventHandler
	public void onDestroy(BlockBreakEvent e) {
		Player p = e.getPlayer();
		DTMPlayerData data = dtm.getDataHandler().getPlayerData(p.getUniqueId());
		if (dtm.getLogicHandler().getGameState() != GameState.RUNNING) {
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
							"§eEt ole tiimissä. Ole hyvä, ja laita gamemode 1, jos haluat muokata mappia, kun peli on käynnissä.");
					e.setCancelled(true);
				}
			}
			return;
		}

		Block b = e.getBlock();
		if (b.getWorld() != dtm.getGameWorldHandler().getCurrentWorld())
			return;

		if (!e.getBlock().getType().equals(Material.OBSIDIAN))
			return;
		for (DTMTeam nt : dtm.getGameWorldHandler().getCurrentMap().getTeams()) {
			DTMTeam team = (DTMTeam) nt;
			for (Monument mon : team.getMonuments()) {
				if (!e.getBlock().equals(mon.getBlock().getBlock(e.getBlock().getWorld())))
					continue;

				// Monument destroyed
				// Test if own
				if (data.getTeam() == team) {
					p.sendMessage("§eTämä on oman tiimisi monumentti.");
					e.setCancelled(true);
					return;
				}
				//
				// if (!ownPlayerClose(p, data) && playersWhoJoined() >= 10) {
				// e.setCancelled(true);
				// p.sendMessage("§eLähelläsi täytyy olla yksi oma tiimiläisesi!");
				// return;
				// }

				if (!mon.isBroken()) {
					// Give points to breaker and announce
					DTMPlayerData pd = dtm.getDataHandler().getPlayerData(p.getUniqueId());
					announcePlayerWhoBrokeTheMonument(p, pd, mon, team);

					// Also give points to closeby teammates
					for (Player closeByPlayer : getCloseByTeammates(p, pd)) {
						DTMPlayerData closeByPlayerData = dtm.getDataHandler().getPlayerData(closeByPlayer
								.getUniqueId());
						announcePlayerWhoBrokeTheMonument(closeByPlayer, closeByPlayerData, mon, team);
					}

					// Notify everyone
					for (Player player : Bukkit.getOnlinePlayers()) {
						player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
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

	private void announcePlayerWhoBrokeTheMonument(Player p, DTMPlayerData pd, Monument mon, DTMTeam team) {
		pd.getSeasonStats().setMonuments(pd.getSeasonStats().getMonuments() + 1);
		pd.setEmeralds(pd.getEmeralds() + 5);
		Bukkit.broadcastMessage("§e" + p.getDisplayName() + " §etuhosi monumentin " + team.getTeamColor() + mon
				.getCustomName());

	}

	private Set<Player> getCloseByTeammates(Player p, DTMPlayerData pd) {
		Set<Player> val = new HashSet<>();
		Set<Player> teamPlayers = Bukkit.getOnlinePlayers().stream().filter(player -> dtm.getDataHandler()
				.getPlayerData(player.getUniqueId()).getTeam() == pd.getTeam()).collect(Collectors.toSet());

		for (Player p2 : teamPlayers) {
			if (p == p2)
				continue;
			if (p2.getLocation().distance(p.getLocation()) < 10)
				val.add(p2);
		}
		return val;
	}

	private boolean ownPlayerClose(Player p, DTMPlayerData pd) {
		for (Player p2 : pd.getTeam().getPlayers()) {
			if (p2.getLocation().distance(p.getLocation()) < 10)
				if (p2 != p)
					if (p2.getGameMode() == GameMode.SURVIVAL)
						return true;
		}
		return false;
	}

	private void handleBrokenMonument(Monument monument) {
		monument.setBroken(true);
		dtm.getScoreboardHandler().updateScoreboard();
		DTMTeam winner = getWinner();

		// If two or more teams alive winner != null
		if (winner == null)
			return;
		String winnerList = "";
		Set<Player> players = winner.getPlayers();
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
		// Calculate Elo ratings
		LinkedHashSet<? extends DTMTeam> allTeams = dtm.getGameWorldHandler().getCurrentMap().getTeams();
		for (DTMTeam team : allTeams) {
			for (Player p : team.getPlayers()) {
				DTMPlayerData pd = dtm.getDataHandler().getPlayerData(p);
				int minutesPlayed = Math.min((int) ((System.currentTimeMillis() - dtm.getLogicHandler().getCurrentMap()
						.getStartTime()) / 1000 / 60), 60);

				int loserPoints = minutesPlayed * 5;
				int winnerPoints = minutesPlayed * 25;

				if (team == winner) {
					p.sendTitle("§a§lVoitto", "§aSait " + winnerPoints + " pistettä!");
				} else if (pd.getTeam() != null) {
					p.sendTitle("§c§lHäviö", "§aSait " + loserPoints + " pistettä!");
				}

				DTMSeasonStats stats = pd.getSeasonStats();

				if (team == winner) {
					stats.setPlayTimeWon(stats.getPlayTimeWon() + loserPoints * 60 * 1000);
					stats.setWins(stats.getWins() + 1);
				} else {
					stats.setPlayTimeLost(stats.getPlayTimeLost() + loserPoints * 60 * 1000);
					stats.setLosses(stats.getLosses() + 1);
				}
			}
		}

		// EloHandler.updateEloRating(winner, allTeams);
		dtm.getLogicHandler().restartGame();
		dtm.getGameWorldHandler().getCurrentMap().setEnded(true);
	}

	private DTMTeam getWinner() {
		int teamsAlive = 0;
		DTMTeam onlyOneAlive = null;

		// Iterate teams and test for solid monuments
		for (DTMTeam team : dtm.getGameWorldHandler().getCurrentMap().getTeams()) {
			boolean hasMonuments = false;
			for (Monument mon : team.getMonuments()) {
				if (!mon.isBroken())
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
