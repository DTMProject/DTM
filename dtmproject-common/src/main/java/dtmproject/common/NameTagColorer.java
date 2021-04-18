package dtmproject.common;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.md_5.bungee.api.ChatColor;

public class NameTagColorer {

    public void changeNameTagAboveHead(Player p, ChatColor chatColor) {
	String teamName = chatColor + "";

	Scoreboard sb = p.getScoreboard();

	if (sb.getTeam(teamName) == null)
	    sb.registerNewTeam(teamName);

	Team team = sb.getTeam(teamName);

	team.setPrefix(teamName);
	team.addPlayer(p);
	p.setScoreboard(sb);
    }

}
