package dtmproject;

import java.util.Collection;

import dtmproject.data.IDTMDataHandler;
import dtmproject.logic.IDTMLogicHandler;

public interface DTMAPI {
    public IShopHandler getShopHandler();

    public IScoreboardHandler getScoreboardHandler();

    public IDTMDataHandler<?, ?> getDataHandler();

    public IDTMLogicHandler<?, ?> getLogicHandler();

    public int getSeason();

    public Collection<String> getActiveMapList();
}
