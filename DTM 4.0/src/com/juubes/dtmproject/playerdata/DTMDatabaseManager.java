package com.juubes.dtmproject.playerdata;

import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import com.juubes.nexus.logic.Team;
import com.juubes.nexus.playerdata.AbstractPlayerData;
import com.juubes.nexus.playerdata.AbstractStats;
import com.juubes.nexus.setup.AbstractDatabaseManager;

public class DTMDatabaseManager implements AbstractDatabaseManager {

	@Override
	public void createMap(String mapID, String displayName, int ticks) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isMapCreated(String mapID) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] getMaps() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Location getLobbyForMap(String mapID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveLobbyForMap(String mapID, Location lobby) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<String> getTeamList(String mapID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTeamList(String mapID, Set<String> teamIDs) {
		// TODO Auto-generated method stub

	}

	@Override
	public Team[] getTeams(String mapID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveTeamSpawn(String editWorld, String string, Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	public Inventory getKitForGame(String mapID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveKitForGame(String mapID, Inventory items) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getMapDisplayName(String mapID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMapID(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractPlayerData getPlayerData(UUID id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void savePlayerData(AbstractPlayerData data) {
		// TODO Auto-generated method stub

	}

	@Override
	public AbstractStats getSeasonStats(UUID id, int currentSeason) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractStats getTotalStats(UUID id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractStats getSeasonStats(String name, int currentSeason) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractStats getTotalStats(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveSeasonStats(AbstractStats stats, int currentSeason) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveMapSettings(String mapID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTeamDisplayName(String mapID, String teamID, String displayName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTeamColor(String mapID, String teamID, ChatColor color) {
		// TODO Auto-generated method stub

	}

}
