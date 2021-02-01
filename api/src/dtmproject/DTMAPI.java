package dtmproject;

import java.util.Collection;

import dtmproject.data.IPlayerDataHandler;
import dtmproject.logic.ILogicHandler;

public interface DTMAPI {
    public IShopHandler getShopHandler();

    public IScoreboardHandler getScoreboardHandler();

    public IPlayerDataHandler<?, ?> getDataHandler();

    public ILogicHandler<?, ?> getLogicHandler();

    public int getSeason();

    public Collection<String> getActiveMapList();
}
