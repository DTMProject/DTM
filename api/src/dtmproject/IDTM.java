package dtmproject;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class IDTM extends JavaPlugin {
    public abstract IShopHandler getShopHandler();

    public abstract IScoreboardHandler getScoreboardHandler();

    public abstract IDTMDataHandler<?, ?> getDataHandler();

}
