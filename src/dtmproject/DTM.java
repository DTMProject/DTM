package dtmproject;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import dtmproject.commands.DTMCommand;
import dtmproject.commands.EditModeCommand;
import dtmproject.commands.JoinCommand;
import dtmproject.commands.SetMonumentCommand;
import dtmproject.commands.SpectateCommand;
import dtmproject.commands.TopCommand;
import dtmproject.events.AnvilPlaceListener;
import dtmproject.events.ChatHandler;
import dtmproject.events.ConnectionListener;
import dtmproject.events.DeathHandler;
import dtmproject.events.DestroyMonumentListener;
import dtmproject.events.FixTeleport;
import dtmproject.events.SpawnProtectionListener;
import dtmproject.events.TeamSpleefListener;
import dtmproject.logic.DTMLogicHandler;
import dtmproject.logic.GameMapHandler;
import dtmproject.playerdata.DTMDataHandler;
import dtmproject.scoreboard.ScoreboardHandler;
import dtmproject.shop.ShopCommand;
import dtmproject.shop.ShopHandler;
import lombok.Getter;

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
	private final GameMapHandler gameWorldHandler;

	@Getter
	private final DeathHandler deathHandler;

	public DTM() {
		this.scoreboardHandler = new ScoreboardHandler(this);
		this.shopHandler = new ShopHandler(this);
		this.dataHandler = new DTMDataHandler(this);
		this.logicHandler = new DTMLogicHandler(this);
		this.gameWorldHandler = new GameMapHandler(this);
		this.editModeHandler = new EditModeCommand(this);
		this.deathHandler = new DeathHandler(this);
	}

	@Override
	public void onEnable() {
		this.saveDefaultConfig();

		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new ConnectionListener(this), this);
		pm.registerEvents(new DestroyMonumentListener(this), this);
		pm.registerEvents(new SpawnProtectionListener(this), this);
		pm.registerEvents(new ConnectionListener(this), this);
		pm.registerEvents(new TeamSpleefListener(this), this);
		pm.registerEvents(new ChatHandler(this), this);
		pm.registerEvents(new AnvilPlaceListener(), this);
		pm.registerEvents(new FixTeleport(), this);
		pm.registerEvents(deathHandler, this);
		pm.registerEvents(shopHandler, this);
		pm.registerEvents(scoreboardHandler, this);

		getCommand("DTM").setExecutor(new DTMCommand(this));
		getCommand("top").setExecutor(new TopCommand(this));
		getCommand("shop").setExecutor(new ShopCommand(this));
		getCommand("setmonument").setExecutor(new SetMonumentCommand(this));
		getCommand("join").setExecutor(new JoinCommand(this));
		getCommand("spec").setExecutor(new SpectateCommand(this));

		dataHandler.init();

		// Load playerdata; only runs after reloads
		Bukkit.getOnlinePlayers().forEach(p -> this.getDataHandler().loadPlayerData(p.getUniqueId(), p.getName()));

		// Load first map
		logicHandler.restartGame();
		
		// Initialize and update the scoreboard
		scoreboardHandler.loadGlobalScoreboard();
		scoreboardHandler.updateScoreboard();
		
		// Broadcast map changes not saved
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> this.getEditModeHandler().getPendingList().forEach(
				sender -> sender.sendMessage("§eDTM-mappeja ei ole tallennettu.")), 20 * 20, 20 * 20);

		// Autosave every 3 minutes
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> Bukkit.getOnlinePlayers().forEach(p -> this
				.getDataHandler().savePlayerData(p.getUniqueId())), 3 * 60 * 20, 3 * 60 * 20);
		
		
	}

	@Override
	public void onDisable() {
		// Save playerdata
		Bukkit.getOnlinePlayers().forEach(p -> this.getDataHandler().savePlayerData(p.getUniqueId()));
	}

	public int getSeason() {
		return getConfig().getInt("season");
	}

	public String[] getMapList() {
		throw new NotImplementedException();
	}
}
