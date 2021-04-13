package dtmproject.common.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import dtmproject.common.DTM;
import dtmproject.common.data.DTMMap;
import dtmproject.common.data.DTMPlayerData;
import dtmproject.common.data.DTMTeam;
import lombok.Getter;

public class DTMLogicHandler implements IDTMLogicHandler<DTMMap, DTMTeam> {
    public static int START_GAME_COUNTDOWN_SECONDS = 20;
    public static int CHANGE_MAP_COUNTDOWN_SECONDS = 30;

    private final DTM pl;

    @Getter
    private DTMMap currentMap;

    @Getter
    private GameState gameState;

    @Getter
    private GameState gameStatePrePause;

    public DTMLogicHandler(DTM pl) {
	this.pl = pl;
    }

    public void startGame() {
	this.gameState = GameState.RUNNING;
	this.currentMap.startGame();
	pl.getCountdownHandler().stopStartGameCountdown();
    }

    /**
     * 1. Loads the new game. <br>
     * 2. Teleports players to new world. <br>
     * 3. Unloads last game.
     * 
     * @param startInstantly starts the game immediately after the map has been
     *                       changed.
     */
    public void loadNextGame(boolean startInstantly, Optional<String> mapRequest) {
	pl.getCountdownHandler().stopChangeMapCountdown();
	pl.getCountdownHandler().stopStartGameCountdown();

	Optional<DTMMap> lastMap = Optional.ofNullable(currentMap);
	List<String> maps = pl.getActiveMapList();

	String lastMapId = lastMap.isPresent() ? lastMap.get().getId() : null;

	if (mapRequest.isPresent()) {
	    if (lastMapId == mapRequest.get())
		throw new IllegalStateException();
	    // Select requested
	    if (pl.getDataHandler().getLoadedMaps().contains(mapRequest.get())) {
		this.currentMap = pl.getDataHandler().getMap(mapRequest.get());
	    } else {
		this.currentMap = pl.getDataHandler().getMap(selectRandomMapId(maps, lastMapId));
	    }
	} else {
	    // Select random map -- exclude last map
	    this.currentMap = pl.getDataHandler().getMap(selectRandomMapId(maps, lastMapId));
	}

	System.out.println("Current map: " + currentMap.getId());

	this.currentMap.load();

	gameState = startInstantly ? GameState.RUNNING : GameState.PRE_START;
	if (!startInstantly) {
	    pl.getCountdownHandler().startGameCountdown(START_GAME_COUNTDOWN_SECONDS);
	}

	for (Player p : Bukkit.getOnlinePlayers()) {
	    DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
	    this.currentMap.sendToSpectate(p);
	    pd.setTeam(null);
	    pd.setLastDamager(null);
	}

	if (lastMap.isPresent())
	    pl.getScoreboardHandler().updateScoreboard();

	// TODO: Callevent start countdown
	// pl.getCountdownHandler().stopChangeMapCountdown();

	// Unloads the map and deletes the old world dir
	if (lastMap.isPresent())
	    lastMap.get().unload();

	if (startInstantly)
	    this.currentMap.startGame();
    }

    public void endGame(DTMTeam winner) {
	this.gameState = GameState.CHANGING_MAP;
	pl.getCountdownHandler().startChangeMapCountdown(CHANGE_MAP_COUNTDOWN_SECONDS);
	currentMap.end(winner);
    }

    private String selectRandomMapId(Collection<String> mapSet, String lastMapId) {
	ArrayList<String> maps = new ArrayList<>(mapSet);
	int randIndex = (int) (Math.random() * maps.size());
	while (maps.get(randIndex).equals(lastMapId)) {
	    randIndex = (int) (Math.random() * maps.size());
	}
	return maps.get(randIndex);
    }

    public void togglePause() {
	switch (gameState) {
	case CHANGING_MAP:
	    gameStatePrePause = gameState;
	    gameState = GameState.PAUSED;

	    pl.getCountdownHandler().stopChangeMapCountdown();
	    Bukkit.broadcastMessage("3>§b> §8+ §7DTM on pysäytetty!");
	    break;

	case PRE_START:
	    gameStatePrePause = gameState;
	    gameState = GameState.PAUSED;

	    pl.getCountdownHandler().stopStartGameCountdown();
	    Bukkit.broadcastMessage("3>§b> §8+ §7DTM on pysäytetty!");
	    break;
	case RUNNING:
	    gameStatePrePause = gameState;
	    gameState = GameState.PAUSED;

	    Bukkit.getOnlinePlayers().forEach(p -> {
		DTMPlayerData pd = pl.getDataHandler().getPlayerData(p);
		if (!pd.isSpectator()) {
		    p.setGameMode(GameMode.SPECTATOR);
		    p.sendMessage(
                    "3>§b> §8+ §7DTM on pysäytetty väliaikaisesti. Kun peli jatkuu, sinut teleportataan spawnille.");
		} else {
		    p.sendMessage("3>§b> §8+ §7DTM on pysäytetty väliaikaisesti.");
		}
	    });
	    break;
	case PAUSED:
	    gameState = gameStatePrePause;

	    switch (gameStatePrePause) {
	    // If game was on, continue from spawn
	    case RUNNING:
		Bukkit.getOnlinePlayers().forEach(p -> {
		    DTMPlayerData pd = pl.getDataHandler().getPlayerData(p);
		    if (!pd.isSpectator()) {
			p.teleport(pd.getTeam().getSpawn().toLocation(currentMap.getWorld()));
			p.setGameMode(GameMode.SURVIVAL);
			p.setHealth(20);
			p.setFoodLevel(20);

			p.sendMessage("3>§b> §8+ §7Peli jatkuu! Sinut on teleportattu spawnille.");
		    }
		});
		break;
	    case CHANGING_MAP:
		pl.getCountdownHandler().startChangeMapCountdown(CHANGE_MAP_COUNTDOWN_SECONDS);
		break;
	    case PRE_START:
		pl.getCountdownHandler().startGameCountdown(START_GAME_COUNTDOWN_SECONDS);
		break;
	    case PAUSED:
		throw new IllegalStateException();
	    }
	}
    }

    public void setPlayerToSmallestTeam(Player p) {
	DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
	pd.setTeam(getSmallestTeam());

	if (gameState == GameState.RUNNING)
	    currentMap.sendPlayerToGame(p);

	updateNameTag(p);
    }

    // TODO: duplicate method with DTMMap#sendSpec
    public void updateNameTag(Player p) {
	DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
	p.setDisplayName(pd.getDisplayName());
	// Handle null prefixes
	if (pd.getPrefix().isPresent()) {
	    p.setPlayerListName(
		    "§8[" + ChatColor.translateAlternateColorCodes('&', pd.getPrefix().get()) + "§8] §7" + p.getName());
	} else {
	    p.setPlayerListName("§7" + p.getName());
	}
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
}
