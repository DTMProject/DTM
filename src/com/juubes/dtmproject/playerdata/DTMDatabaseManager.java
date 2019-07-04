package com.juubes.dtmproject.playerdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.juubes.dtmproject.DTM;
import com.juubes.dtmproject.setup.DTMTeam;
import com.juubes.dtmproject.setup.MapSettings;
import com.juubes.dtmproject.setup.Monument;
import com.juubes.dtmproject.setup.MonumentSettings;
import com.juubes.dtmproject.setup.TeamSettings;
import com.juubes.nexus.InventoryUtils;
import com.juubes.nexus.LocationUtils;
import com.juubes.nexus.Nexus;
import com.juubes.nexus.data.AbstractDatabaseManager;
import com.juubes.nexus.data.AbstractPlayerData;
import com.juubes.nexus.data.AbstractSeasonStats;
import com.juubes.nexus.data.AbstractTotalStats;
import com.zaxxer.hikari.HikariDataSource;

public class DTMDatabaseManager extends AbstractDatabaseManager {
	private final DTM dtm;
	private final Nexus nexus;

	private final HashMap<String, MapSettings> mapSettings;
	private final HashMap<UUID, DTMPlayerData> playerDataCache = new HashMap<>();
	private final HashMap<UUID, HashMap<Integer, DTMSeasonStats>> seasonStatsCache = new HashMap<>();

	public final File mapConfFolder;
	public final File kitFile;

	private HikariDataSource HDS;

	public DTMDatabaseManager(DTM dtm) {
		this.dtm = dtm;
		this.nexus = dtm.getNexus();
		this.mapSettings = new HashMap<>();
		this.mapConfFolder = new File(nexus.getConfigFolder(), "settings");
		this.kitFile = new File(nexus.getConfigFolder(), "kits.yml");
	}

