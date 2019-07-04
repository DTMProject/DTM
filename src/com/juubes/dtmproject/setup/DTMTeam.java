package com.juubes.dtmproject.setup;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import com.juubes.nexus.logic.Team;

public class DTMTeam extends Team {
	private Monument[] monuments;

	public DTMTeam(String ID, ChatColor teamColor, String displayName, Location spawn,
			Monument[] monuments) {
		super(ID, teamColor, displayName, spawn);
		this.monuments = monuments;
	}

	public Monument[] getMonuments() {
		return monuments;
	}

}
