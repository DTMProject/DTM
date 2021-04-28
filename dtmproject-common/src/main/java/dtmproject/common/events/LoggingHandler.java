package dtmproject.common.events;

import java.util.HashMap;
import java.util.UUID;

import dtmproject.common.DTM;
import dtmproject.common.logic.GameState;

/**
 * Logs various information about games and events.
 */
public class LoggingHandler implements ILoggingHandler {
    private final DTM pl;

    public LoggingHandler(DTM pl) {
	this.pl = pl;
    }

    @Override
    public void logGameStart(String mapId, HashMap<String, Integer> teamPlayerCounts) {

    }

    @Override
    public void logGameEnd(String mapId, String winnerTeamId, HashMap<String, Integer> teamPlayerCounts) {
	
    }

    @Override
    public void logMonumentDestroyed(String mapId, String teamId, String monumentPos, UUID player) {

    }

    @Override
    public void logPlayerJoin(UUID player) {

    }

    @Override
    public void logPlayerLeave(UUID player, String mapId, long timeAfterStart, GameState gameState) {

    }
}
