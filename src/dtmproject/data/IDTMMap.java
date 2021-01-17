package dtmproject.data;

import java.util.LinkedHashSet;
import java.util.Optional;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import dtmproject.WorldlessLocation;

public interface IDTMMap<T extends IDTMTeam> {
	public String getId();

	public String getDisplayName();

	public void setDisplayName(String displayName);

	/**
	 * @implSpec returns Optional of the map lobby. If the lobby isn't set, returns
	 *           an empty Optional.
	 */
	public Optional<WorldlessLocation> getLobby();

	public void setLobby(Optional<WorldlessLocation> lobby);

	public int getTicks();

	public void setTicks(int ticks);

	public LinkedHashSet<? extends IDTMTeam> getTeams();

	public long getStartTime();

	public void setStartTime(long startTime);

	/**
	 * @return the time in milliseconds since the game started. Returns 0 if not yet
	 *         started.
	 */
	public long getTimePlayed();

	public ItemStack[] getKit();

	/**
	 * @implSpec array has to have 9 items.
	 */
	public void setKit(ItemStack[] kit);

	public World getWorld();

	public void setWorld(World world);

	/**
	 * @implSpec Makes the world ready to start. Loads the world associated with the
	 *           map.
	 *
	 * 
	 */
	public void load();

	/**
	 * @implSpec sends all joined players into game.
	 */
	public void startGame();

	public void end(T winner);

	public void unload();

	public void sendToSpectate(Player p);

	public void sendPlayerToGame(Player p);
}
