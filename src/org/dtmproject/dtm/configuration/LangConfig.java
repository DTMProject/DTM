package org.dtmproject.dtm.configuration;

import lombok.SneakyThrows;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class LangConfig {

    private static final ObjectMapper<LangConfig> MAPPER;

    static {
        try {
            MAPPER = ObjectMapper.forClass(LangConfig.class); // We hold on to the instance of our ObjectMapper
        } catch (ObjectMappingException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static LangConfig loadFrom(ConfigurationNode node) throws ObjectMappingException {
        return MAPPER.bindToNew().populate(node);
    }

	private String prefix = "prefix";

	public String prefix() {
		return prefix;
	}

    @SneakyThrows
    public void saveTo(ConfigurationNode node) {
        MAPPER.bind(this).serialize(node);
    }

}
