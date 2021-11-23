package dtmproject.api.data;

import org.bukkit.World;

import dtmproject.api.WorldlessBlockLocation;

public interface IDTMMonument {
    /**
     * The pretty emoji thingy for monuments.
     */
    public static char CHAR = (char) 0x1f844;

    public WorldlessBlockLocation getBlock();

    public void setBlock(WorldlessBlockLocation block);

    public String getPosition();

    public void setPosition(String position);

    public String getCustomName();

    public void setCustomName(String customName);

    public boolean isBroken();

    public void setBroken(boolean broken);

    public void repair(World world);

}
