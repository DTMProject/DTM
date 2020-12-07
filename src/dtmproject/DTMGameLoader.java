package com.juubes.dtmproject;

import com.juubes.dtmproject.setup.DTMGame;
import com.juubes.nexus.logic.Game;
import com.juubes.nexus.logic.GameLoader;

public class DTMGameLoader extends GameLoader {
	private final DTM dtm;

	public DTMGameLoader(DTM dtm) {
		super(dtm.getNexus());
		this.dtm = dtm;
	}

	@Override
	public Game loadGame(String request) {
		return new DTMGame(dtm, request);
	}
}
