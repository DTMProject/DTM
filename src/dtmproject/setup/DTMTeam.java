package dtmproject.setup;

import org.bukkit.ChatColor;

import com.juubes.nexus.Nexus;
import com.juubes.nexus.NexusLocation;
import com.juubes.nexus.data.AbstractTeam;

public class DTMTeam extends AbstractTeam {
	private Monument[] monuments;

	public DTMTeam(Nexus nexus, String ID, ChatColor teamColor, String displayName, NexusLocation spawn,
			Monument[] monuments) {
		super(ID, displayName, teamColor, spawn);
		this.monuments = monuments;
	}

	public Monument[] getMonuments() {
		return monuments;
	}

}
