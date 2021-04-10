<<<<<<< HEAD:common/src/dtmproject/logic/CountdownHandler.java
package dtmproject.logic;

import java.util.Optional;

import org.bukkit.Bukkit;

import dtmproject.DTM;
import dtmproject.data.DTMTeam;

public class CountdownHandler {
    private final DTM pl;

    private int startGame = -1;
    private int changeMap = 0;

    public CountdownHandler(DTM pl) {
	this.pl = pl;
    }

    public void startScheduling() {
	Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, () -> {
	    if (Bukkit.getOnlinePlayers().size() == 0)
		return;
	    if (changeMap > 0) {
		if (changeMap < 4 || changeMap == 10 || changeMap == 20 || changeMap == 30 || changeMap % 60 == 0)
		    Bukkit.broadcastMessage("3>§b> §8+ §7Vaihdetaan mappia " + changeMap + " sekunnissa.");
	    }
	    if (startGame > 0) {
		if (startGame < 4 || startGame == 10 || startGame == 20 || startGame == 30 || startGame % 60 == 0)
		    Bukkit.broadcastMessage("3>§b> §8+ §7Peli alkaa " + startGame + " sekunnissa.");
	    }
	    if (changeMap > 0) {
		changeMap--;
	    } else if (changeMap == 0) {
		pl.getLogicHandler().loadNextGame(false, Optional.empty());
		Bukkit.broadcastMessage("3>§b> §8+ §7Liity peliin komennolla /liity");
		changeMap = -1;
	    }

	    if (startGame > 0) {
		startGame--;
	    } else if (startGame == 0) {
		for (DTMTeam team : pl.getLogicHandler().getCurrentMap().getTeams()) {
		    if (team.getPlayers().size() == 0) {
			Bukkit.broadcastMessage("3>§b> §8+ §7Pelissä ei ole tarpeeksi pelaajia.");
			startGame = 30;
			return;
		    }
		}
		pl.getLogicHandler().startGame();
		startGame = -1;
	    }
	}, 0, 20);
    }

    public void startGameCountdown(int seconds) {
	startGame = seconds;
    }

    public void startChangeMapCountdown(int seconds) {
	changeMap = seconds;
    }

    public void stopStartGameCountdown() {
	startGame = -1;
    }

    public void stopChangeMapCountdown() {
	changeMap = -1;
    }
}
=======
package dtmproject.logic;

import java.util.Optional;

import org.bukkit.Bukkit;

import dtmproject.DTM;
import dtmproject.data.DTMTeam;

public class CountdownHandler {
    private final DTM pl;

    private int startGame = -1;
    private int changeMap = 0;

    public CountdownHandler(DTM pl) {
	this.pl = pl;
    }

    public void startScheduling() {
	Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, () -> {
	    if (Bukkit.getOnlinePlayers().size() == 0)
		return;
	    if (changeMap > 0) {
		if (changeMap < 4 || changeMap == 10 || changeMap == 20 || changeMap == 30 || changeMap % 60 == 0)
		    Bukkit.broadcastMessage("§eVaihdetaan mappia " + changeMap + " sekunnissa.");
	    }
	    if (startGame > 0) {
		if (startGame < 4 || startGame == 10 || startGame == 20 || startGame == 30 || startGame % 60 == 0)
		    Bukkit.broadcastMessage("§ePeli alkaa " + startGame + " sekunnissa.");
	    }
	    if (changeMap > 0) {
		changeMap--;
	    } else if (changeMap == 0) {
		pl.getLogicHandler().loadNextGame(false, Optional.empty());
		Bukkit.broadcastMessage("§eLiity peliin komennolla /liity");
		changeMap = -1;
	    }

	    if (startGame > 0) {
		startGame--;
	    } else if (startGame == 0) {
		for (DTMTeam team : pl.getLogicHandler().getCurrentMap().getTeams()) {
		    if (team.getPlayers().size() == 0) {
			Bukkit.broadcastMessage("§ePelissä ei ole tarpeeksi pelaajia.");
			startGame = 30;
			return;
		    }
		}
		pl.getLogicHandler().startGame();
		startGame = -1;
	    }
	}, 0, 20);
    }

    public void startGameCountdown(int seconds) {
	startGame = seconds;
    }

    public void startChangeMapCountdown(int seconds) {
	changeMap = seconds;
    }

    public void stopStartGameCountdown() {
	startGame = -1;
    }

    public void stopChangeMapCountdown() {
	changeMap = -1;
    }
}
>>>>>>> d72545a246c4ddde54e7f06999600d3deaeefb0d:dtmproject-common/src/main/java/dtmproject/logic/CountdownHandler.java
