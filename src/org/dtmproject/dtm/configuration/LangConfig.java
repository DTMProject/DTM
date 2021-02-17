package org.dtmproject.dtm.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class LangConfig {

	private String prefix = "prefix";

	public String prefix() {
		return prefix;
	}
}
