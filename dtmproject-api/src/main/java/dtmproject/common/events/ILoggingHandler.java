package dtmproject.common.events;

import java.util.HashMap;
import java.util.UUID;

import dtmproject.common.logic.GameState;

public interface ILoggingHandler {
    public void logGameStart(String mapId, HashMap<String, Integer> teamPlayerCounts);

    public void logGameEnd(String mapId, String winnerTeamId, HashMap<String, Integer> teamPlayerCounts);

    public void logMonumentDestroyed(String mapId, String teamId, String monumentPos, UUID player);

    public void logPlayerJoin(UUID player);

    public void logPlayerLeave(UUID player, String mapId, long timeAfterStart, GameState gameState);

}
