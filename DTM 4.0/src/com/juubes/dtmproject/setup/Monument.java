package com.juubes.dtmproject.setup;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class Monument {

	public Block block;
	public String position;
	public String customName;

	public boolean broken = false;

	public static char getSymbol() {
		return (char) 0x1f844;
	}

	public Monument(Block block, String position, String customName) {
		this.block = block;
		this.position = position;
		this.customName = customName;
	}

	public void repair() {
		this.block.setType(Material.OBSIDIAN);
		this.broken = false;
	}
}
