package dtmproject.api;

import java.util.Collection;

import dtmproject.api.data.IDTMDataHandler;
import dtmproject.api.logic.IDTMLogicHandler;

public interface DTMAPI {
    public IShopHandler getShopHandler();

    public IScoreboardHandler getScoreboardHandler();

    public IDTMDataHandler<?, ?> getDataHandler();

    public IDTMLogicHandler<?, ?> getLogicHandler();

    public int getSeason();

    public Collection<String> getActiveMapList();
}
