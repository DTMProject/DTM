package com.juubes.dtmproject.setup;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;

public class TeamSettings {
	public ChatColor color;
	public Location spawn;
	public String ID, displayName;
	public HashMap<String, MonumentSettings> monumentSettings;

	public TeamSettings(String teamID, ChatColor color, String displayName, Location spawn,
			HashMap<String, MonumentSettings> monumentSettings) {
		this.ID = teamID;
		this.color = color;
		this.displayName = displayName;
		this.spawn = spawn;
		this.monumentSettings = monumentSettings;
		if (monumentSettings == null)
			this.monumentSettings = new HashMap<>(2);
	}
}