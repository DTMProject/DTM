package dtmproject.logic;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import dtmproject.DTM;
import dtmproject.WorldlessLocation;
import dtmproject.data.DTMMap;
import dtmproject.data.DTMPlayerData;
import dtmproject.setup.DTMTeam;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

public class DTMLogicHandler {
	private final DTM pl;

	@Getter
	@Setter
	private DTMMap currentMap;

	@Getter
	private GameState gameState;

	public DTMLogicHandler(DTM pl) {
		this.pl = pl;
	}

	/**
	 * Starts the already loaded game. Sends joined players to game.
	 */
	public void startGame() {
		this.gameState = GameState.RUNNING;
		pl.getCountdownHandler().stopStartGameCountdown();
		this.currentMap.startGame();
	}

	/**
	 * 1. Loads the new game. <br>
	 * 2. Teleports players to new world. <br>
	 * 3. Unloads last game.
	 */
	public void loadNextGame(boolean countdownChange, Optional<String> mapRequest) {
		pl.getCountdownHandler().stopChangeMapCountdown();
		pl.getCountdownHandler().stopStartGameCountdown();
		
		Optional<DTMMap> lastMap = Optional.ofNullable(currentMap);
		List<String> maps = pl.getMapList();

		String lastMapId = lastMap.isPresent() ? lastMap.get().getId() : null;

		if (mapRequest.isPresent()) {
			// Select requested
			if (lastMapId != mapRequest.get() && maps.contains(mapRequest.get())) {
				this.currentMap = pl.getDataHandler().getMap(mapRequest.get());
			} else {
				this.currentMap = pl.getDataHandler().getMap(selectRandomMapId(maps, lastMapId));
			}
		} else {
			// Select random map -- exclude last map
			this.currentMap = pl.getDataHandler().getMap(selectRandomMapId(maps, lastMapId));
		}

		this.currentMap.load();

		gameState = countdownChange ? GameState.COUNTDOWN : GameState.RUNNING;

		for (Player p : Bukkit.getOnlinePlayers()) {
			DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
			p.teleport(this.currentMap.getLobby().orElse(new WorldlessLocation(0, 100, 0)).toLocation(this.currentMap
					.getWorld()));
			pd.setTeam(null);
			pd.setLastDamager(null);
		}

		if (countdownChange) {
			pl.getCountdownHandler().startChangeMapCountdown(20);
		} else {
			pl.getCountdownHandler().startGameCountdown(20);
		}

		// TODO: Callevent start countdown
		// pl.getCountdownHandler().stopChangeMapCountdown();

		// Unloads the map and deletes the old world dir
		if (lastMap.isPresent())
			lastMap.get().unload();

	}

	public void endGame(DTMTeam winner) {
		this.gameState = GameState.COUNTDOWN;
		currentMap.end(winner);
		loadNextGame(true, Optional.empty());
	}

	private String selectRandomMapId(List<String> maps, String lastMapId) {
		int randIndex = (int) (Math.random() * maps.size());
		while (maps.get(randIndex).equals(lastMapId)) {
			randIndex = (int) (Math.random() * maps.size());
		}
		return maps.get(randIndex);
	}

	public void togglePause() {
		throw new NotImplementedException();
	}

	public DTMTeam setPlayerToSmallestTeam(Player p) {
		DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
		pd.setTeam(getSmallestTeam());

		if (gameState == GameState.RUNNING)
			currentMap.sendPlayerToGame(p);

		updateNameTag(p);

		return pd.getTeam();
	}

	public void updateNameTag(Player p) {
		DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
		p.setDisplayName(pd.getDisplayName());
		p.setPlayerListName("§8[" + ChatColor.translateAlternateColorCodes('&', pd.getPrefix()) + "§8] " + pd
				.getDisplayName());
		p.setCustomName(pd.getDisplayName());
		p.setCustomNameVisible(true);

		if (pd.getTeam() != null)
			pl.getNameTagColorer().changeNameTag(p, pd.getTeam().getTeamColor());
	}

	public DTMTeam getSmallestTeam() {
		Iterator<DTMTeam> teams = this.currentMap.getTeams().iterator();
		DTMTeam smallest = teams.next();
		while (teams.hasNext()) {
			DTMTeam anotherTeam = teams.next();
			if (smallest.getPlayers().size() > anotherTeam.getPlayers().size())
				smallest = anotherTeam;
		}
		return smallest;
	}

	/**
	 * Convenience method for backwards-combactibility.
	 * 
	 * Use {@link DTMMap#getWorld()}
	 * 
	 * @deprecated
	 */
	@Deprecated
	public World getCurrentWorld() {
		return this.currentMap.getWorld();
	}

}
