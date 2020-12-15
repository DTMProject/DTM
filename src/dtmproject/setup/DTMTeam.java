package dtmproject.setup;

import org.bukkit.ChatColor;

import dtmproject.WorldlessLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class DTMTeam {

	@Getter
	private final String ID;

	@Getter
	@Setter
	private String displayName;
	
	@Getter
	@Setter
	private ChatColor teamColor;
	
	@Getter
	@Setter
	private WorldlessLocation spawn;
	
	@Getter
	@Setter
	private Monument[] monuments;

}
