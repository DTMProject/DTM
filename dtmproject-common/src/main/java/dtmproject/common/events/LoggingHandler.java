package dtmproject.common.events;

import java.util.HashMap;
import java.util.UUID;

import dtmproject.api.events.ILoggingHandler;
import dtmproject.api.logic.GameState;
import dtmproject.common.DTM;

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
	pl.getDataHandler().logGameStart(mapId, teamPlayerCounts);
    }

    @Override
    public void logGameEnd(String mapId, String winnerTeamId, HashMap<String, Integer> teamPlayerCounts) {
	pl.getDataHandler().logGameEnd(mapId, winnerTeamId, teamPlayerCounts);
    }

    @Override
    public void logMonumentDestroyed(String mapId, String teamId, String monumentPos, UUID player) {
	pl.getDataHandler().logMonumentDestroyed(mapId, teamId, monumentPos, player);
    }

    @Override
    public void logPlayerJoin(UUID playerUUID) {
	pl.getDataHandler().logPlayerJoin(playerUUID);
    }

    @Override
    public void logPlayerLeave(UUID playerUUID, String mapId, long timeAfterStart, GameState gameState) {
	pl.getDataHandler().logPlayerLeave(playerUUID, mapId, timeAfterStart, gameState);
    }
}
