package dtmproject.logic;

import java.util.Optional;

import org.bukkit.entity.Player;

import dtmproject.data.IDTMMap;
import dtmproject.data.IDTMTeam;

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

    public void setPlayerToSmallestTeam(Player p);

    public void updateNameTag(Player p);

    public T getSmallestTeam();
}
