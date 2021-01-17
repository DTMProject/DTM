package dtmproject.data;

import org.bukkit.Material;
import org.bukkit.World;

import dtmproject.NexusBlockLocation;
import lombok.Getter;
import lombok.Setter;

public class DTMMonument {
	@Getter
	@Setter
	private NexusBlockLocation block;

	@Getter
	@Setter
	private String position;

	@Getter
	@Setter
	private String customName;

	@Getter
	@Setter
	private boolean broken = false;

	public static char getSymbol() {
		return (char) 0x1f844;
	}

	public DTMMonument(NexusBlockLocation block, String position, String customName) {
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
