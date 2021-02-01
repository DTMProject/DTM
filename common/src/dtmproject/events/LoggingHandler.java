package dtmproject.events;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import dtmproject.DTM;
import dtmproject.logic.GameState;

/**
 * Logs various information about games and events.
 */
public class LoggingHandler implements ILoggingHandler {
    private PrintWriter gameLogger, connectionLogger, monumentLogger;

    public LoggingHandler(DTM pl) {
	try {
	    new File(pl.getDataFolder(), "logs").mkdirs();

	    
	    new BufferedWriter()
	    
	    
	    this.gameLogger = new PrintWriter(new FileWriter(new File(pl.getDataFolder(), "logs/games.log")), true);
	    this.connectionLogger = new PrintWriter(
		    new FileWriter(new File(pl.getDataFolder(), "logs/connections.log")), true);
	    this.monumentLogger = new PrintWriter(new FileWriter(new File(pl.getDataFolder(), "logs/monuments.log")),
		    true);

	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

    @Override
    public void logGameStart(String mapId, HashMap<String, Integer> teamPlayerCounts) {
	this.gameLogger.println(String.format("S:%d:%s", System.currentTimeMillis(), mapId));
	for (Entry<String, Integer> e : teamPlayerCounts.entrySet()) {
	    this.gameLogger.println(String.format("	%s:%d", e.getKey(), e.getValue()));
	}
    }

    @Override
    public void logGameEnd(String mapId, String winnerTeamId, HashMap<String, Integer> teamPlayerCounts) {
	this.gameLogger.println(String.format("E:%d:%s:%s", System.currentTimeMillis(), mapId, winnerTeamId));
	for (Entry<String, Integer> e : teamPlayerCounts.entrySet()) {
	    this.gameLogger.println(String.format("	%s:%d", e.getKey(), e.getValue()));
	}
    }

    @Override
    public void logMonumentDestroyed(String mapId, String teamId, String monumentPos, UUID player) {
	this.monumentLogger.println(String.format("M:%d:%s:%s:%s:%s", System.currentTimeMillis(), mapId, teamId,
		monumentPos, player.toString()));
    }

    @Override
    public void logPlayerJoin(UUID player) {
	this.connectionLogger.println(String.format("J:%d:%s", System.currentTimeMillis(), player.toString()));
    }

    @Override
    public void logPlayerQuit(UUID player, String mapId, long timeAfterStart, GameState gameState) {
	this.connectionLogger.println(String.format("Q:%d:%s:%d:%s", System.currentTimeMillis(), player.toString(),
		timeAfterStart, gameState.toString()));
    }

    @Override
    public void stopLogging() {
	connectionLogger.close();
	gameLogger.close();
	monumentLogger.close();
    }
}
