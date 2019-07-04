package com.juubes.dtmproject.playerdata;

import java.util.HashMap;
import java.util.UUID;

import com.juubes.nexus.data.AbstractSeasonStats;
import com.juubes.nexus.data.AbstractTotalStats;

public class DTMTotalStats extends AbstractTotalStats {

	public DTMTotalStats(UUID uuid, HashMap<Integer, DTMSeasonStats> allStats) {
		super(uuid, allStats);
	}

	public int getMonuments() {
		int sum = 0;
		for (AbstractSeasonStats s : allStats.values()) {
			sum += ((DTMSeasonStats) s).monuments;
		}
		return sum;
	}

}