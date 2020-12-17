package dtmproject.playerdata;

import java.util.LinkedHashSet;

import org.apache.commons.lang.NotImplementedException;

import dtmproject.WorldlessLocation;
import dtmproject.setup.DTMTeam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class DTMMap {
	@NonNull
	@Getter
	private final String id;

	@NonNull
	@Getter
	@Setter
	private String displayName;

	@Getter
	@Setter
	private WorldlessLocation lobby;

	@Getter
	@Setter
	private int ticks;

	@Getter
	private final LinkedHashSet<DTMTeam> teams;

	@Getter
	private long startTime;
	// TODO where is this set? constructor?

	public DTMMap(@NonNull String id, @NonNull String displayName, WorldlessLocation lobby, int ticks,
			LinkedHashSet<DTMTeam> teams) {
		this.id = id;
		this.displayName = displayName;
		this.lobby = lobby;
		this.ticks = ticks;
		this.teams = teams;
	}

	public void reset() {
		// TODO
		throw new NotImplementedException();
	}

	public void end() {
		
	}

}
