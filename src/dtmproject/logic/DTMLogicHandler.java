package dtmproject.logic;

import java.util.Iterator;
import java.util.Optional;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import dtmproject.DTM;
import dtmproject.playerdata.DTMMap;
import dtmproject.playerdata.DTMPlayerData;
import dtmproject.setup.DTMTeam;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

public class DTMLogicHandler {
	private final DTM pl;
	private final GameMapHandler gwh;

	@Getter
	private GameState gameState;

	public DTMLogicHandler(DTM pl) {
		this.pl = pl;
		this.gwh = pl.getGameWorldHandler();
	}

	/**
	 * Starts the already loaded game. Sends joined players to game.
	 */
	public void startGame() {
		this.gameState = GameState.RUNNING;
		throw new NotImplementedException();
	}

	/**
	 * 1. Loads the new game. <br>
	 * 2. Teleports players to new world. <br>
	 * 3. Unloads last game.
	 */
	public void loadNextGame(Optional<String> mapRequest) {
		DTMMap lastMap = pl.getGameWorldHandler().getCurrentMap();
		String[] maps = pl.getMapList();

		DTMMap selectedMap;
		if (mapRequest.isPresent()) {
			// Select requested
			int foundIndex = -1;
			for (int i = 0; i < maps.length; i++) {
				if (maps[i] == mapRequest.get()) {
					foundIndex = i;
					break;
				}
			}

			selectedMap = pl.getDataHandler().getMap(foundIndex == -1 ? maps[foundIndex]
					: selectRandomMapId(maps, lastMap.getId()));
		} else {
			// Select random map -- exclude last map
			selectedMap = pl.getDataHandler().getMap(selectRandomMapId(maps, lastMap.getId()));
		}

		pl.getCountdownHandler().startGameCountdown(20);
		gameState = GameState.COUNTDOWN;

		for (Player p : Bukkit.getOnlinePlayers()) {
			DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
			pd.setTeam(null);
			pd.setLastDamager(null);
		}
		// TODO: Callevent start countdown
		pl.getCountdownHandler().stopChangeMapCountdown();

	}

	// private String getRandomOtherMapID(String lastMapID) {
	// String[] maps = pl.getMapList();
	// List<String> mapsList = Arrays.asList(maps);
	// mapsList.remove(lastMapID);
	// mapsList.get((int) (Math.random() * mapsList.size()));
	// return mapsList.get(0);
	// }

	private String selectRandomMapId(String[] maps, String lastMapId) {
		int randIndex = (int) (Math.random() * maps.length);
		if (maps.length > 1) {
			while (maps[randIndex].equals(lastMapId)) {
				randIndex = (int) (Math.random() * maps.length);
			}
		}
		return maps[randIndex];
	}

	public void togglePause() {
		throw new NotImplementedException();
	}

	public void endGame(DTMTeam winnerTeam) {
		throw new NotImplementedException();
	}

	public void sendPlayerToGame(Player p) {
		DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
		throw new NotImplementedException();
	}

	public void sendToSpectate(Player p) {
		DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
		pd.setTeam(null);

		p.setGameMode(GameMode.SPECTATOR);

		// Teleport to lobby
		DTMMap currentMap = gwh.getCurrentMap();
		World currentWorld = gwh.getCurrentWorld();
		Location lobby = currentMap.getLobby().toLocation(currentWorld);
		p.teleport(lobby);

		if (p.getWorld() != currentWorld)
			return;

		if (lobby != null) {
			p.teleport(lobby);
		} else {
			System.err.println("Lobby null for map " + currentMap.getDisplayName());
			p.teleport(new Location(currentWorld, 0, 100, 0));
		}
		p.setGameMode(GameMode.SPECTATOR);
		p.getInventory().clear();

		// Handle appropriate nametag colours
		p.setDisplayName("§7" + p.getName());
		p.setPlayerListName("§8[" + ChatColor.translateAlternateColorCodes('&', pd.getPrefix()) + "§8] §7" + p
				.getName());
		p.setCustomName(pd.getTeam().getTeamColor() + p.getName());
		p.setCustomNameVisible(false);

	}

	public void restartGame() {
		// TODO

	}

	public void setPlayerToSmallestTeam(Player p) {
		DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
		pd.setTeam(getSmallestTeam());
	}

	public DTMTeam getSmallestTeam() {
		Iterator<DTMTeam> teams = gwh.getCurrentMap().getTeams().iterator();
		DTMTeam smallest = teams.next();
		while (teams.hasNext()) {
			DTMTeam anotherTeam = teams.next();
			if (smallest.getPlayers().size() > anotherTeam.getPlayers().size())
				smallest = anotherTeam;
		}
		return smallest;
	}
}
