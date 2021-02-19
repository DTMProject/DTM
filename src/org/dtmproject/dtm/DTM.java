package org.dtmproject.dtm;

import java.io.File;
import java.util.List;
import java.util.Optional;

import com.google.inject.Guice;
import com.google.inject.Injector;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.dtmproject.dtm.configuration.LangConfig;
import org.dtmproject.dtm.injection.DTMBinderModule;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import org.dtmproject.dtm.commands.DTMCommand;
import org.dtmproject.dtm.commands.EditModeCommand;
import org.dtmproject.dtm.commands.GetposCommand;
import org.dtmproject.dtm.commands.JoinCommand;
import org.dtmproject.dtm.commands.NextMapCommand;
import org.dtmproject.dtm.commands.PauseCommand;
import org.dtmproject.dtm.commands.PlayTimeCommand;
import org.dtmproject.dtm.commands.SetMonumentCommand;
import org.dtmproject.dtm.commands.SpectateCommand;
import org.dtmproject.dtm.commands.StartCommand;
import org.dtmproject.dtm.commands.StatsCommand;
import org.dtmproject.dtm.commands.TopCommand;
import org.dtmproject.dtm.commands.WorldsCommand;
import org.dtmproject.dtm.data.DTMDataHandler;
import org.dtmproject.dtm.data.DefaultMapLoader;
import org.dtmproject.dtm.events.AnvilPlaceListener;
import org.dtmproject.dtm.events.ChatHandler;
import org.dtmproject.dtm.events.ConnectionListener;
import org.dtmproject.dtm.events.DeathHandler;
import org.dtmproject.dtm.events.DestroyMonumentListener;
import org.dtmproject.dtm.events.SpawnProtectionListener;
import org.dtmproject.dtm.events.TeamSpleefListener;
import org.dtmproject.dtm.logic.CountdownHandler;
import org.dtmproject.dtm.logic.DTMLogicHandler;
import org.dtmproject.dtm.scoreboard.ScoreboardHandler;
import org.dtmproject.dtm.shop.ShopCommand;
import org.dtmproject.dtm.shop.ShopHandler;
import lombok.Getter;
import org.yaml.snakeyaml.DumperOptions;

public class DTM extends JavaPlugin {
	public static final String DEFAULT_PREFIX = "§eDTM-Jonne";

	private final Injector injector;

	@Getter
	private ShopHandler shopHandler;

	@Getter
	private ScoreboardHandler scoreboardHandler;

	@Getter
	private DTMDataHandler dataHandler;

	@Getter
	private DTMLogicHandler logicHandler;

	@Getter
	private EditModeCommand editModeHandler;

	@Getter
	private DeathHandler deathHandler;

	@Getter
	private CountdownHandler countdownHandler;

	@Getter
	private NameTagColorer nameTagColorer;

	@Getter
	private DefaultMapLoader defaultMapLoader;

	@Getter
	private final LangConfig lang;

	@SneakyThrows
	public DTM() {
		final YAMLConfigurationLoader loader = YAMLConfigurationLoader.builder()
				.setFile(new File(this.getDataFolder().getAbsolutePath() + "/lang.yml"))
                .setFlowStyle(DumperOptions.FlowStyle.BLOCK)
				.build();

		final ConfigurationNode node = loader.load();
		this.lang = LangConfig.loadFrom(node);

		this.lang.saveTo(node);
		loader.save(node);

		this.injector = Guice.createInjector(new DTMBinderModule(this, this.lang));
	}

	@Override
	public void onEnable() {
		this.injector.injectMembers(this);

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
