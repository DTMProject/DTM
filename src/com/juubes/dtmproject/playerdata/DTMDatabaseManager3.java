package com.juubes.dtmproject.playerdata;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

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

public class DTMDatabaseManager3 extends AbstractDatabaseManager {
	private final Nexus nexus;
	/**
	 * <mapID, settings>
	 */
	private HashMap<String, MapSettings> mapSettings;

	public File mapConfFolder;
	public File kitFile;
	private Connection conn;

	public DTMDatabaseManager3(Nexus nexus) {
		this.nexus = nexus;
		this.mapConfFolder = new File("../Nexus/settings/");
		this.kitFile = new File("../Nexus/kits.yml");

		checkConnection();
		// Load data
		try (Statement stmt = conn.createStatement()) {
			stmt.executeQuery("SELECT * FROM PlayerData");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkConnection() {
		if (conn == null) {
			FileConfiguration conf = nexus.getConfig();
			String pw = conf.getString("mysql.password");
			String user = conf.getString("mysql.user");
			String server = conf.getString("mysql.server");
			String db = conf.getString("mysql.database");

			System.out.println("Connecting to " + server + "/" + db + " as user " + user);
			try {
				conn = DriverManager.getConnection("jdbc:mysql://" + server + "/" + db, user, pw);
			} catch (SQLException e) {
				e.printStackTrace();
				for (Player p : Bukkit.getOnlinePlayers()) {
					p.kickPlayer("�e�lDTM\n�b      Palvelin uudelleenk�ynnistyy teknisist� syist�.");
				}
				Bukkit.shutdown();
			}
		} else {
			try {
				if (!conn.isValid(0)) {
					conn.close();
					conn = null;
					checkConnection();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return;
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

	/**
	 * Creates and loads playerdata from database
	 */
	public DTMPlayerData getPlayerData(UUID id) {
		checkConnection();
		try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM PlayerData WHERE UUID = ?")) {
			stmt.setString(1, id.toString());
			try (ResultSet rs = stmt.executeQuery()) {
				DTMPlayerData pd = new DTMPlayerData(null, null, null, null, 0, null, 0) {

					@Override
					public DTMSeasonStats getTotalStats() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public DTMSeasonStats getSeasonStats() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public DTMSeasonStats getSeasonStats(int season) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public void save() {
						// TODO Auto-generated method stub

					}

				};

				if (rs.next()) {
					pd.setPrefix(rs.getString("PREFIX"));
					pd.setKillStreak(rs.getInt("KILLSTREAK"));
					pd.setEmeralds(rs.getInt("EMERALDS"));
					try {
						// TODO: Make database compactible
						pd.setNick(rs.getString("NICK"));
					} catch (Exception e) {
					}
				}
				return pd;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Something went wrong loading the playerdata for \n	" + id);
	}

	@Override
	public void savePlayerData(AbstractPlayerData data) {
		checkConnection();
		// Save playerdata
		try (PreparedStatement stmt = conn.prepareStatement(
				"INSERT INTO PlayerData(UUID, NAME, PREFIX, EMERALDS, KILLSTREAK) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE NAME = VALUES(NAME), PREFIX = VALUES(PREFIX), EMERALDS = VALUES(EMERALDS), KILLSTREAK = VALUES(KILLSTREAK)")) {
			stmt.setString(1, data.getUUID().toString());
			stmt.setString(2, data.getLastSeenName());
			stmt.setString(3, data.getPrefix());
			stmt.setInt(4, data.getEmeralds());
			stmt.setInt(5, data.getKillStreak());
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Save monthly and total stats

		DTMSeasonStats m = (DTMSeasonStats) data.getSeasonStats();
		try (PreparedStatement stmt = conn.prepareStatement(
				"INSERT INTO `SeasonStats`(`STATS_ID`, `SEASON`, `UUID`, `KILLS`, `DEATHS`, `MONUMENTS_DESTROYED`, `WINS`, `LOSSES`, `PLAY_TIME_WON`, `PLAY_TIME_LOST`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE KILLS = VALUES(KILLS), DEATHS = VALUES(DEATHS), MONUMENTS_DESTROYED = VALUES(MONUMENTS_DESTROYED), WINS = VALUES(WINS), LOSSES = VALUES(LOSSES), PLAY_TIME_WON = VALUES(PLAY_TIME_WON), PLAY_TIME_LOST = VALUES(PLAY_TIME_LOST)")) {
			stmt.setInt(1, m.getSeasonStatsID());
			stmt.setInt(2, nexus.getCurrentSeason());
			stmt.setString(3, m.getUUID().toString());
			stmt.setInt(4, m.kills);
			stmt.setInt(5, m.deaths);
			stmt.setInt(6, m.monuments);
			stmt.setInt(7, m.wins);
			stmt.setInt(8, m.losses);
			stmt.setLong(9, m.playTimeWon);
			stmt.setLong(10, m.playTimeLost);
			stmt.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public DTMSeasonStats getSeasonStats(UUID id, int season) {
		checkConnection();
		try (PreparedStatement stmt = conn.prepareStatement(
				"SELECT * FROM SeasonStats WHERE UUID = ? AND SEASON = ?")) {
			stmt.setString(1, id.toString());
			stmt.setInt(2, season);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					DTMSeasonStats stats = new DTMSeasonStats(rs.getInt("STATS_ID"), id, season);
					stats.kills = rs.getInt("KILLS");
					stats.deaths = rs.getInt("DEATHS");
					stats.monuments = rs.getInt("MONUMENTS_DESTROYED");
					stats.wins = rs.getInt("WINS");
					stats.losses = rs.getInt("LOSSES");
					stats.playTimeWon = rs.getLong("PLAY_TIME_WON");
					stats.playTimeLost = rs.getLong("PLAY_TIME_LOST");
					return stats;
				} else {
					nexus.getLogger().warning("Didn't find any data for player " + id);
					return new DTMSeasonStats(0, id, season);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new DTMSeasonStats(0, id, season);
	}

	@Override
	public DTMTotalStats getTotalStats(UUID id) {
		return null;
	}

	@Override
	public AbstractSeasonStats getSeasonStats(String name, int season) {
		checkConnection();
		// TODO: get correct season
		try (PreparedStatement stmt = conn.prepareStatement(
				"SELECT PD.UUID, SS.* FROM [PlayerData] PD, [SeasonStats] SS, [PDSS] PDSS WHERE PD.Name = ? AND PDSS.UUID = PD.UUID AND PDSS.SeasonStatsID = SS.SeasonStatsID")) {
			stmt.setString(1, name);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					DTMSeasonStats stats = new DTMSeasonStats(rs.getInt("SeasonStatsID"), UUID.fromString(rs.getString(
							"UUID")), season);
					stats.kills = rs.getInt("KILLS");
					stats.deaths = rs.getInt("DEATHS");
					stats.monuments = rs.getInt("MONUMENTS_DESTROYED");
					stats.wins = rs.getInt("WINS");
					stats.losses = rs.getInt("LOSSES");
					stats.playTimeWon = rs.getLong("PLAY_TIME_WON");
					stats.playTimeLost = rs.getLong("PLAY_TIME_LOST");
					return stats;
				} else {
					// If the name hasn't been logged to stats, return null
					nexus.getLogger().warning("Didn't find any data for player " + name);
					return null;
				}
			}
		} catch (SQLException e) {
		}
		// Error
		return null;
	}

	@Override
	public void saveSeasonStats(AbstractSeasonStats stats) {
		// TODO
	}

	@Override
	public AbstractTotalStats getTotalStats(String name) {
		return null;
	}

	@Override
	public void saveTeamSpawn(String mapID, String teamID, Location spawn) {
		this.mapSettings.get(mapID).getTeamSettings(teamID).spawn = spawn;
	}

	public void prepareMapSettings(String[] mapIDs) {
		this.mapSettings = new HashMap<>(mapIDs.length);

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
	public LinkedList<DTMSeasonStats> getLeaderboard(int length, int season) {
		LinkedList<DTMSeasonStats> topData = new LinkedList<>();
		try (PreparedStatement stmt = conn.prepareStatement(
				"SELECT *, KILLS * 3 + DEATHS + MONUMENTS_DESTROYED * 10 + PLAY_TIME_WON / 1000 / 60 * 5 + PLAY_TIME_LOST / 1000 / 60 AS SUM FROM SeasonStats WHERE SEASON = ? ORDER BY SUM DESC LIMIT ?")) {
			stmt.setInt(1, season);
			stmt.setInt(2, length);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					DTMSeasonStats stats = new DTMSeasonStats(rs.getInt("STATS_ID"), UUID.fromString(rs.getString(
							"UUID")), season);
					stats.kills = rs.getInt("KILLS");
					stats.deaths = rs.getInt("DEATHS");
					stats.monuments = rs.getInt("MONUMENTS_DESTROYED");
					stats.wins = rs.getInt("WINS");
					stats.losses = rs.getInt("LOSSES");
					stats.playTimeWon = rs.getLong("PLAY_TIME_WON");
					stats.playTimeLost = rs.getLong("PLAY_TIME_LOST");
					topData.add(stats);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return topData;
	}

	@Override
	public AbstractPlayerData getPlayerData(Player p) {
		return null;
	}

	@Override
	public UUID getUUIDByLastSeenName(String name) {
		return null;
	}

	@Override
	public LinkedList<? extends AbstractTotalStats> getAlltimeLeaderboard(int count) {
		// TODO Auto-generated method stub
		return null;
	}
}
