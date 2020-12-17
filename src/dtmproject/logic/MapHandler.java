package dtmproject.logic;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.World;

import dtmproject.DTM;
import dtmproject.data.DTMMap;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class MapHandler {
	private final DTM pl;

	@Getter
	@Setter
	private World currentWorld;

	@Getter
	@Setter
	@NonNull
	private DTMMap currentMap;

	public MapHandler(DTM pl) {
		this.pl = pl;
	}

	public void nextMap(@NonNull String requestedMapID) {
		throw new NotImplementedException();
	}

}
