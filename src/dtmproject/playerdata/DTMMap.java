package dtmproject.playerdata;

import java.util.LinkedHashSet;

import dtmproject.WorldlessLocation;
import dtmproject.setup.DTMTeam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public class DTMMap {
	@NonNull
	@Getter
	private final String id;

	@NonNull
	private String displayName;

	@NonNull
	private WorldlessLocation lobby;

	public int ticks;

	public final LinkedHashSet<? extends DTMTeam> teams = new LinkedHashSet<>();

}
