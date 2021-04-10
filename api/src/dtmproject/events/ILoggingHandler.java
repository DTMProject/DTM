package dtmproject.events;

import java.util.HashMap;
import java.util.UUID;

import dtmproject.logic.GameState;

public interface ILoggingHandler {
    public void logGameStart(String mapId, HashMap<String, Integer> teamPlayerCounts);

    public void logGameEnd(String mapId, String winnerTeamId, HashMap<String, Integer> teamPlayerCounts);

    public void logMonumentDestroyed(String mapId, String teamId, String monumentPos, UUID player);

    public void logPlayerJoin(UUID player);

    public void logPlayerQuit(UUID player, String mapId, long timeAfterStart, GameState gameState);

    public void stopLogging();

}
