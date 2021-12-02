package dtmproject.common;

import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import dtmproject.api.DTMAPI;
import dtmproject.api.data.IDTMDataHandler;
import dtmproject.common.commands.DTMCommand;
import dtmproject.common.commands.EditModeCommand;
import dtmproject.common.commands.GetposCommand;
import dtmproject.common.commands.JoinCommand;
import dtmproject.common.commands.NextMapCommand;
import dtmproject.common.commands.PauseCommand;
import dtmproject.common.commands.PlayTimeCommand;
import dtmproject.common.commands.SetMonumentCommand;
import dtmproject.common.commands.SpectateCommand;
import dtmproject.common.commands.StartCommand;
import dtmproject.common.commands.StatsCommand;
import dtmproject.common.commands.TopCommand;
import dtmproject.common.commands.WorldsCommand;
import dtmproject.common.data.ContributionCounter;
import dtmproject.common.data.DTMMap;
import dtmproject.common.data.DTMPlayerData;
import dtmproject.common.data.DefaultMapLoader;
import dtmproject.common.data.impl.MongoDBDatabaseImpl;
import dtmproject.common.data.impl.MySQLDatabaseImpl;
import dtmproject.common.events.AnvilPlaceListener;
import dtmproject.common.events.ChatHandler;
import dtmproject.common.events.ConnectionListener;
import dtmproject.common.events.DeathHandler;
import dtmproject.common.events.DestroyMonumentListener;
import dtmproject.common.events.LoggingHandler;
import dtmproject.common.events.SpawnProtectionListener;
import dtmproject.common.events.TNTListener;
import dtmproject.common.events.TeamSpleefListener;
import dtmproject.common.logic.CountdownHandler;
import dtmproject.common.logic.DTMLogicHandler;
import dtmproject.common.scoreboard.ScoreboardHandler;
import dtmproject.common.shop.ShopCommand;
import dtmproject.common.shop.ShopHandler;
import lombok.Getter;

public final class DTM extends JavaPlugin implements DTMAPI {

    /**
     * This essentially makes seasons obsolete. Player's are rated using relative
     * rating instead of points.
     */
    public final static boolean USE_RELATIVE_SKILL_LEVELS = true;

    public static final String DEFAULT_PREFIX = null;

    @Getter
    private final ShopHandler shopHandler;

    @Getter
    private final ScoreboardHandler scoreboardHandler;

    @Getter
    private IDTMDataHandler<DTMPlayerData, DTMMap> dataHandler;

    @Getter
    private final DTMLogicHandler logicHandler;

    @Getter
    private final EditModeCommand editModeHandler;

    @Getter
    private final DeathHandler deathHandler;

    @Getter
    private final LoggingHandler loggingHandler;

    @Getter
    private final CountdownHandler countdownHandler;

    @Getter
    private final NameTagColorer nameTagColorer;

    @Getter
    private final DefaultMapLoader defaultMapLoader;

    /**
     * Handles information on the playtime of each player for each team. Helps to
     * determine who gets how much credit for a win.
     */
    @Getter
    private final ContributionCounter contributionCounter;

    public DTM() {
	this.scoreboardHandler = new ScoreboardHandler(this);
	this.shopHandler = new ShopHandler(this);
	this.logicHandler = new DTMLogicHandler(this);
	this.editModeHandler = new EditModeCommand(this);
	this.deathHandler = new DeathHandler(this);
	this.countdownHandler = new CountdownHandler(this);
	this.nameTagColorer = new NameTagColorer();
	this.defaultMapLoader = new DefaultMapLoader(this);
	this.loggingHandler = new LoggingHandler(this);
	this.contributionCounter = new ContributionCounter();
    }

