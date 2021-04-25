package dtmproject.common;

import org.bukkit.World;
import org.bukkit.block.Block;

import dtmproject.api.IWorldlessBlockLocation;
import dtmproject.api.IWorldlessLocation;

public class WorldlessBlockLocation implements IWorldlessBlockLocation {
    private WorldlessLocation loc;

    public WorldlessBlockLocation(int x, int y, int z) {
	this.loc = new WorldlessLocation(x, y, z, 0, 0);
    }

    public WorldlessBlockLocation(WorldlessLocation loc) {
	this.loc = loc;
    }

    public WorldlessBlockLocation(Block block) {
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

    public IWorldlessLocation getLocation() {
	return loc;
    }

    @Override
    public WorldlessBlockLocation clone() {
	return new WorldlessBlockLocation(getX(), getY(), getZ());
    }
}
