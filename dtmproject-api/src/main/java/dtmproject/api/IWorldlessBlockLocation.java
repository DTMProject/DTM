package dtmproject.api;

import org.bukkit.World;
import org.bukkit.block.Block;

public interface IWorldlessBlockLocation extends Cloneable {
    /**
     * Returns a bukkit block object at the located at this world.
     */
    public Block getBlock(World world);

    public int getX();

    public int getY();

    public int getZ();

    public IWorldlessLocation getLocation();
}