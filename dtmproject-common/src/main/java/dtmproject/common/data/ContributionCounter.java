package dtmproject.common.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import dtmproject.api.data.IDTMTeam;

public class ContributionCounter {
    /**
     * Stores the join and leave timestamps for each player joined in each team
     */
    public final HashMap<UUID, HashMap<IDTMTeam<?>, LinkedList<Long>>> events;

    public ContributionCounter() {
	this.events = new HashMap<>();
    }

    /**
     * Returns the percentage of match time played in the team.
     */
    public double getRelativePoints(UUID p, IDTMTeam<?> team, long matchTime) {
	long timePlayed = getTimePlayedForTeam(p, team);
	return timePlayed / matchTime;
    }

    public long getTimePlayedForTeam(UUID player, IDTMTeam<?> team) {
	HashMap<IDTMTeam<?>, LinkedList<Long>> map = events.get(player);
	if (map == null)
	    return 0;

	LinkedList<Long> timestamps = map.get(team);

	if (timestamps == null || timestamps.size() == 0)
	    return 0;

	boolean inTeam = timestamps.size() % 2 == 1;

	long total = 0;
	if (inTeam) {
	    for (int i = 0; i < timestamps.size() - 1; i += 2) {
		total += timestamps.get(i + 1) - timestamps.get(i);
	    }

	    long current = System.currentTimeMillis();
	    total += current - timestamps.getLast();
	} else {
	    for (int i = 0; i < timestamps.size(); i += 2) {
		total += timestamps.get(i + 1) - timestamps.get(i);
	    }
	}

	return total;
    }

    public void playerJoined(UUID player, IDTMTeam<?> team) {
	HashMap<IDTMTeam<?>, LinkedList<Long>> map = events.getOrDefault(player, new HashMap<>());
	LinkedList<Long> timestamps = map.getOrDefault(team, new LinkedList<>());

	timestamps.add(System.currentTimeMillis());
	map.put(team, timestamps);
	events.put(player, map);
    }

    public void playerLeaved(UUID player, IDTMTeam<?> team) {
	HashMap<IDTMTeam<?>, LinkedList<Long>> map = events.get(player);
	LinkedList<Long> timestamps = map.get(team);

	timestamps.add(System.currentTimeMillis());
	events.put(player, map);
    }

    public void gameEnded() {
	this.events.clear();
    }

}

class ContributionKey {
    public final UUID player;
    public final IDTMTeam<?> team;

    public ContributionKey(UUID player, IDTMTeam<?> team2) {
	this.player = player;
	this.team = team2;
    }

    @Override
    public String toString() {
	return player.toString() + " - " + team.getId();
    }
}

enum TeamEventAction {
    JOIN, LEAVE
}
