package dtmproject.setup;

import org.bukkit.Material;
import org.bukkit.World;

import dtmproject.NexusBlockLocation;

public class Monument {

	public NexusBlockLocation block;
	public String position;
	public String customName;

	public boolean broken = false;

	public static char getSymbol() {
		return (char) 0x1f844;
	}

	public Monument(NexusBlockLocation block, String position, String customName) {
		this.block = block;
		this.position = position;
		this.customName = customName;
	}

	/**
	 * @param world
	 *            is the world where the block is.
	 **/
	public void repair(World world) {
		this.block.getBlock(world).setType(Material.OBSIDIAN);
		this.broken = false;
	}
}
