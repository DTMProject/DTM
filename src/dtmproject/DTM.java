package dtmproject;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import com.juubes.nexus.Nexus;

import dtmproject.commands.DTMCommand;
import dtmproject.commands.SetMonumentCommand;
import dtmproject.commands.TopCommand;
import dtmproject.events.AnvilPlaceEvent;
import dtmproject.events.ArrowsDestroyBlocks;
import dtmproject.events.ChatHandler;
import dtmproject.events.ConnectionListener;
import dtmproject.events.DestroyMonumentListener;
import dtmproject.events.FixTeleport;
import dtmproject.events.PreWorldLoadListener;
import dtmproject.events.SpawnProtectionListener;
import dtmproject.events.TeamSpleefListener;
import dtmproject.playerdata.DTMDataHandler;
import dtmproject.setup.DTMLogicHandler;
import dtmproject.shop.ShopCommand;
import dtmproject.shop.ShopHandler;

public class DTM extends Nexus {
	private final ShopHandler shopHandler;
	private final ScoreboardManager sbManager;
	private final DTMDataHandler dataHandler;
	private final DTMLogicHandler logicHandler;

	public DTM() {
		this.sbManager = new ScoreboardManager(this);
		this.shopHandler = new ShopHandler(this);
		this.dataHandler = new DTMDataHandler(this);
		this.logicHandler = new DTMLogicHandler(this);
	}

	@Override
	public void onEnable() {
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new DestroyMonumentListener(this), this);
		pm.registerEvents(new SpawnProtectionListener(this), this);
		// pm.registerEvents(new InstakillTNTHandler(this),
		// this);
		pm.registerEvents(new ArrowsDestroyBlocks(this), this);
		pm.registerEvents(new ConnectionListener(this), this);
		pm.registerEvents(new TeamSpleefListener(this), this);
		pm.registerEvents(new ChatHandler(this), this);
		pm.registerEvents(new AnvilPlaceEvent(), this);
		pm.registerEvents(new FixTeleport(), this);

		// pm.registerEvents(deathHandler, this);
		pm.registerEvents(shopHandler, this);
		pm.registerEvents(sbManager, this);

		// Events from Nexus
		pm.registerEvents(new PreWorldLoadListener(this), this);

		getCommand("setmonument").setExecutor(new SetMonumentCommand(this));
		// getCommand("createmap").setExecutor(new CreateMapCommand(nexus));
		getCommand("DTM").setExecutor(new DTMCommand(this));
		getCommand("top").setExecutor(new TopCommand(this));
		getCommand("shop").setExecutor(new ShopCommand(this));

		saveDefaultConfig();

		this.getDataHandler().init();

		sbManager.updateScoreboard();

		// Broadcast map changes not saved TODO
		// Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
		// for (CommandSender sender : this.getEditModeHandler().getPendingList()) {
		// sender.sendMessage("§eDTM-mappeja ei ole tallennettu.");
		// }
		// }, 20 * 20, 20 * 20);

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			Bukkit.getOnlinePlayers().forEach(p -> this.getDataHandler().savePlayerData(p.getUniqueId()));
		}, 3 * 60 * 20, 3 * 60 * 20);
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}

	@Override
	public DTMLogicHandler getLogicHandler() {
		return logicHandler;
	}

	@Override
	public DTMDataHandler getDataHandler() {
		return dataHandler;
	}

	public ScoreboardManager getScoreboardHandler() {
		return sbManager;
	}

	public ShopHandler getShopHandler() {
		return shopHandler;
	}
}
