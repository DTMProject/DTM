package com.juubes.dtmproject;

import java.util.Arrays;
import java.util.Comparator;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.juubes.dtmproject.setup.DTMTeam;
import com.juubes.dtmproject.setup.Monument;
import com.juubes.nexus.events.StartCountdownEvent;
import com.juubes.nexus.logic.Team;

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

		obj.setDisplayName("�e�l" + dtm.getNexus().getGameLogic().getCurrentGame().getMapDisplayName());
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);

		int score = 15;

		obj.getScore(getSpacer()).setScore(score--);

		for (Team t : dtm.getNexus().getGameLogic().getCurrentGame().getTeams()) {
			DTMTeam team = (DTMTeam) t;
			obj.getScore(team.getChatColor() + "�l   " + team.getDisplayName()).setScore(score--);
			obj.getScore(getSpacer()).setScore(score--);
			int sameMonumentNameCount = 0;

			Arrays.sort(team.getMonuments(), new Comparator<Monument>() {
				@Override
				public int compare(Monument o1, Monument o2) {
					return o1.customName.compareTo(o2.customName);
				}
			});
			for (Monument mon : team.getMonuments()) {
				if (mon.broken)
					obj.getScore("�7�l    " + mon.customName).setScore(score--);
				else {
					// There can be two "blacked out" or destroyed but similarly named monuments
					String spacer = "";
					if (obj.getScore(team.getChatColor() + "�l�m    " + mon.customName).isScoreSet()) {
						sameMonumentNameCount++;
						spacer = addSpacer(sameMonumentNameCount);
					}
					obj.getScore(team.getChatColor() + "�l    " + mon.customName + spacer).setScore(score--);
				}
			}
			obj.getScore(getSpacer()).setScore(score--);
		}
	}

	private static int spacerInt = 1;

	private static String addSpacer(int count) {
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

	public Scoreboard getGlobalScoreboard() {
		return globalScoreboard;
	}

	@EventHandler
	public void onCountdownStart(StartCountdownEvent e) {
		this.updateScoreboard();
	}
}
