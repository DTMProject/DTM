package dtmproject.data;

import java.util.LinkedHashSet;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.juubes.nexus.data.AbstractPlayerData;

import dtmproject.DTM;
import dtmproject.TeamArmorUtils;
import dtmproject.WorldlessLocation;
import dtmproject.logic.GameMapHandler;
import dtmproject.setup.DTMTeam;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

public class DTMMap {
	private final DTM pl;
	private final GameMapHandler gmh;

	@NonNull
	@Getter
	private final String id;

	@NonNull
	@Getter
	@Setter
	private String displayName;

	@Getter
	@Setter
	private WorldlessLocation lobby;

	@Getter
	@Setter
	private int ticks;

	@Getter
	private final LinkedHashSet<DTMTeam> teams;

	@Getter
	private long startTime;
	// TODO where is this set? constructor?

	@Getter
	@Setter
	private ItemStack[] kit;

	public DTMMap(DTM pl, @NonNull String id, @NonNull String displayName, WorldlessLocation lobby, int ticks,
			LinkedHashSet<DTMTeam> teams) {
		this.pl = pl;
		this.gmh = pl.getGameWorldHandler();
		this.id = id;
		this.displayName = displayName;
		this.lobby = lobby;
		this.ticks = ticks;
		this.teams = teams;
	}

	public void reset() {
		// TODO
		throw new NotImplementedException();
	}

	public void load() {

	}

	public void end() {

	}

	public void sendToSpectate(Player p) {
		DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
		pd.setTeam(null);

		p.setGameMode(GameMode.SPECTATOR);

		// Teleport to lobby
		DTMMap currentMap = gmh.getCurrentMap();
		World currentWorld = gmh.getCurrentWorld();
		Location lobby = currentMap.getLobby().toLocation(currentWorld);
		p.teleport(lobby);

		if (p.getWorld() != currentWorld)
			return;

		if (lobby != null) {
			p.teleport(lobby);
		} else {
			System.err.println("Lobby null for map " + currentMap.getDisplayName());
			p.teleport(new Location(currentWorld, 0, 100, 0));
		}
		p.setGameMode(GameMode.SPECTATOR);
		p.getInventory().clear();

		// Handle appropriate nametag colours
		p.setDisplayName("§7" + p.getName());
		p.setPlayerListName("§8[" + ChatColor.translateAlternateColorCodes('&', pd.getPrefix()) + "§8] §7" + p
				.getName());
		p.setCustomName(pd.getTeam().getTeamColor() + p.getName());
		p.setCustomNameVisible(false);

	}

	public void sendPlayerToGame(Player p) {
		DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());

		// Reset properties and teleport to spawn
		p.setFallDistance(0);
		p.setHealthScale(20);
		p.setHealth(p.getHealthScale());
		p.setFoodLevel(20);
		p.teleport(pd.getTeam().getSpawn().toLocation(pl.getGameWorldHandler().getCurrentWorld()));
		p.setGameMode(GameMode.SURVIVAL);

		p.getInventory().setContents(pl.getGameWorldHandler().getCurrentMap().getKit());
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
			pl.getNameTagColorer().changeNameTag(p, pd.getTeam().getChatColor());
	}

}
