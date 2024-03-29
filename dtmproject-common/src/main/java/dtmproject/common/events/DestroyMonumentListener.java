package dtmproject.common.events;

import java.util.HashSet;
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

import dtmproject.api.logic.GameState;
import dtmproject.common.DTM;
import dtmproject.common.data.DTMMonument;
import dtmproject.common.data.DTMPlayerData;
import dtmproject.common.data.DTMTeam;

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
		p.sendMessage("§3>§b> §8+ §7Et voi tuhota monumenttia spectatessa.");
		e.setCancelled(true);
	    } else {
		if (p.getGameMode() != GameMode.CREATIVE) {
		    p.sendMessage(
                    "§3>§b> §8+ §7Et ole tiimissä. Ole hyvä, ja laita gamemode 1, jos haluat muokata mappia, kun peli on käynnissä.");
		    e.setCancelled(true);
		}
	    }
	    return;
	}

	Block b = e.getBlock();
	if (b.getWorld() != dtm.getLogicHandler().getCurrentMap().getWorld())
	    return;

	if (!e.getBlock().getType().equals(Material.OBSIDIAN))
	    return;
	for (DTMTeam nt : dtm.getLogicHandler().getCurrentMap().getTeams()) {
	    DTMTeam team = (DTMTeam) nt;
	    for (DTMMonument mon : team.getMonuments()) {
		if (!e.getBlock().equals(mon.getBlock().getBlock(e.getBlock().getWorld())))
		    continue;

		// Monument destroyed
		// Test if own
		if (data.getTeam() == team) {
		    p.sendMessage("§3>§b> §8+ §7Tämä on oman tiimisi monumentti.");
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
			DTMPlayerData closeByPlayerData = dtm.getDataHandler()
				.getPlayerData(closeByPlayer.getUniqueId());
			announcePlayerWhoBrokeTheMonument(closeByPlayer, closeByPlayerData, mon, team);
		    }

		    // Notify everyone
		    for (Player player : Bukkit.getOnlinePlayers()) {
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
		    }
		    handleBrokenMonument(mon);
		} else {
		    p.sendMessage("§3>§b> §8+ §7Tämä monumentti on jo kerran tuhottu.");
		}
		e.setCancelled(true);
		e.getBlock().setType(Material.AIR);
		return;
	    }
	}
    }

    private void announcePlayerWhoBrokeTheMonument(Player p, DTMPlayerData pd, DTMMonument mon, DTMTeam team) {
	pd.getSeasonStats().increaseMonumentsDestroyed();
	pd.increaseEmeralds(5);
	Bukkit.broadcastMessage(
            "§3>§b> §8+ §7" + p.getDisplayName() + " §7tuhosi monumentin " + team.getTeamColor() + mon.getCustomName());
    }

    private Set<Player> getCloseByTeammates(Player p, DTMPlayerData pd) {
	Set<Player> val = new HashSet<>();
	Set<Player> teamPlayers = Bukkit.getOnlinePlayers().stream()
		.filter(player -> dtm.getDataHandler().getPlayerData(player.getUniqueId()).getTeam() == pd.getTeam())
		.collect(Collectors.toSet());

	for (Player p2 : teamPlayers) {
	    if (p == p2)
		continue;
	    if (p2.getLocation().distance(p.getLocation()) < 10)
		val.add(p2);
	}
	return val;
    }

    private void handleBrokenMonument(DTMMonument monument) {
	monument.setBroken(true);
	dtm.getScoreboardHandler().updateScoreboard();
	DTMTeam winner = getWinner();

	// If two or more teams alive winner != null
	if (winner == null)
	    return;

	dtm.getLogicHandler().endGame(winner);
    }

    /**
     * Checks if there's only one team "alive" with any monuments.
     */
    private DTMTeam getWinner() {
	int teamsAlive = 0;
	DTMTeam onlyOneAlive = null;

	// Iterate teams and test for solid monuments
	for (DTMTeam team : dtm.getLogicHandler().getCurrentMap().getTeams()) {
	    boolean hasMonuments = false;
	    for (DTMMonument mon : team.getMonuments()) {
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
