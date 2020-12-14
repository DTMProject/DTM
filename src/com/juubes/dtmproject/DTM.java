package com.juubes.dtmproject;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.juubes.dtmproject.commands.DTMCommand;
import com.juubes.dtmproject.commands.SetMonumentCommand;
import com.juubes.dtmproject.commands.TopCommand;
import com.juubes.dtmproject.events.AnvilPlaceEvent;
import com.juubes.dtmproject.events.ChatHandler;
import com.juubes.dtmproject.events.ConnectionListener;
import com.juubes.dtmproject.events.DeathHandler;
import com.juubes.dtmproject.events.DestroyMonumentListener;
import com.juubes.dtmproject.events.FixTeleport;
import com.juubes.dtmproject.events.PreWorldLoadListener;
import com.juubes.dtmproject.events.SpawnProtectionListener;
import com.juubes.dtmproject.events.TeamSpleefListener;
import com.juubes.dtmproject.playerdata.DTMDatabaseManager;
import com.juubes.dtmproject.shop.ShopCommand;
import com.juubes.dtmproject.shop.ShopHandler;
import com.juubes.nexus.InitOptions;
import com.juubes.nexus.Lang;
import com.juubes.nexus.Nexus;
import com.juubes.nexus.commands.CreateMapCommand;

public class DTM extends JavaPlugin {
	private final Nexus nexus;
	private final ShopHandler shopHandler;
	private final DeathHandler deathHandler;
	private final ScoreboardManager sbManager;
	private final DTMDatabaseManager dbManager;

	public DTM() {
		this.nexus = (Nexus) Bukkit.getPluginManager().getPlugin("Nexus");
		this.dbManager = new DTMDatabaseManager(this);
		this.sbManager = new ScoreboardManager(this);
		this.deathHandler = new DeathHandler(this);
		this.shopHandler = new ShopHandler(this);
	}

	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new DestroyMonumentListener(this), this);
		Bukkit.getPluginManager().registerEvents(new SpawnProtectionListener(this), this);
		// Bukkit.getPluginManager().registerEvents(new InstakillTNTHandler(this),
		// this);
		// Bukkit.getPluginManager().registerEvents(new ArrowsDestroyBlocks(this),
		// this);
		Bukkit.getPluginManager().registerEvents(new ConnectionListener(this), this);
		Bukkit.getPluginManager().registerEvents(new TeamSpleefListener(this), this);
		Bukkit.getPluginManager().registerEvents(new ChatHandler(this), this);
		Bukkit.getPluginManager().registerEvents(new AnvilPlaceEvent(), this);
		Bukkit.getPluginManager().registerEvents(new FixTeleport(), this);

		Bukkit.getPluginManager().registerEvents(deathHandler, this);
		Bukkit.getPluginManager().registerEvents(shopHandler, this);
		Bukkit.getPluginManager().registerEvents(sbManager, this);

		// Events from Nexus
		Bukkit.getPluginManager().registerEvents(new PreWorldLoadListener(this), this);

		getCommand("setmonument").setExecutor(new SetMonumentCommand(this));
		getCommand("createmap").setExecutor(new CreateMapCommand(nexus));
		getCommand("DTM").setExecutor(new DTMCommand(this));
		getCommand("top").setExecutor(new TopCommand(this));
		getCommand("shop").setExecutor(new ShopCommand(this));

		saveDefaultConfig();

		List<String> maps = nexus.getConfig().getStringList("maps");
		InitOptions options = new InitOptions(new DTMGameLoader(this), dbManager, maps, null, "./Nexus");

		dbManager.prepareMapSettings(options.getMapIDs());
		dbManager.loadCache();

		Nexus.getAPI().getLang().loadTranslations(this.getResource("lang.yml"));

		nexus.init(options);

		sbManager.updateScoreboard();

		// Broadcast map changes not saved
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for (CommandSender sender : nexus.getEditModeHandler().getPendingList()) {
				sender.sendMessage(Lang.get("map-settings-pending"));
			}
		}, 20 * 20, 20 * 20);

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for (Player p : Bukkit.getOnlinePlayers()) {
				this.getDatabaseManager().savePlayerData(p.getUniqueId());
			}
		}, 3 * 60 * 20, 3 * 60 * 20);
	}

	@Override
	public void onDisable() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			this.getDatabaseManager().savePlayerData(p.getUniqueId());
			p.kickPlayer(Lang.get("server-restarting"));
		}
	}

	public DeathHandler getDeathHandler() {
		return deathHandler;
	}

	public Nexus getNexus() {
		return nexus;
	}

	public DTMDatabaseManager getDatabaseManager() {
		return dbManager;
	}

	public ScoreboardManager getScoreboardManager() {
		return sbManager;
	}

	public ShopHandler getShopHandler() {
		return shopHandler;
	}
}
