package dtmproject.common.data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Joiner;

import dtmproject.api.IWorldlessLocation;
import dtmproject.common.DTM;
import dtmproject.common.TeamArmorUtils;
import dtmproject.common.WorldlessLocation;
import dtmproject.common.logic.GameState;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Entity(name = "Map")
public class DTMMap implements IDTMMap<DTMTeam> {
    public static final WorldlessLocation DEFAULT_LOBBY = new WorldlessLocation(0, 100, 0);

    @Transient
    private final DTM pl;

    @Getter
    @Id
    @Column(name = "MapID", nullable = false)
    private final String id;

    @NonNull
    @Getter
    @Setter
    @Column(name = "DisplayName", nullable = false)
    private String displayName;

    @Column(name = "Lobby")
    private WorldlessLocation lobby;

    @Getter
    @Setter
    @Column(name = "Ticks", nullable = false)
    private int ticks;

    @Getter
    @OneToMany(fetch = FetchType.EAGER)
    private final Set<DTMTeam> teams;

    @Getter
    @Setter
    @Transient
    private long startTime;

    @Getter
    @Setter
    @Column(name = "Kit")
    private ItemStack[] kit;

    @Getter
    @Setter
    @Transient
    private World world;

    /**
     * Stores the time each player spent in each team.
     */
    // TODO
    private final HashMap<UUID, HashMap<DTMTeam, Integer>> contributionPoints;

    public DTMMap(DTM pl, @NonNull String id, @NonNull String displayName, WorldlessLocation lobby, int ticks,
	    ItemStack[] kit, LinkedHashSet<DTMTeam> teams) {
	this.pl = pl;
	this.id = id;
	this.displayName = displayName;
	this.lobby = lobby;
	this.ticks = ticks;
	this.kit = kit;
	this.teams = teams;

	this.contributionPoints = new HashMap<>();

    }

    /**
     * Loads the world associated with the map.
     */
    @Override
    public void load() {
	// First delete and replace with a generated world, then load it
	System.out.println("Loading map: " + this.id);

	/**
	 * The 'clean' world saved for copying.
	 */
	File savedWorld = new File(pl.getDataFolder(), "maps/" + this.id);

	/**
	 * The world folder that players interact with. Will be deleted later.
	 */
	File createdWorldFolder = Bukkit.getWorldContainer();
	try {
	    // Path has to be specified like this so the map isn't copied to the already
	    // existing dir.
	    FileUtils.deleteDirectory(new File(Bukkit.getWorldContainer(), id));
	    FileUtils.copyDirectory(savedWorld, createdWorldFolder);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.err.println("Failed to load a new world: " + this.id);
	}

	WorldCreator wc = new WorldCreator(this.id);
	wc.type(WorldType.FLAT);
	// wc.generatorSettings("2;0;1;");

	this.world = Bukkit.createWorld(wc);
	world.setStorm(false);
	world.setThundering(false);
	world.setTime(this.ticks);
	world.setWeatherDuration(5000000);
	world.setGameRuleValue("doDaylightCycle", "false");
	world.setGameRuleValue("doWeatherCycle", "false");
	world.setGameRuleValue("randomTickSpeed", "5");
	world.setGameRuleValue("announceAdvancements", "false");
	world.setGameRuleValue("maxEntityCramming", "-1");

	// Regenerate monuments if any are missing
	teams.forEach(team -> team.getMonuments().forEach(mon -> {
	    mon.getBlock().getBlock(world).setType(Material.OBSIDIAN);
	    mon.setBroken(false);
	}));

	pl.getLogger().finer("§eLoaded world " + id);
    }

    /**
     * Starts the already loaded game. Sends joined players to game.
     */
    public void startGame() {
	// Send everyone to game
	teams.forEach(team -> team.getPlayers().forEach(this::sendPlayerToGame));

	this.startTime = System.currentTimeMillis();

	// Title for everyone
	Bukkit.getOnlinePlayers()
		.forEach(p -> p.sendTitle("§eTuhoa monumentit!", "§eNopein tiimi voittaa!", 0, 5 * 20, 3 * 20));
    }

