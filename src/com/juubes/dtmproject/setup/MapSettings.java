package com.juubes.dtmproject.setup;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;

import com.juubes.nexus.NexusLocation;

public class MapSettings {

	private HashMap<String, TeamSettings> teamSettings;
	private Set<String> teamIDs;

	private final String id;
	public String displayName;
	public NexusLocation lobby;
	public int ticks;
	public boolean sortMonumentNames = true;

	public MapSettings(String id, Set<String> teams) {
		this.id = id;
		this.loadTeamSettings(teams);
	}

	public MapSettings(String id, Set<String> teams, String displayName, NexusLocation lobby, int ticks,
			boolean sortMonumentNames) {
		super();
		this.id = id;
		this.displayName = displayName;
		this.lobby = lobby;
		this.ticks = ticks;
		this.sortMonumentNames = sortMonumentNames;

		this.loadTeamSettings(teams);
	}

	public TeamSettings getTeamSettings(String teamID) {
		if (teamSettings.containsKey(teamID))
			return teamSettings.get(teamID);

		throw new NullPointerException("Team " + teamID + " not loaded.");
	}

	public void loadTeamSettings(Set<String> teamIDs) {
		this.teamSettings = new HashMap<>(teamIDs.size());
		this.teamIDs = teamIDs;

		// Setup default values for new teams
		for (String id : teamIDs) {
			if (!teamSettings.containsKey(id)) {
				teamSettings.put(id, new TeamSettings(id, ChatColor.YELLOW, "Yellow team", null, null));
			}
		}
	}

	public Set<String> getTeamIDs() {
		return teamIDs;
	}

	public void addTeam(String teamID, TeamSettings ts) {
		if (getTeamIDs().contains(teamID))
			throw new IllegalArgumentException("Team already exists: " + teamID);

		Set<String> teamIDs = getTeamIDs();
		teamIDs.add(teamID);
		loadTeamSettings(teamIDs);
	}

	public HashMap<String, TeamSettings> getTeamSettings() {
		return teamSettings;
	}

	public String getId() {
		return id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public NexusLocation getLobby() {
		return lobby;
	}

	public int getTicks() {
		return ticks;
	}

	public void setTeamSettings(HashMap<String, TeamSettings> teamSettings) {
		this.teamSettings = teamSettings;
	}

	public void setTeamIDs(Set<String> teamIDs) {
		this.teamIDs = teamIDs;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setTicks(int ticks) {
		this.ticks = ticks;
	}

	public void setSortMonumentNames(boolean sortMonumentNames) {
		this.sortMonumentNames = sortMonumentNames;
	}

	public boolean isSortMonumentNames() {
		return sortMonumentNames;
	}

	public void setLobby(NexusLocation lobby) {
		this.lobby = lobby;
	}

}
