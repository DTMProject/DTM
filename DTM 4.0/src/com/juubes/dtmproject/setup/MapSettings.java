package com.juubes.dtmproject.setup;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;

public class MapSettings {

	private final HashMap<String, TeamSettings> teamSettings;
	private Set<String> teamIDs;

	public String id, displayName;
	public Location lobby;
	public int ticks;

	public boolean sortMonumentNames;

	public MapSettings(Set<String> teams) {
		this.teamSettings = new HashMap<>(teams.size());
		this.setTeamIDs(teams);
	}

	public TeamSettings getTeamSettings(String teamID) {
		if (teamSettings.containsKey(teamID))
			return teamSettings.get(teamID);

		throw new NullPointerException("Team " + teamID + " not loaded.");
	}

	public void setTeamIDs(Set<String> teamIDs) {
		this.teamIDs = teamIDs;

		// Setup default values for new teams
		for (String id : teamIDs) {
			if (!teamSettings.containsKey(id)) {
				teamSettings.put(id, new TeamSettings(id, ChatColor.YELLOW, "Yellow team", null,
						null));
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
		setTeamIDs(teamIDs);
	}
}