	/**
	 * Loads all playerdata and other important stuff to memory from MySQL.
	 */
	public void loadCache() {
		// Load config for MySQL credentials
		FileConfiguration conf = dtm.getConfig();
		String pw = conf.getString("mysql.password");
		String user = conf.getString("mysql.user");
		String server = conf.getString("mysql.server");
		String db = conf.getString("mysql.database");

		System.out.println("Connecting to " + server + "/" + db + " as user " + user);

		// Initialize HikariCP connection pooling
		this.HDS = new HikariDataSource();
		HDS.setPassword(pw);
		HDS.setUsername(user);
		HDS.setJdbcUrl("jdbc:mysql://" + server + "/" + db);
		HDS.addDataSourceProperty("cachePrepStmts", "true");
		HDS.addDataSourceProperty("prepStmtCacheSize", "250");
		HDS.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

		try {
			HDS.getConnection();
		} catch (Exception e) {
			e.printStackTrace();
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.kickPlayer("�e�lDTM\n�b      Palvelin uudelleenk�ynnistyy teknisist� syist�.");
			}
			Bukkit.shutdown();

		}
		// Create tables
		try (Statement stmt = HDS.getConnection().createStatement()) {
			BufferedReader in = new BufferedReader(new InputStreamReader(dtm.getResource("create-tables.sql")));
			String sql;
			while ((sql = in.readLine()) != null) {
				stmt.addBatch(sql);
			}
			stmt.executeBatch();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createMap(String mapID, String displayName, int ticks) {
		MapSettings settings = new MapSettings(new HashSet<>());
		settings.id = mapID;
		settings.displayName = displayName;
		settings.ticks = ticks;
		this.mapSettings.put(mapID, settings);
	}

	@Override
	public boolean isMapCreated(String mapID) {
		for (String id : mapSettings.keySet()) {
			if (mapID.equalsIgnoreCase(id)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String[] getMaps() {
		return mapSettings.keySet().toArray(new String[mapSettings.size()]);
	}

	@Override
	public Location getLobbyForMap(String mapID) {
		return mapSettings.get(mapID).lobby;
	}

	@Override
	public void saveLobbyForMap(String mapID, Location lobby) {
		this.mapSettings.get(mapID).lobby = lobby;
	}

	@Override
	public Set<String> getTeamList(String mapID) {
		return this.mapSettings.get(mapID).getTeamIDs();
	}

	@Override
	public void setTeamList(String mapID, Set<String> teamIDs) {
		this.mapSettings.get(mapID).setTeamIDs(teamIDs);
	}

	@Override
	public void setTeamDisplayName(String mapID, String teamID, String displayName) {
		mapSettings.get(mapID).getTeamSettings(teamID).displayName = displayName;
	}

	@Override
	public void setTeamColor(String mapID, String teamID, ChatColor color) {
		mapSettings.get(mapID).getTeamSettings(teamID).color = color;
	}

	@Override
	public DTMTeam[] getTeams(String mapID) {
		MapSettings ms = mapSettings.get(mapID);
		DTMTeam[] teams = new DTMTeam[ms.getTeamIDs().size()];
		int i = 0;
		for (String teamID : ms.getTeamIDs()) {
			TeamSettings ts = ms.getTeamSettings(teamID);
			Monument[] monuments = new Monument[ts.monumentSettings.size()];

			int j = 0;
			for (Entry<String, MonumentSettings> monSet : ts.monumentSettings.entrySet()) {
				String id = monSet.getKey();
				Block block = monSet.getValue().loc.getBlock();
				String customName = monSet.getValue().customName;
				monuments[j++] = new Monument(block, id, customName);
			}
			teams[i++] = new DTMTeam(ts.ID, ts.color, ts.displayName, ts.spawn, monuments);
		}
		return teams;
	}

	@Override
	public Inventory getKitForGame(String mapID) {
		FileConfiguration conf = YamlConfiguration.loadConfiguration(kitFile);

		// Get Base64 data to build an inventory
		String data = conf.getString(mapID);
		if (data == null)
			data = conf.getString("default");
		return InventoryUtils.inventoryFromString(data);
	}

	@Override
	public String getMapDisplayName(String mapID) {
		return mapSettings.get(mapID).displayName;
	}

	@Override
	public String getMapID(String mapDisplayName) {
		for (Entry<String, MapSettings> ms : mapSettings.entrySet()) {
			if (ms.getValue().displayName.equals(mapDisplayName))
				return ms.getKey();
		}
		throw new IllegalArgumentException("There's no map named " + mapDisplayName);
	}

	@Override
	public void saveKitForGame(String mapName, Inventory inv) {
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(kitFile);
		conf.set(mapName, InventoryUtils.inventoryToString(inv));
		try {
			conf.save(kitFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveTeamSpawn(String mapID, String teamID, Location spawn) {
		this.mapSettings.get(mapID).getTeamSettings(teamID).spawn = spawn;
	}

	public void prepareMapSettings(String[] mapIDs) {
		// Load map settings
		for (String mapID : mapIDs) {
			FileConfiguration conf = YamlConfiguration.loadConfiguration(new File(nexus.getConfigFolder(), "./settings/"
					+ mapID + ".yml"));

			MapSettings settings = new MapSettings(new HashSet<>(conf.getStringList("team-names")));
			settings.displayName = conf.getString("name");
			settings.lobby = LocationUtils.toLocation(conf.getString("lobby"));
			settings.ticks = conf.getInt("ticks");
			settings.id = mapID;

			try {
				for (String teamID : settings.getTeamIDs()) {
					TeamSettings ts = settings.getTeamSettings(teamID);
					ts.color = ChatColor.valueOf(conf.getString("teams." + teamID + ".color"));
					ts.displayName = conf.getString("teams." + teamID + ".name");
					ts.spawn = LocationUtils.toLocation(conf.getString("teams." + teamID + ".spawn"));

					if (ts.spawn == null) {
						Bukkit.broadcastMessage("�eDTM ep�onnistui spawnin lataamisessa " + mapID + ":" + teamID);
						System.err.println("DTM ep�onnistui spawnin lataamisessa " + mapID + ":" + teamID);
					}

					HashMap<String, MonumentSettings> monumentSettings = null;
					try {
						Set<String> monumentNames = conf.getConfigurationSection("teams." + teamID + ".monuments")
								.getKeys(false);
						monumentSettings = new HashMap<>(monumentNames.size());
						for (String position : monumentNames) {
							MonumentSettings ms = new MonumentSettings();
							ms.loc = LocationUtils.toLocation(conf.getString("teams." + teamID + ".monuments."
									+ position + ".loc"));
							ms.customName = conf.getString("teams." + teamID + ".monuments." + position + ".name");
							monumentSettings.put(position, ms);
						}
						ts.monumentSettings = monumentSettings;
					} catch (Exception e) {
						Bukkit.broadcastMessage("�eDTM ep�onnistui monumenttien lataamisessa " + mapID + ":" + teamID);
						System.err.println("DTM ep�onnistui monumenttien lataamisessa " + mapID + ":" + teamID);
					}
				}

			} catch (Exception e) {
				Bukkit.broadcastMessage("�eDTM ep�onnistui tiimien lataamisessa mapille " + mapID);
				System.err.println("DTM ep�onnistui tiimien lataamisessa mapille " + mapID);
				e.printStackTrace();
			}
			this.mapSettings.put(mapID, settings);
		}
	}

	@Override
	public void saveMapSettings(String mapID) {
		File f = new File(mapConfFolder, mapID + ".yml");
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);

		MapSettings settings = mapSettings.get(mapID);
		conf.set("name", settings.displayName);
		conf.set("lobby", LocationUtils.toString(settings.lobby));
		conf.set("ticks", settings.ticks);
		conf.set("team-names", settings.getTeamIDs().toArray(new String[settings.getTeamIDs().size()]));
		for (String teamID : settings.getTeamIDs()) {
			TeamSettings ts = settings.getTeamSettings(teamID);
			conf.set("teams." + teamID + ".name", ts.displayName);
			conf.set("teams." + teamID + ".spawn", LocationUtils.toString(ts.spawn));
			conf.set("teams." + teamID + ".color", ts.color.name());
			for (Entry<String, MonumentSettings> pos : ts.monumentSettings.entrySet()) {
				conf.set("teams." + teamID + ".monuments." + pos.getKey() + ".name", pos.getValue().customName);
				conf.set("teams." + teamID + ".monuments." + pos.getKey() + ".loc", LocationUtils.toString(pos
						.getValue().loc));
			}
		}

		DateFormat format = DateFormat.getDateInstance(DateFormat.LONG);
		conf.set("time-modified", format.format(Date.from(Instant.now())));

		try {
			conf.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveMonument(String mapID, String teamID, String pos, Monument monument) {
		MonumentSettings ms = new MonumentSettings();
		ms.customName = monument.customName;
		ms.loc = monument.block.getLocation();
		this.mapSettings.get(mapID).getTeamSettings(teamID).monumentSettings.put(pos, ms);
	}

	public void updateLocationWorlds(World w) {
		MapSettings ms = this.mapSettings.get(w.getName());
		if (ms.lobby != null)
			ms.lobby.setWorld(w);
		for (String teamID : this.getTeamList(w.getName())) {
			TeamSettings ts = ms.getTeamSettings(teamID);
			if (ts.spawn != null)
				ts.spawn.setWorld(w);
			for (Entry<String, MonumentSettings> monSet : ts.monumentSettings.entrySet()) {
				if (monSet.getValue().loc != null)
					monSet.getValue().loc.setWorld(w);
			}
		}
	}

	@Override
	public DTMPlayerData getPlayerData(Player p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void savePlayerData(AbstractPlayerData data) {
		// TODO Auto-generated method stub

	}

	@Override
	public UUID getUUIDByLastSeenName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractSeasonStats getSeasonStats(UUID id, int currentSeason) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractTotalStats getTotalStats(UUID id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractSeasonStats getSeasonStats(String name, int currentSeason) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractTotalStats getTotalStats(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveSeasonStats(AbstractSeasonStats stats) {
		HashMap<Integer, DTMSeasonStats> seasonStats = seasonStatsCache.getOrDefault(stats.getUUID(),
				new HashMap<Integer, DTMSeasonStats>());
		seasonStats.put(stats.getSeason(), (DTMSeasonStats) stats);
		this.seasonStatsCache.put(stats.getUUID(), seasonStats);
	}

	@Override
	public LinkedList<? extends AbstractSeasonStats> getLeaderboard(int count, int season) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<? extends AbstractTotalStats> getAlltimeLeaderboard(int count) {
		// TODO Auto-generated method stub
		return null;
	}
}
