package dtmproject;

import java.io.File;
import java.util.List;
import java.util.Optional;

import dtmproject.configuration.LangConfig;
import lombok.SneakyThrows;
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
import dtmproject.events.AnvilPlaceListener;
import dtmproject.events.ChatHandler;
import dtmproject.events.ConnectionListener;
import dtmproject.events.DeathHandler;
import dtmproject.events.DestroyMonumentListener;
import dtmproject.events.SpawnProtectionListener;
import dtmproject.events.TeamSpleefListener;
import dtmproject.logic.CountdownHandler;
import dtmproject.logic.DTMLogicHandler;
import dtmproject.scoreboard.ScoreboardHandler;
import dtmproject.shop.ShopCommand;
import dtmproject.shop.ShopHandler;
import lombok.Getter;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

public class DTM extends JavaPlugin {
	public static final String DEFAULT_PREFIX = "§eDTM-Jonne";

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
	private final CountdownHandler countdownHandler;

	@Getter
	private final NameTagColorer nameTagColorer;

	@Getter
	private final DefaultMapLoader defaultMapLoader;

	@Getter
	private final LangConfig lang;

	@SneakyThrows
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

		final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
				.file(new File(this.getDataFolder().getAbsolutePath() + "/lang.yml"))
				.nodeStyle(NodeStyle.BLOCK)
				.build();

		final ConfigurationNode node = loader.load();
		this.lang = node.get(LangConfig.class);

		node.set(LangConfig.class, this.lang);
		loader.save(node);
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

		// Broadcast map changes not saved
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> this.getEditModeHandler().getPendingList().forEach(
				sender -> sender.sendMessage("§eDTM-mappeja ei ole tallennettu.")), 20 * 20, 20 * 20);

		// Autosave every 3 minutes
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> Bukkit.getOnlinePlayers().forEach(p -> this
				.getDataHandler().savePlayerData(p.getUniqueId())), 3 * 60 * 20, 3 * 60 * 20);

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
