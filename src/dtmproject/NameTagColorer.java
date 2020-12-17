package dtmproject;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class NameTagColorer {

	public void changeNameTag(Player p, org.bukkit.ChatColor color) {
		String teamName = color + "";

		Scoreboard sb = p.getScoreboard();

		if (sb.getTeam(teamName) == null)
			sb.registerNewTeam(teamName);

		Team team = sb.getTeam(teamName);

		team.setPrefix(teamName);
		team.addPlayer(p);
		p.setScoreboard(sb);
	}

}
