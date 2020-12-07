package com.juubes.dtmproject.setup.DTMGameLogic;

import org.bukkit.Bukkit;

import com.juubes.nexus.Nexus;
import com.juubes.nexus.logic.AbstractLogicHandler;

public class DTMGameLogic extends AbstractLogicHandler {
	public DTMGameLogic() {
		// Can't just pass the reference in the constructor god dammit.
		super((Nexus) Bukkit.getPluginManager().getPlugin("DTM"));
	}

}
