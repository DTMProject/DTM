package dtmproject.common.data;

import org.bukkit.Material;
import org.bukkit.World;

import dtmproject.api.WorldlessBlockLocation;
import dtmproject.api.data.IDTMMonument;
import lombok.Getter;
import lombok.Setter;

public class DTMMonument implements IDTMMonument {
    @Getter
    @Setter
    private WorldlessBlockLocation block;

    @Getter
    @Setter
    private String position;

    @Getter
    @Setter
    private String customName;

    @Getter
    @Setter
    private boolean broken = false;

    public DTMMonument(WorldlessBlockLocation block, String position, String customName) {
	this.block = block;
	this.position = position;
	this.customName = customName;
    }

    /**
     * @param world is the world where the block is.
     **/
    public void repair(World world) {
	this.block.getBlock(world).setType(Material.OBSIDIAN);
	this.broken = false;
    }
}
