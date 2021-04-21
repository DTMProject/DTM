package dtmproject.common.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.entity.Player;

public class ContributionCounter {
    public final HashMap<ContributionKey, LinkedList<TeamEvent>> events;

    public ContributionCounter() {
	this.events = new HashMap<>();
    }

    public double getRelativePoints(Player p, DTMTeam team, long matchTime) {
	return 0;
    }

    public long getTimePlayedForTeam(Player p, DTMTeam team) {
	ContributionKey key = new ContributionKey(p.getUniqueId(), team);
	LinkedList<TeamEvent> events = this.events.get(key);

	if (events.size() == 0)
	    return 0;

	long totalTime = 0;

	int max = events.size() % 2 == 0 ? events.size() : events.size() - 1;
	for (int i = 1; i < max; i++) {
	    TeamEvent event = events.get(i);
	    totalTime += event.timeStamp - events.get(i - 1).timeStamp;
	}

	// Hasn't left the game yet
	if (max < events.size()) {
	    // Add remaining
	    totalTime += System.currentTimeMillis() - events.getLast().timeStamp;
	}

	return totalTime;
    }

    public void playerJoined(UUID player, IDTMTeam<?> team) {
	ContributionKey key = new ContributionKey(player, team);
	LinkedList<TeamEvent> events = this.events.getOrDefault(key, new LinkedList<>());
	events.add(new TeamEvent(TeamEventAction.JOIN));
	this.events.put(key, events);
    }

    public void playerLeaved(UUID player, IDTMTeam<?> team) {
	ContributionKey key = new ContributionKey(player, team);

	for (Entry<ContributionKey, LinkedList<TeamEvent>> e : this.events.entrySet()) {
	    System.out.println(e.getKey());

	    for (TeamEvent event : e.getValue()) {
		System.out.println("    " + event.action.name());
	    }
	}

	LinkedList<TeamEvent> events = Objects.requireNonNull(this.events.get(key));
	events.add(new TeamEvent(TeamEventAction.LEAVE));
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
    public int hashCode() {
	return Objects.hash(player, team);
    }

    @Override
    public String toString() {
	return player.toString() + " - " + team.getId();
    }
}

class TeamEvent {
    public long timeStamp;
    public TeamEventAction action;

    public TeamEvent(TeamEventAction action) {
	this.action = action;
	this.timeStamp = System.currentTimeMillis();
    }
}

enum TeamEventAction {
    JOIN, LEAVE
}
