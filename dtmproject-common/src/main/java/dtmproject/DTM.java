<<<<<<< HEAD:common/src/dtmproject/DTM.java
package dtmproject;

import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import dtmproject.commands.DTMCommand;
import dtmproject.commands.EditModeCommand;
import dtmproject.commands.GetposCommand;
import dtmproject.commands.JoinCommand;
import dtmproject.commands.NextMapCommand;
import dtmproject.commands.PauseCommand;
import dtmproject.commands.PlayTimeCommand;
import dtmproject.commands.SetMonumentCommand;
import dtmproject.commands.SpectateCommand;
import dtmproject.commands.StartCommand;
import dtmproject.commands.StatsCommand;
import dtmproject.commands.TopCommand;
import dtmproject.commands.WorldsCommand;
import dtmproject.data.DTMPlayerDataHandler;
import dtmproject.data.DefaultMapLoader;
import dtmproject.events.AnvilPlaceListener;
import dtmproject.events.ChatHandler;
import dtmproject.events.ConnectionListener;
import dtmproject.events.DeathHandler;
import dtmproject.events.DestroyMonumentListener;
import dtmproject.events.LoggingHandler;
import dtmproject.events.SpawnProtectionListener;
import dtmproject.events.TeamSpleefListener;
import dtmproject.logic.CountdownHandler;
import dtmproject.logic.DTMLogicHandler;
import dtmproject.scoreboard.ScoreboardHandler;
import dtmproject.shop.ShopCommand;
import dtmproject.shop.ShopHandler;
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
    private final DTMPlayerDataHandler dataHandler;

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

    public DTM() {
	this.scoreboardHandler = new ScoreboardHandler(this);
	this.shopHandler = new ShopHandler(this);
	this.dataHandler = new DTMPlayerDataHandler(this);
	this.logicHandler = new DTMLogicHandler(this);
	this.editModeHandler = new EditModeCommand(this);
	this.deathHandler = new DeathHandler(this);
	this.countdownHandler = new CountdownHandler(this);
	this.nameTagColorer = new NameTagColorer();
	this.defaultMapLoader = new DefaultMapLoader(this);
	this.loggingHandler = new LoggingHandler(this);
    }

    @Override
    public void onEnable() {
	this.saveDefaultConfig();

	// Events
	PluginManager pm = Bukkit.getPluginManager();
	pm.registerEvents(new ConnectionListener(this), this);
	pm.registerEvents(new DestroyMonumentListener(this), this);
	pm.registerEvents(new SpawnProtectionListener(this), this);
	pm.registerEvents(new TeamSpleefListener(this), this);
	pm.registerEvents(new ChatHandler(this), this);
	pm.registerEvents(new AnvilPlaceListener(), this);
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
	Bukkit.getScheduler()
		.scheduleSyncRepeatingTask(this,
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
	dataHandler.getDataSaver().emptyQueueSync();

	// Stop logging
	loggingHandler.stopLogging();
    }

    public int getSeason() {
	return getConfig().getInt("season");
    }

    public List<String> getActiveMapList() {
	return getConfig().getStringList("maps");
    }
}
=======
package dtmproject;

import java.util.List;
import java.util.Optional;

import dtmproject.events.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import dtmproject.commands.DTMCommand;
import dtmproject.commands.EditModeCommand;
import dtmproject.commands.GetposCommand;
import dtmproject.commands.JoinCommand;
import dtmproject.commands.NextMapCommand;
import dtmproject.commands.PauseCommand;
import dtmproject.commands.PlayTimeCommand;
import dtmproject.commands.SetMonumentCommand;
import dtmproject.commands.SpectateCommand;
import dtmproject.commands.StartCommand;
import dtmproject.commands.StatsCommand;
import dtmproject.commands.TopCommand;
import dtmproject.commands.WorldsCommand;
import dtmproject.data.DTMDataHandler;
import dtmproject.data.DefaultMapLoader;
import dtmproject.logic.CountdownHandler;
import dtmproject.logic.DTMLogicHandler;
import dtmproject.scoreboard.ScoreboardHandler;
import dtmproject.shop.ShopCommand;
import dtmproject.shop.ShopHandler;
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
    private final DTMDataHandler dataHandler;

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

    public DTM() {
	this.scoreboardHandler = new ScoreboardHandler(this);
	this.shopHandler = new ShopHandler(this);
	this.dataHandler = new DTMDataHandler(this);
	this.logicHandler = new DTMLogicHandler(this);
	this.editModeHandler = new EditModeCommand(this);
	this.deathHandler = new DeathHandler(this);
	this.countdownHandler = new CountdownHandler(this);
	this.nameTagColorer = new NameTagColorer();
	this.defaultMapLoader = new DefaultMapLoader(this);
	this.loggingHandler = new LoggingHandler(this);
    }

    @Override
    public void onEnable() {
	this.saveDefaultConfig();

	// Events
	PluginManager pm = Bukkit.getPluginManager();
	pm.registerEvents(new ConnectionListener(this), this);
	pm.registerEvents(new DestroyMonumentListener(this), this);
	pm.registerEvents(new SpawnProtectionListener(this), this);
	pm.registerEvents(new TeamSpleefListener(this), this);
	pm.registerEvents(new ChatHandler(this), this);
	pm.registerEvents(new AnvilPlaceListener(), this);
	pm.registerEvents(new TNTListener(), this);
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
	Bukkit.getScheduler()
		.scheduleSyncRepeatingTask(this,
			() -> this.getEditModeHandler().getPendingList()
				.forEach(sender -> sender.sendMessage("§eDTM-mappeja ei ole tallennettu.")),
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
	dataHandler.getDataSaver().emptyQueueSync();
    }

    public int getSeason() {
	return getConfig().getInt("season");
    }

    public List<String> getActiveMapList() {
	return getConfig().getStringList("maps");
    }
}
>>>>>>> d72545a246c4ddde54e7f06999600d3deaeefb0d:dtmproject-common/src/main/java/dtmproject/DTM.java