    @Override
    public void onEnable() {
	this.saveDefaultConfig();

	String databaseSetting = getConfig().getString("database");
	if (databaseSetting.equalsIgnoreCase("MongoDB"))
	    this.dataHandler = new MongoDBDatabaseImpl(this);
	else if (databaseSetting.equalsIgnoreCase("MySQL"))
	    this.dataHandler = new MySQLDatabaseImpl(this);

	// Events
	PluginManager pm = Bukkit.getPluginManager();
	pm.registerEvents(new ConnectionListener(this), this);
	pm.registerEvents(new DestroyMonumentListener(this), this);
	pm.registerEvents(new SpawnProtectionListener(this), this);
	pm.registerEvents(new TeamSpleefListener(this), this);
	pm.registerEvents(new ChatHandler(this), this);
	pm.registerEvents(new AnvilPlaceListener(), this);
	pm.registerEvents(new TNTListener(this), this);
	pm.registerEvents(deathHandler, this);
	pm.registerEvents(shopHandler, this);
	pm.registerEvents(scoreboardHandler, this);

	// Commands
	getCommand("DTM").setExecutor(new DTMCommand(this));
	getCommand("top").setExecutor(new TopCommand(this));
	getCommand("shop").setExecutor(new ShopCommand(this));
	getCommand("setmonument").setExecutor(new SetMonumentCommand(this));
	getCommand("join").setExecutor(new JoinCommand(this));
	getCommand("spec").setExecutor(new SpectateCommand(this));
	getCommand("nextmap").setExecutor(new NextMapCommand(this));
	getCommand("playtime").setExecutor(new PlayTimeCommand(this));
	getCommand("pause").setExecutor(new PauseCommand(this));
	getCommand("stats").setExecutor(new StatsCommand(this));

	getCommand("start").setExecutor(new StartCommand(this));
	getCommand("getpos").setExecutor(new GetposCommand());

	WorldsCommand worldsCommand = new WorldsCommand();
	getCommand("worlds").setExecutor(worldsCommand);
	getCommand("world").setExecutor(worldsCommand);

	// HikariCP init and other stuff
	dataHandler.init();

	// Load playerdata; only runs after reloads
	Bukkit.getOnlinePlayers().forEach(p -> dataHandler.loadPlayerData(p.getUniqueId(), p.getName()));

	// Load maps to cache
	defaultMapLoader.copyDefaultMapFiles();
	dataHandler.loadMaps();

	// Load first map ingame
	logicHandler.loadNextGame(true, Optional.empty());

	// Initialize and update the scoreboard
	scoreboardHandler.loadGlobalScoreboard();
	scoreboardHandler.updateScoreboard();

	final int MINUTE_IN_TICKS = 60 * 20;

	// Broadcast map changes not saved
	Bukkit.getScheduler().scheduleSyncRepeatingTask(this,
		() -> this.getEditModeHandler().getPendingList()
			.forEach(sender -> sender.sendMessage("§3>§b> §8+ §7DTM-mappeja ei ole tallennettu.")),
		20 * 20, 20 * 20);

	// Autosave every 3 minutes
	Bukkit.getScheduler().scheduleSyncRepeatingTask(this,
		() -> Bukkit.getOnlinePlayers().forEach(p -> this.getDataHandler().savePlayerData(p.getUniqueId())),
		3 * MINUTE_IN_TICKS, 3 * MINUTE_IN_TICKS);

	// Reload winlossdistrcache
	Bukkit.getScheduler().runTaskTimerAsynchronously(this, dataHandler::updateWinLossDistributionCache,
		3 * MINUTE_IN_TICKS, 3 * MINUTE_IN_TICKS);

	countdownHandler.startScheduling();
    }

    @Override
    public void onDisable() {
	// Save playerdata
	Bukkit.getOnlinePlayers().forEach(p -> this.getDataHandler().savePlayerData(p.getUniqueId()));

	// Empty playerdata saving queue
	dataHandler.shutdown();
    }

    public int getSeason() {
	return getConfig().getInt("season");
    }

    public List<String> getActiveMapList() {
	return getConfig().getStringList("maps");
    }
}
