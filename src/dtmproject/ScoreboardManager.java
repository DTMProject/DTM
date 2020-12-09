package dtmproject;

import java.util.Arrays;
import java.util.Comparator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.juubes.nexus.events.StartCountdownEvent;

import dtmproject.setup.DTMTeam;
import dtmproject.setup.Monument;

public class ScoreboardManager implements Listener {
	private final DTM dtm;
	private Scoreboard globalScoreboard;

	public ScoreboardManager(DTM dtm) {
		this.dtm = dtm;
	}

	public void updateScoreboard() {
		if (globalScoreboard == null)
			globalScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective obj = globalScoreboard.getObjective(DisplaySlot.SIDEBAR);
		if (obj != null)
			obj.unregister();
		obj = globalScoreboard.registerNewObjective("global", "dummy");
		obj.setDisplayName(ChatColor.GOLD + ChatColor.BOLD.toString() + dtm.getGameWorldHandler().getCurrentMap()
				.getMapDisplayName());
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);

		int score = 69;

		obj.getScore(getSpacer()).setScore(score--);

		int teamSpacerCount = 0;
		for (Team t : dtm.getGameWorldHandler().getCurrentMap().getTeams()) {
			DTMTeam team = (DTMTeam) t;
			obj.getScore(team.getChatColor() + ChatColor.BOLD.toString() + "   " + team.getDisplayName()).setScore(
					score--);
			obj.getScore(getSpacer()).setScore(score--);

			Arrays.sort(team.getMonuments(), new Comparator<Monument>() {
				@Override
				public int compare(Monument o1, Monument o2) {
					return o1.customName.compareTo(o2.customName);
				}
			});

			for (Monument mon : team.getMonuments()) {
				if (mon.broken)
					obj.getScore(ChatColor.GRAY + ChatColor.BOLD.toString() + "    " + mon.customName + getSpacer(
							teamSpacerCount)).setScore(score--);
				else {
					// There can be two "blacked out" or destroyed but similarly named monuments
					String name = team.getChatColor() + ChatColor.BOLD.toString() + "    " + mon.customName + getSpacer(
							teamSpacerCount);
					obj.getScore(name).setScore(score--);
				}
			}
			obj.getScore(getSpacer()).setScore(score--);
			teamSpacerCount++;
		}
	}

	private static int spacerInt = 1;

	private static String getSpacer(int count) {
		String val = "";
		for (int i = 0; i < count; i++) {
			val += " ";
		}
		return val;
	}

	private static String getSpacer() {
		String ready = "";
		spacerInt++;
		for (int i = 0; i < spacerInt % 30; i++)
			ready += " ";

		return ready;
	}

	public void changeNameTag(Player p, org.bukkit.ChatColor color) {
		String teamName = color + "";

		if (globalScoreboard.getTeam(teamName) == null)
			globalScoreboard.registerNewTeam(teamName);

		globalScoreboard.getTeam(teamName).setPrefix("Yeet " + teamName);
		globalScoreboard.getTeam(teamName).addPlayer(p);
		p.setScoreboard(globalScoreboard);
	}

	public Scoreboard getGlobalScoreboard() {
		return globalScoreboard;
	}

	@EventHandler
	public void onCountdownStart(StartCountdownEvent e) {
		this.updateScoreboard();
	}
}