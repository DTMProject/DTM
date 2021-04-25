package dtmproject.api;

import java.io.Serializable;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * A class to replace org.bukkit.Location in some cases, where a reference to a
 * World is not necessary.
 */
public interface IWorldlessLocation extends Cloneable, Serializable {
    public double getX();

    public void setX(double x);

    public double getY();

    public void setY(double y);

    public double getZ();

    public void setZ(double z);

    public float getPitch();

    public void setPitch(float pitch);

    public float getYaw();

    public void setYaw(float yaw);

    public Location toLocation(World world);

}
