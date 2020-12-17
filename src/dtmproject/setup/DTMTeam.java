package dtmproject.setup;

import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import dtmproject.DTM;
import dtmproject.WorldlessLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

@AllArgsConstructor
public class DTMTeam {
	private final DTM pl;

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
	private LinkedList<Monument> monuments;

	public Set<Player> getPlayers() {
		return Bukkit.getOnlinePlayers().stream().filter(p -> pl.getDataHandler().getPlayerData(p).getTeam() == this)
				.collect(Collectors.toSet());
	}
}
