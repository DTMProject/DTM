package com.juubes.dtmproject.setup;

import com.juubes.dtmproject.DTM;
import com.juubes.nexus.logic.Game;

public class DTMGame extends Game {

//	private final Set<Monument> monuments;

	public DTMGame(DTM dtm, String mapRequest) {
		super(dtm.getNexus(), mapRequest);

	}

}
