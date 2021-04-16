package dtmproject.api;

import java.util.Collection;

import dtmproject.common.logic.IDTMLogicHandler;
import dtmproject.common.data.IDTMDataHandler;

public interface DTMAPI {
    public IShopHandler getShopHandler();

    public IScoreboardHandler getScoreboardHandler();

    public IDTMDataHandler<?, ?> getDataHandler();

    public IDTMLogicHandler<?, ?> getLogicHandler();

    public int getSeason();

    public Collection<String> getActiveMapList();
}
