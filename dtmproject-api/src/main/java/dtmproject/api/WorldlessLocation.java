package dtmproject.api;

import org.bukkit.Location;
import org.bukkit.World;

import lombok.Getter;
import lombok.Setter;

/**
 * A class to replace org.bukkit.Location in some cases, where a reference to a
 * World is not necessary.
 */
public class WorldlessLocation implements Cloneable {
    @Getter
    @Setter
    private double x;

    @Getter
    @Setter
    private double y;

    @Getter
    @Setter
    private double z;

    @Getter
    @Setter
    private float pitch;

    @Getter
    @Setter
    private float yaw;

    public WorldlessLocation(double x, double y, double z, float yaw, float pitch) {
	this.x = x;
	this.y = y;
	this.z = z;
	this.yaw = yaw;
	this.pitch = pitch;
    }

    public WorldlessLocation(Location location) {
	this.x = location.getX();
	this.y = location.getY();
	this.z = location.getZ();
	this.yaw = location.getYaw();
	this.pitch = location.getPitch();
    }

    public WorldlessLocation(int x, int y, int z) {
	this(x, y, z, 0, 0);
    }

    public Location toLocation(World world) {
	if (world == null)
	    throw new NullPointerException();
	return new Location(world, x, y, z, yaw, pitch);
    }

    public WorldlessBlockLocation getBlock() {
	return new WorldlessBlockLocation((int) x, (int) y, (int) z);
    }

    @Override
    public Object clone() {
	return new WorldlessLocation(x, y, z, yaw, pitch);
    }
}
