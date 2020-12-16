package dtmproject.setup;

import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.entity.Player;

import dtmproject.WorldlessLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

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

	public Set<Player> getPlayers() {
		throw new NotImplementedException();
	}

}
