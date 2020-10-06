package com.juubes.dtmproject.playerdata;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import com.juubes.nexus.NexusBlockLocation;
import com.juubes.nexus.NexusLocation;
import com.juubes.nexus.TopListEntry;
import com.juubes.nexus.TopListEntryTotal;
import com.juubes.nexus.data.AbstractDatabaseManager;
import com.juubes.nexus.data.AbstractSeasonStats;
import com.zaxxer.hikari.HikariDataSource;

public class DTMDatabaseManager extends AbstractDatabaseManager {
	private final DTM dtm;
	private final Nexus nexus;

	private final HashMap<String, MapSettings> mapSettings;
	private final ConcurrentHashMap<UUID, DTMPlayerData> loadedPlayerdata = new ConcurrentHashMap<>(20);

	private final QueueDataSaver dataSaver;

	public final File mapConfFolder;
	public final File kitFile;

	private HikariDataSource HDS;

	public DTMDatabaseManager(DTM dtm) {
		this.dtm = dtm;
		this.nexus = dtm.getNexus();
		this.mapSettings = new HashMap<>();
		this.mapConfFolder = new File(nexus.getConfigFolder(), "settings");
		this.kitFile = new File(nexus.getConfigFolder(), "kits.yml");
		this.dataSaver = new QueueDataSaver(dtm);
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
		HDS.setConnectionTimeout(10000);
		HDS.setLeakDetectionThreshold(5000);
		HDS.setMinimumIdle(1);
		HDS.setMaximumPoolSize(20);
		try {
			Connection conn = HDS.getConnection();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.kickPlayer("§e§lDTM\n§b      Palvelin uudelleenkäynnistyy teknisistä syistä.");
			}
			Bukkit.shutdown();
		}
		// Create tables
		try (Connection conn = HDS.getConnection(); Statement stmt = conn.createStatement()) {
			String createTables = IOUtils.toString(dtm.getResource("create-tables.sql"));
			String[] sqlStatements = createTables.split(";");
			for (String sql : sqlStatements) {
				stmt.addBatch(sql);
			}
			stmt.executeBatch();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}

