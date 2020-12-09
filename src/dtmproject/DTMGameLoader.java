package dtmproject;

import com.juubes.nexus.logic.Game;
import com.juubes.nexus.logic.GameLoader;

import dtmproject.setup.DTMGame;

public class DTMGameLoader extends GameLoader {
	private final DTM dtm;

	public DTMGameLoader(DTM dtm) {
		super(dtm);
		this.dtm = dtm;
	}

	@Override
	public Game loadGame(String request) {
		return new DTMGame(dtm, request);
	}
}
