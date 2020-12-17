package dtmproject.logic;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.World;

import dtmproject.DTM;
import dtmproject.data.DTMMap;
import lombok.Getter;
import lombok.NonNull;

public class MapHandler {
	private final DTM pl;

	@Getter
	private World currentWorld;

	@Getter
	@NonNull
	private DTMMap currentMap;

	public MapHandler(DTM pl) {
		this.pl = pl;
	}

	public void nextMap(@NonNull String requestedMapID) {
		throw new NotImplementedException();
	}

}
