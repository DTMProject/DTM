package dtmproject.common.data;

import org.bukkit.World;

import dtmproject.api.IWorldlessBlockLocation;

public interface IDTMMonument {
    /**
     * The pretty emoji thingy for monuments.
     */
    public static char CHAR = (char) 0x1f844;

    public IWorldlessBlockLocation getBlock();

    public void setBlock(IWorldlessBlockLocation block);

    public String getPosition();

    public void setPosition(String position);

    public String getCustomName();

    public void setCustomName(String customName);

    public boolean isBroken();

    public void setBroken(boolean broken);

    public void repair(World world);

}
