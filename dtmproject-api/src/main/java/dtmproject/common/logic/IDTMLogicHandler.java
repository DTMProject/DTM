package dtmproject.common.logic;

import java.util.Optional;

import org.bukkit.entity.Player;

import dtmproject.common.data.IDTMMap;
import dtmproject.common.data.IDTMTeam;

public interface IDTMLogicHandler<M extends IDTMMap<?>, T extends IDTMTeam<?>> {
    public M getCurrentMap();

    public GameState getGameState();

    public GameState getGameStatePrePause();

    public void loadNextGame(boolean startInstantly, Optional<String> mapRequest);

    /**
     * Starts the already loaded game.
     */
    public void startGame();

    public void endGame(T winner);

    public void togglePause();

    public void setPlayerToWorstTeam(Player p);

    public void updateNameTag(Player p);

    public T getSmallestTeam();
}
