package dtmproject.logic;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.World;

import dtmproject.DTM;
import dtmproject.playerdata.DTMMap;
import lombok.Getter;
import lombok.NonNull;

public class GameWorldHandler {
	private final DTM pl;

	@Getter
	private World currentWorld;

	@Getter
	private DTMMap currentMap;

	public GameWorldHandler(DTM pl) {
		this.pl = pl;
	}

	public void nextMap(@NonNull String requestedMapID) {
		throw new NotImplementedException();
	}

}
