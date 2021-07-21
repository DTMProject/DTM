package dtmproject.common.data;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;

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

import dtmproject.api.WorldlessLocation;
import dtmproject.common.DTM;
import dtmproject.common.TeamArmorUtils;
import dtmproject.common.logic.GameState;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class DTMMap implements IDTMMap<DTMTeam> {
    public static final WorldlessLocation DEFAULT_LOBBY = new WorldlessLocation(0, 100, 0);

    private final DTM pl;

    @NonNull
    @Getter
    private final String id;

    @NonNull
    @Getter
    @Setter
    private String displayName;

    private WorldlessLocation lobby;

    @Getter
    @Setter
    private int ticks;

    @Getter
    private final LinkedHashSet<DTMTeam> teams;

    @Getter
    @Setter
    private long startTime;

    @Getter
    @Setter
    private ItemStack[] kit;

    @Getter
    @Setter
    private World world;

    public DTMMap(DTM pl, @NonNull String id, @NonNull String displayName, WorldlessLocation lobby, int ticks,
	    ItemStack[] kit, LinkedHashSet<DTMTeam> teams) {
	this.pl = pl;
	this.id = id;
	this.displayName = displayName;
	this.lobby = lobby;
	this.ticks = ticks;
	this.kit = kit;
	this.teams = teams;
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
	DTMTeam loser = pl.getLogicHandler().getCurrentMap().getTeams().stream().filter(t -> t != winner).findFirst()
		.get();
	String winnerList = Joiner.on(", ").join(winner.getPlayers().stream().map(p -> p.getDisplayName()).iterator());
	Bukkit.broadcastMessage("§ePelin voittajat: " + winnerList);
	Bukkit.broadcastMessage(winner.getTeamColor() + "§l" + winner.getDisplayName() + " §e§lvoitti pelin!");
	Bukkit.getOnlinePlayers().forEach(p -> p.setGameMode(GameMode.SPECTATOR));

	// 50 points to the winner team, 15 to losers -- weighted by played time in
	// winnerteam

	double winnerRating = winner.getAvgEloRating();
	double loserRating = loser.getAvgEloRating();
	double eloDiff = winnerRating - loserRating;

	// Recalculate elo rating
	double probWinner = (1.0 / (1.0 + Math.pow(10, (-eloDiff / 400))));

	double eloAdjustment = 30 * (1 - probWinner);

	System.out.println("Elo difference: " + eloDiff);
	System.out.println("Winner chance of victory: " + (int) (probWinner * 100) + "%");
	System.out.println("Elo adjustment for everyone: " + eloAdjustment);

	for (DTMTeam team : pl.getLogicHandler().getCurrentMap().getTeams()) {
	    team.getPlayers().forEach(p -> {
		DTMPlayerData pd = pl.getDataHandler().getPlayerData(p);

		final int MAX_PLAY_TIME = 90 * 1000 * 60;

		long uncutMatchTime = System.currentTimeMillis() - pl.getLogicHandler().getCurrentMap().getStartTime();
		long matchTime = Math.min(MAX_PLAY_TIME, uncutMatchTime);
		int minutesPlayed = (int) (matchTime / 1000 / 60);

		DTMSeasonStats stats = pd.getSeasonStats();
		long timeForTeam = Math.min(MAX_PLAY_TIME,
			pl.getContributionCounter().getTimePlayedForTeam(p.getUniqueId(), team));

		double timeForTeamFactor = (double) timeForTeam / (double) matchTime;

		int winnerPoints = (int) (timeForTeamFactor * minutesPlayed * 25);
		int loserPoints = (int) ((1 - timeForTeamFactor) * minutesPlayed * 5);

		int totalPoints = winnerPoints + loserPoints;

		// System.out.println(p.getName() + "'s time for team: " + timeForTeam);
		if (pd.getTeam() == winner) {

		    p.sendTitle("§a§lVoitto", "§aSait " + totalPoints + " pistettä!");

		    stats.increaseWins();

		    stats.increasePlayTimeWon(timeForTeam);
		    stats.increasePlayTimeLost(matchTime - timeForTeam);
		} else {

		    p.sendTitle("§c§lHäviö", "§aSait " + totalPoints + " pistettä!");

		    stats.increaseLosses();

		    stats.increasePlayTimeLost(timeForTeam);
		    stats.increasePlayTimeWon(matchTime - timeForTeam);
		}

		pd.setLastGamePlayed(System.currentTimeMillis());

		// Update elo rating individually
		if (loser.getPlayers().size() > 0 && winner.getPlayers().size() > 0) {
		    if (pd.getTeam() == winner)
			pd.adjustEloRating(timeForTeamFactor * eloAdjustment);
		    else
			pd.adjustEloRating(timeForTeamFactor * -eloAdjustment);
		}
	    });
	}
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

	pl.getContributionCounter().gameEnded();
    }

    public void sendToSpectate(Player p) {
	DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
	DTMTeam oldTeam = pd.getTeam();
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

	if (oldTeam != null)
	    pl.getContributionCounter().playerLeaved(p.getUniqueId(), oldTeam);
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
    public void setLobby(WorldlessLocation lobby) {
	this.lobby = lobby;
    }

    @Override
    public Optional<WorldlessLocation> getLobby() {
	return Optional.ofNullable(lobby);
    }
}