    @Override
    public void end(DTMTeam winner) {
	String winnerList = Joiner.on(", ").join(winner.getPlayers().stream().map(p -> p.getDisplayName()).iterator());
	Bukkit.broadcastMessage("§ePelin voittajat: " + winnerList);
	Bukkit.broadcastMessage(winner.getTeamColor() + "§l" + winner.getDisplayName() + " §e§lvoitti pelin!");
	Bukkit.getOnlinePlayers().forEach(p -> p.setGameMode(GameMode.SPECTATOR));

	// 50 points to the winner team, 15 to losers
	pl.getLogicHandler().getCurrentMap().getTeams().forEach(team -> {
	    team.getPlayers().forEach(p -> {
		DTMPlayerData pd = pl.getDataHandler().getPlayerData(p);
		int minutesPlayed = Math
			.min((int) ((System.currentTimeMillis() - pl.getLogicHandler().getCurrentMap().getStartTime())
				/ 1000 / 60), 90);

		int loserPoints = minutesPlayed * 5;
		int winnerPoints = minutesPlayed * 25;

		if (team == winner) {
		    p.sendTitle("§a§lVoitto", "§aSait " + winnerPoints + " pistettä!");
		} else if (pd.getTeam() != null) {
		    p.sendTitle("§c§lHäviö", "§aSait " + loserPoints + " pistettä!");
		}

		DTMSeasonStats stats = pd.getSeasonStats();

		long playTime = loserPoints * 60 * 1000;
		if (team == winner) {
		    stats.increaseWins();
		    stats.increasePlayTimeWon(playTime);
		} else {
		    stats.increaseLosses();
		    stats.increasePlayTimeLost(playTime);
		}
	    });
	});
    }

    @Override
    public void unload() {
	Bukkit.unloadWorld(id, false);

	// Delete the worldfolder so it doens't get messy
	try {
	    FileUtils.deleteDirectory(new File(Bukkit.getWorldContainer(), id));
	} catch (IOException e) {
	    e.printStackTrace();
	}

	// For memory leak prevention (idk if it works) - Juubes
	this.setWorld(null);

	this.contributionPoints.clear();
    }

    public void sendToSpectate(Player p) {
	DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
	pd.setTeam(null);

	p.setGameMode(GameMode.SPECTATOR);

	// Teleport to lobby
	Location lobby = getLobby().orElse(DTMMap.DEFAULT_LOBBY).toLocation(world);
	p.teleport(lobby);

	if (p.getWorld() != world)
	    return;

	if (lobby != null) {
	    p.teleport(lobby);
	} else {
	    System.err.println("Lobby null for map " + getDisplayName());
	    p.teleport(DTMMap.DEFAULT_LOBBY.toLocation(world));
	}
	p.setGameMode(GameMode.SPECTATOR);
	p.getInventory().clear();

	pl.getLogicHandler().updateNameTag(p);
    }

    @Override
    public void sendPlayerToGame(Player p) {
	if (pl.getLogicHandler().getGameState() != GameState.RUNNING)
	    throw new IllegalStateException();

	Objects.requireNonNull(this.world);

	DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());

	Objects.requireNonNull(pd.getTeam());

	// Reset properties and teleport to spawn
	p.setFallDistance(0);
	p.setHealthScale(20);
	p.setHealth(p.getHealthScale());
	p.setFoodLevel(20);
	p.teleport(pd.getTeam().getSpawn().toLocation(this.world));
	p.setGameMode(GameMode.SURVIVAL);

	p.getInventory().setContents(pl.getLogicHandler().getCurrentMap().getKit());
	p.getInventory().setArmorContents(TeamArmorUtils.getArmorForTeam(p, pd.getTeam()));

	pl.getLogicHandler().updateNameTag(p);
    }

    @Override
    public long getTimePlayed() {
	return System.currentTimeMillis() - startTime;
    }

    /**
     * Tests for IDs and display names.
     * 
     * @returns null if team isn't in the map.
     */
    public DTMTeam getTeamWithName(String name) {
	return teams.stream().filter(team -> team.getId().equals(name) || team.getDisplayName().equalsIgnoreCase(name))
		.findFirst().orElse(null);
    }

    @Override
    public void setLobby(IWorldlessLocation lobby) {
	this.lobby = (WorldlessLocation) lobby;
    }

    @Override
    public Optional<IWorldlessLocation> getLobby() {
	return Optional.ofNullable(lobby);
    }
}
