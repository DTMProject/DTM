package dtmproject.common.scoreboard;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import dtmproject.api.IScoreboardHandler;
import dtmproject.common.DTM;
import dtmproject.common.data.DTMMap;
import dtmproject.common.data.DTMMonument;
import dtmproject.common.data.DTMTeam;
import lombok.Getter;

public class ScoreboardHandler implements IScoreboardHandler, Listener {
    private final DTM pl;

    @Getter
    private Scoreboard globalScoreboard;

    private final ScoreboardSpacerHandler spacers;

    public ScoreboardHandler(DTM dtm) {
	this.pl = dtm;
	this.spacers = new ScoreboardSpacerHandler();
    }

    public void loadGlobalScoreboard() {
	if (globalScoreboard != null)
	    throw new IllegalStateException("Scoreboard already exists.");
	this.globalScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    public void updateScoreboard() {
	DTMMap currentMap = Objects.requireNonNull(pl.getLogicHandler().getCurrentMap());
	Objective obj = globalScoreboard.getObjective(DisplaySlot.SIDEBAR);
	if (obj != null)
	    obj.unregister();
	obj = globalScoreboard.registerNewObjective("global", "dummy");
	obj.setDisplayName("§e§l" + currentMap.getDisplayName());
	obj.setDisplaySlot(DisplaySlot.SIDEBAR);

	int score = 69;

	obj.getScore(spacers.getUnusedSpacer()).setScore(score--);

	int teamSpacerCount = 0;
	for (DTMTeam team : currentMap.getTeams()) {
	    // Render teamname
	    obj.getScore(team.getTeamColor() + "§l   " + team.getDisplayName()).setScore(score--);
	    obj.getScore(spacers.getUnusedSpacer()).setScore(score--);

	    // Sort monuments by name
	    team.getMonuments().sort((mon1, mon2) -> mon1.getCustomName().compareTo(mon2.getCustomName()));

	    // Render monuments under teamname
	    for (DTMMonument mon : team.getMonuments()) {
		if (mon.isBroken()) {
		    obj.getScore("§7§l    " + mon.getCustomName() + spacers.getSpacer(teamSpacerCount))
			    .setScore(score--);
		} else {
		    // There can be two "blacked out" or destroyed but similarly named monuments
		    String name = team.getTeamColor() + "§l    " + mon.getCustomName()
			    + spacers.getSpacer(teamSpacerCount);
		    obj.getScore(name).setScore(score--);
		}
	    }
	    obj.getScore(spacers.getUnusedSpacer()).setScore(score--);
	    teamSpacerCount++;
	}
    }

    public void changeNameTag(Player p, ChatColor color) {
	String teamName = color + "";

	Team team = globalScoreboard.getTeam(teamName);

	if (team == null) {
	    globalScoreboard.registerNewTeam(teamName);
	    team = globalScoreboard.getTeam(teamName);
	}

	team.setPrefix("Yeet " + teamName);
	team.addPlayer(p);
	
	team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

	p.setScoreboard(globalScoreboard);
    }
}
