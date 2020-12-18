package dtmproject.data;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Joiner;

import dtmproject.DTM;
import dtmproject.TeamArmorUtils;
import dtmproject.WorldlessLocation;
import dtmproject.setup.DTMTeam;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Builder.Default;
import net.md_5.bungee.api.ChatColor;

public class DTMMap {
	private final DTM pl;

	@NonNull
	@Getter
	private final String id;

	@NonNull
	@Getter
	@Setter
	private String displayName;

	@Getter
	@Setter
	private Optional<WorldlessLocation> lobby;

	@Getter
	private int ticks;

	@Getter
	private final LinkedHashSet<DTMTeam> teams;

	@Getter
	private long startTime;

	@Getter
	@Setter
	private ItemStack[] kit;

	@Getter
	private boolean running;

	@Getter
	@Setter
	private World world;

	public DTMMap(DTM pl, @NonNull String id, @NonNull String displayName, WorldlessLocation lobby, int ticks,
			ItemStack[] kit, LinkedHashSet<DTMTeam> teams) {
		this.pl = pl;
		this.id = id;
		this.displayName = displayName;
		this.lobby = Optional.of(lobby);
		this.ticks = ticks;
		this.kit = kit;
		this.teams = teams;
	}

	/**
	 * Loads the world associated with the map.
	 */
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
		File createdWorldFolder = new File(Bukkit.getWorldContainer(), this.id);

		try {
			FileUtils.deleteDirectory(createdWorldFolder);
			FileUtils.copyDirectory(savedWorld, createdWorldFolder);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Failed to load a new world: " + this.id);
		}

		WorldCreator wc = new WorldCreator(this.id);
		wc.type(WorldType.FLAT);
		wc.generatorSettings("2;0;1;");

		this.world = Bukkit.createWorld(wc);
		world.setStorm(false);
		world.setThundering(false);
		world.setTime(this.ticks);
		world.setWeatherDuration(5000000);
		world.setGameRuleValue("doDaylightCycle", "false");
		world.setGameRuleValue("randomTickSpeed", "5");
		world.setGameRuleValue("announceAchievements", "false");

		// TODO: Call event for preload
		// Bukkit.getPluginManager().callEvent(new PreLoadGameWorldEvent(nextMapID,
		// createdWorld));
	}

	/**
	 * Starts the already loaded game. Sends joined players to game.
	 */
	public void startGame() {
		// Send everyone to game
		teams.forEach(team -> team.getPlayers().forEach(this::sendPlayerToGame));

		this.startTime = System.currentTimeMillis();
	}

	public void end(DTMTeam winner) {
		String winnerList = Joiner.on(", ").join(winner.getPlayers().stream().map(p -> p.getDisplayName()).iterator());
		Bukkit.broadcastMessage("§ePelin voittajat: " + winnerList);
		Bukkit.broadcastMessage(winner.getDisplayName() + " §e§lvoitti pelin!");
		for (Player p : Bukkit.getOnlinePlayers())
			p.setGameMode(GameMode.SPECTATOR);

		// 50 points to the winner team, 15 to losers
		LinkedHashSet<? extends DTMTeam> allTeams = pl.getLogicHandler().getCurrentMap().getTeams();
		for (DTMTeam team : allTeams) {
			for (Player p : team.getPlayers()) {
				DTMPlayerData pd = pl.getDataHandler().getPlayerData(p);
				int minutesPlayed = Math.min((int) ((System.currentTimeMillis() - pl.getLogicHandler().getCurrentMap()
						.getStartTime()) / 1000 / 60), 60);

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
			}
		}

	}

	public void unload() {
		Bukkit.unloadWorld(id, false);

		// Delete the worldfolder so it doens't get messy
		try {
			FileUtils.deleteDirectory(new File(Bukkit.getWorldContainer() + "/" + id));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// For memory leak prevention (idk if it works) - Juubes
		this.setWorld(null);
	}

	public void sendToSpectate(Player p) {
		DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
		pd.setTeam(null);

		p.setGameMode(GameMode.SPECTATOR);

		// Teleport to lobby
		World currentWorld = pl.getLogicHandler().getCurrentWorld();
		Location lobby = getLobby().orElse(new WorldlessLocation(0, 100, 0)).toLocation(currentWorld);
		p.teleport(lobby);

		if (p.getWorld() != currentWorld)
			return;

		if (lobby != null) {
			p.teleport(lobby);
		} else {
			System.err.println("Lobby null for map " + getDisplayName());
			p.teleport(new Location(currentWorld, 0, 100, 0));
		}
		p.setGameMode(GameMode.SPECTATOR);
		p.getInventory().clear();

		// Handle appropriate nametag colours
		p.setDisplayName("§7" + p.getName());
		p.setPlayerListName("§8[" + ChatColor.translateAlternateColorCodes('&', pd.getPrefix()) + "§8] §7" + p
				.getName());
		p.setCustomName("§7" + p.getName());
		p.setCustomNameVisible(false);

	}

	public void sendPlayerToGame(Player p) {
		DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());

		// Reset properties and teleport to spawn
		p.setFallDistance(0);
		p.setHealthScale(20);
		p.setHealth(p.getHealthScale());
		p.setFoodLevel(20);
		p.teleport(pd.getTeam().getSpawn().toLocation(pl.getLogicHandler().getCurrentWorld()));
		p.setGameMode(GameMode.SURVIVAL);

		p.getInventory().setContents(pl.getLogicHandler().getCurrentMap().getKit());
		p.getInventory().setArmorContents(TeamArmorUtils.getArmorForTeam(p, pd.getTeam()));

		updateNameTag(p);
	}

	public void updateNameTag(Player p) {
		DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
		p.setDisplayName(pd.getDisplayName());
		p.setPlayerListName("§8[" + ChatColor.translateAlternateColorCodes('&', pd.getPrefix()) + "§8] " + pd
				.getDisplayName());
		p.setCustomName(pd.getDisplayName());
		p.setCustomNameVisible(true);

		if (pd.getTeam() != null)
			pl.getNameTagColorer().changeNameTag(p, pd.getTeam().getTeamColor());
	}

}
