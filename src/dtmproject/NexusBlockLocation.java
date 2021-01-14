package dtmproject;

import org.bukkit.World;
import org.bukkit.block.Block;

public class NexusBlockLocation implements Cloneable {
	private WorldlessLocation loc;

	public NexusBlockLocation(int x, int y, int z) {
		this.loc = new WorldlessLocation(x, y, z, 0, 0);
	}

	public NexusBlockLocation(WorldlessLocation loc) {
		this.loc = loc;
	}

	public NexusBlockLocation(Block block) {
		this.loc = new WorldlessLocation(block.getLocation());
	}

	/**
	 * Returns a bukkit block object at the located at this world.
	 */
	public Block getBlock(World world) {
		return world.getBlockAt(loc.toLocation(world));
	}

	public int getX() {
		return (int) loc.getX();
	}

	public int getY() {
		return (int) loc.getY();
	}

	public int getZ() {
		return (int) loc.getZ();
	}

	public WorldlessLocation getLocation() {
		return loc;
	}

	@Override
	public NexusBlockLocation clone() throws CloneNotSupportedException {
		return new NexusBlockLocation(getX(), getY(), getZ());
	}
}
