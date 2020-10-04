package com.juubes.dtmproject.events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.juubes.dtmproject.DTM;
import com.juubes.dtmproject.setup.DTMTeam;
import com.juubes.dtmproject.setup.Monument;

public class StartGameListener implements Listener {

	private final DTM dtm;

	public StartGameListener(DTM dtm) {
		this.dtm = dtm;
	}

}
