package com.juubes.dtmproject;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.juubes.dtmproject.commands.DTMCommand;
import com.juubes.dtmproject.commands.SetMonumentCommand;
import com.juubes.dtmproject.commands.TopCommand;
import com.juubes.dtmproject.events.ChatHandler;
import com.juubes.dtmproject.events.ConnectionListener;
import com.juubes.dtmproject.events.DeathHandler;
import com.juubes.dtmproject.events.DestroyMonumentListener;
import com.juubes.dtmproject.events.PreWorldLoadListener;
import com.juubes.dtmproject.events.SpawnProtectionListener;
import com.juubes.dtmproject.events.TeamSpleefListener;
import com.juubes.dtmproject.playerdata.DTMDatabaseManager;
import com.juubes.nexus.InitOptions;
import com.juubes.nexus.Nexus;
import com.juubes.nexus.commands.CreateMapCommand;
import com.juubes.nexus.commands.EditModeHandler;
import com.juubes.nexus.data.AbstractDatabaseManager;
import com.juubes.nexus.data.AbstractPlayerData;
import com.juubes.nexus.data.PlayerDataHandler;

public class DTM extends JavaPlugin {
	public static DTM getPlugin() {
		return (DTM) Bukkit.getPluginManager().getPlugin("DTM");
	}

	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new ConnectionListener(), this);
		Bukkit.getPluginManager().registerEvents(new ChatHandler(), this);
		Bukkit.getPluginManager().registerEvents(new DestroyMonumentListener(), this);
		Bukkit.getPluginManager().registerEvents(new ScoreboardManager(), this);
		Bukkit.getPluginManager().registerEvents(new SpawnProtectionListener(), this);
		Bukkit.getPluginManager().registerEvents(new TeamSpleefListener(), this);
		Bukkit.getPluginManager().registerEvents(new DeathHandler(), this);

		// Events from Nexus
		Bukkit.getPluginManager().registerEvents(new PreWorldLoadListener(), this);

		getCommand("setmonument").setExecutor(new SetMonumentCommand());
		getCommand("DTM").setExecutor(new DTMCommand());
		getCommand("top").setExecutor(new TopCommand());
		getCommand("createmap").setExecutor(new CreateMapCommand());
		Nexus nexus = (Nexus) Bukkit.getPluginManager().getPlugin("Nexus");
		final InitOptions options = new InitOptions();
		List<String> maps = nexus.getConfig().getStringList("maps");
		options.setMapIDs(maps.toArray(new String[maps.size()]));
		options.setDatabaseManager((AbstractDatabaseManager) new DTMDatabaseManager());

		((DTMDatabaseManager) options.getDatabaseManager()).prepareMapSettings(options.getMapIDs());

		Nexus.init(options);

		ScoreboardManager.updateScoreboard();

		// Broadcast map changes not saved
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for (CommandSender sender : EditModeHandler.pendingList) {
				sender.sendMessage("§cMappeja ei ole tallennettu. Tallenna komennolla /DTM:DTM save");
			}
		}, 20 * 20, 20 * 20);
	}

	@Override
	public void onDisable() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			PlayerDataHandler.unload(AbstractPlayerData.get(p));
			p.kickPlayer("§e§lDTM§b      \nPalvelin uudelleenkäynnistyy teknisistä syistä.");
		}
	}

	public static DTMDatabaseManager getDatabaseManager() {
		return (DTMDatabaseManager) Nexus.getDatabaseManager();
	}
}