		dataSaver.init();
	}

	@Override
	public void createMap(String mapID, String displayName, int ticks) {
		MapSettings settings = new MapSettings(mapID, new HashSet<>());
		settings.setDisplayName(displayName);
		settings.setTicks(ticks);
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
	public NexusLocation getLobbyForMap(String mapID) {
		return mapSettings.get(mapID).getLobby();
	}

	@Override
	public void saveLobbyForMap(String mapID, NexusLocation lobby) {
		this.mapSettings.get(mapID).setLobby(lobby);
	}

	@Override
	public Set<String> getTeamList(String mapID) {
		return this.mapSettings.get(mapID).getTeamIDs();
	}

	@Override
	public void setTeamList(String mapID, Set<String> teamIDs) {
		this.mapSettings.get(mapID).loadTeamSettings(teamIDs);
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
				NexusBlockLocation block = monSet.getValue().loc.getBlock();
				String customName = monSet.getValue().customName;
				monuments[j++] = new Monument(block, id, customName);
			}
			teams[i++] = new DTMTeam(nexus, ts.ID, ts.color, ts.displayName, ts.spawn, monuments);
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
	public void saveTeamSpawn(String mapID, String teamID, NexusLocation spawn) {
		this.mapSettings.get(mapID).getTeamSettings(teamID).spawn = spawn;
	}

	public void prepareMapSettings(String[] mapIDs) {
		// Load map settings
		for (String mapID : mapIDs) {
			FileConfiguration conf = YamlConfiguration.loadConfiguration(new File(nexus.getConfigFolder(), "./settings/"
					+ mapID + ".yml"));

			String displayName = conf.getString("name");
			NexusLocation lobby = LocationUtils.toLocation(conf.getString("lobby"));
			int ticks = conf.getInt("ticks");
			MapSettings settings = new MapSettings(mapID, new HashSet<>(conf.getStringList("team-names")), displayName,
					lobby, ticks, true);

			try {
				for (String teamID : settings.getTeamIDs()) {
					TeamSettings ts = settings.getTeamSettings(teamID);
					ts.color = ChatColor.valueOf(conf.getString("teams." + teamID + ".color"));
					ts.displayName = conf.getString("teams." + teamID + ".name");
					ts.spawn = LocationUtils.toLocation(conf.getString("teams." + teamID + ".spawn"));

					if (ts.spawn == null) {
						Bukkit.broadcastMessage("§eDTM epäonnistui spawnin lataamisessa " + mapID + ":" + teamID);
						System.err.println("DTM epäonnistui spawnin lataamisessa " + mapID + ":" + teamID);
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
						Bukkit.broadcastMessage("§eDTM epäonnistui monumenttien lataamisessa " + mapID + ":" + teamID);
						System.err.println("DTM epäonnistui monumenttien lataamisessa " + mapID + ":" + teamID);
					}
				}

			} catch (Exception e) {
				Bukkit.broadcastMessage("§eDTM epäonnistui tiimien lataamisessa mapille " + mapID);
				System.err.println("DTM epäonnistui tiimien lataamisessa mapille " + mapID);
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

	public DTMPlayerData getPlayerData(Player p) {
		return getPlayerData(p.getUniqueId());
	}

	@Override
	public DTMPlayerData getPlayerData(UUID uuid) {
		return loadedPlayerdata.get(uuid);
	}

	/**
	 * Saves the playerdata to MySQL. Also calls
	 * {@link DTMDatabaseManager#saveSeasonStats(AbstractSeasonStats)}
	 */
	@Override
	public void savePlayerData(UUID uuid) {
		this.savePlayerData(loadedPlayerdata.get(uuid));
	}

	/**
	 * Asynchronously saves playerdata.
	 */
	public void savePlayerData(DTMPlayerData data) {
		dataSaver.queue(data);
	}

	@Override
	public LinkedList<TopListEntry> getLeaderboard(int count, int season) {
		LinkedList<TopListEntry> allStats = new LinkedList<>();

		try (Connection conn = HDS.getConnection();
				PreparedStatement stmt = conn.prepareStatement(
						"SELECT PlayerData.UUID, LastSeenName, Kills, Deaths, MonumentsDestroyed, Wins, Losses, PlayTimeWon, PlayTimeLost, LongestKillStreak FROM SeasonStats INNER JOIN PlayerData ON PlayerData.UUID = SeasonStats.UUID WHERE Season = ? ORDER BY (Kills *  3 + Deaths + MonumentsDestroyed * 10 + PlayTimeWon/1000/60*5 + PlayTimeLost/1000/60) DESC LIMIT ?")) {
			stmt.setInt(1, season);
			stmt.setInt(2, count);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					UUID uuid = UUID.fromString(rs.getString("UUID"));
					int kills = rs.getInt("Kills");
					int deaths = rs.getInt("Deaths");
					int monuments = rs.getInt("MonumentsDestroyed");
					int wins = rs.getInt("Wins");
					int losses = rs.getInt("Losses");
					long playTimeWon = rs.getLong("PlayTimeWon");
					long playTimeLost = rs.getLong("PlayTimeLost");
					int longestKillStreak = rs.getInt("LongestKillStreak");

					String lastSeenName = rs.getString("LastSeenName");
					DTMSeasonStats stats = new DTMSeasonStats(uuid, season, kills, deaths, monuments, wins, losses,
							playTimeWon, playTimeLost, longestKillStreak);

					allStats.add(new TopListEntry(uuid, lastSeenName, stats));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return allStats;
	}

	// Method not used anywhere in the DTM code
	@Override
	public LinkedList<TopListEntryTotal> getAlltimeLeaderboard(int count) {
		// TODO: Implement method
		return null;
	}

	/**
	 * Loads playerdata and seasonstats to cache.
	 */
	public void loadPlayerdata(UUID uuid, String lastSeenName) {
		if (loadedPlayerdata.contains(uuid))
			throw new IllegalStateException("Playerdata already loaded!");

		{
			try (Connection conn = HDS.getConnection();
					PreparedStatement stmt = conn.prepareStatement(
							"SELECT Prefix, Emeralds, Nick, KillStreak FROM PlayerData WHERE UUID = ?")) {
				stmt.setString(1, uuid.toString());

				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						String prefix = rs.getString("Prefix");
						int emeralds = rs.getInt("Emeralds");
						String nick = rs.getString("Nick");
						int killStreak = rs.getInt("KillStreak");

						loadedPlayerdata.put(uuid, new DTMPlayerData(nexus, uuid, lastSeenName, prefix, emeralds, nick,
								killStreak));
					} else {
						loadedPlayerdata.put(uuid, new DTMPlayerData(nexus, uuid, lastSeenName));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		DTMPlayerData pd = getPlayerData(uuid);
		try (Connection conn2 = HDS.getConnection();
				PreparedStatement stmt1 = conn2.prepareStatement("SELECT * FROM SeasonStats WHERE UUID = ?")) {
			stmt1.setString(1, uuid.toString());
			try (ResultSet rs = stmt1.executeQuery()) {
				HashMap<Integer, DTMSeasonStats> stats = new HashMap<>();
				while (rs.next()) {
					int season = rs.getInt("Season");
					int kills = rs.getInt("Kills");
					int deaths = rs.getInt("Deaths");
					int monuments = rs.getInt("MonumentsDestroyed");
					int wins = rs.getInt("Wins");
					int losses = rs.getInt("Losses");
					long playTimeWon = rs.getLong("PlayTimeWon");
					long playTimeLost = rs.getLong("PlayTimeLost");
					int longestKillStreak = rs.getInt("LongestKillStreak");

					stats.put(season, new DTMSeasonStats(uuid, season, kills, deaths, monuments, wins, losses,
							playTimeWon, playTimeLost, longestKillStreak));
				}
				if (stats.isEmpty())
					stats.put(1, new DTMSeasonStats(uuid, 1));
				pd.loadSeasonStats(stats);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void unloadPlayerdata(UUID uuid, boolean save) {
		if (save)
			savePlayerData(uuid);
		loadedPlayerdata.remove(uuid);
	}

	public Connection getConnection() throws SQLException {
		return HDS.getConnection();
	}

}
