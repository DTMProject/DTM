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

public class DTMLogicHandler {
	private final DTM pl;

	@Getter
	@Setter
	private DTMMap currentMap;

	@Getter
	@Setter
	private World currentWorld;

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

		this.currentMap.startGame();
	}

	/**
	 * 1. Loads the new game. <br>
	 * 2. Teleports players to new world. <br>
	 * 3. Unloads last game.
	 */
	public void loadNextGame(Optional<String> mapRequest) {
		DTMMap lastMap = currentMap;
		String lastMapId = lastMap == null ? null : lastMap.getId();
		List<String> maps = pl.getMapList();

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

		currentWorld = this.currentMap.load();

		for (Player p : Bukkit.getOnlinePlayers()) {
			DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
			p.teleport(this.currentMap.getLobby().orElse(new WorldlessLocation(0, 100, 0)).toLocation(
					this.currentWorld));
			pd.setTeam(null);
			pd.setLastDamager(null);
		}

		pl.getCountdownHandler().startGameCountdown(20);
		gameState = GameState.COUNTDOWN;

		// TODO: Callevent start countdown
		pl.getCountdownHandler().stopChangeMapCountdown();

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

	public void endGame(DTMTeam winnerTeam) {
		throw new NotImplementedException();
	}

	public void setPlayerToSmallestTeam(Player p) {
		DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
		pd.setTeam(getSmallestTeam());
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
}
