package dtmproject.playerdata;

import java.util.LinkedHashSet;

import dtmproject.WorldlessLocation;
import dtmproject.setup.DTMTeam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
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
	private final LinkedHashSet<? extends DTMTeam> teams = new LinkedHashSet<>();

}
