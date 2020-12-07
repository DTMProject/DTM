package com.juubes.dtmproject;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.juubes.dtmproject.commands.DTMCommand;
import com.juubes.dtmproject.commands.SetMonumentCommand;
import com.juubes.dtmproject.commands.TopCommand;
import com.juubes.dtmproject.events.AnvilPlaceEvent;
import com.juubes.dtmproject.events.ArrowsDestroyBlocks;
import com.juubes.dtmproject.events.ChatHandler;
import com.juubes.dtmproject.events.ConnectionListener;
import com.juubes.dtmproject.events.DeathHandler;
import com.juubes.dtmproject.events.DestroyMonumentListener;
import com.juubes.dtmproject.events.FixTeleport;
import com.juubes.dtmproject.events.PreWorldLoadListener;
import com.juubes.dtmproject.events.SpawnProtectionListener;
import com.juubes.dtmproject.events.TeamSpleefListener;
import com.juubes.dtmproject.playerdata.DTMDataHandler;
import com.juubes.dtmproject.setup.DTMGameLogic.DTMGameLogic;
import com.juubes.dtmproject.shop.ShopCommand;
import com.juubes.dtmproject.shop.ShopHandler;
import com.juubes.nexus.Nexus;
import com.juubes.nexus.data.AbstractDataHandler;
import com.juubes.nexus.logic.AbstractLogicHandler;

public class DTM extends Nexus {
	private final ShopHandler shopHandler;
	private final DeathHandler deathHandler;
	private final ScoreboardManager sbManager;

	public DTM() {
		super(new DTMDataHandler(), new DTMGameLogic());
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
		Bukkit.getPluginManager().registerEvents(new ArrowsDestroyBlocks(this), this);
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

		this.getDataHandler().init();

		sbManager.updateScoreboard();

		// Broadcast map changes not saved TODO
//		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
//			for (CommandSender sender : this.getEditModeHandler().getPendingList()) {
//				sender.sendMessage("§eDTM-mappeja ei ole tallennettu.");
//			}
//		}, 20 * 20, 20 * 20);

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			Bukkit.getOnlinePlayers().forEach(p -> this.getDataHandler().savePlayerData(p.getUniqueId()));
		}, 3 * 60 * 20, 3 * 60 * 20);
	}

	@Override
	public void onDisable() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			this.getDataHandler().savePlayerData(p.getUniqueId());
			p.kickPlayer("§ePalvelin käynnistyy uudelleen.");
		}
	}

	public DeathHandler getDeathHandler() {
		return deathHandler;
	}

	@Override
	public DTMGameLogic getLogicHandler() {
		return (DTMGameLogic) super.getLogicHandler();
	}

	@Override
	public DTMDataHandler getDataHandler() {
		return (DTMDataHandler) super.getDataHandler();
	}

	public ScoreboardManager getScoreboardManager() {
		return sbManager;
	}

	public ShopHandler getShopHandler() {
		return shopHandler;
	}
}
