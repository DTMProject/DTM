package dtmproject.lang;

import lombok.Getter;
import lombok.SneakyThrows;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
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

    public String prefix() {
	return prefix;
    }

    @SneakyThrows
    public void saveTo(ConfigurationNode node) {
	MAPPER.bind(this).serialize(node);
    }

    @Setting
    @Getter
    private String prefix = "prefix";

    @Setting
    @Getter
    private String gameNotStarted = "§ePeli ei ole käynnissä.";

    @Setting
    @Getter
    private String joinWithCommand = "§eLiity peliin komennolla /liity.";

    @Setting
    @Getter
    private String invalidGamemode = "§eEt ole oikeassa pelitilassa.";

    @Setting
    @Getter
    private String DTMIsStopped = "§eDTM on pysäytetty!";

    @Setting
    @Getter
    private String DTMIsPausedTemporarilyPlayer = "§eDTM on pysäytetty väliaikaisesti. Kun peli jatkuu, sinut teleportataan spawnille.";

    @Setting
    @Getter
    private String DTMIsPausedTemporarily = "§eDTM on pysäytetty väliaikaisesti.";

    @Setting
    @Getter
    private String gameUnpausedPlayer = "§ePeli jatkuu! Sinut on teleportattu spawnille.";

    @Setting
    @Getter
    private String notEnoughPlayers = "§ePelissä ei ole tarpeeksi pelaajia.";

    @Setting
    @Getter
    private String gameStartsInSeconds = "§ePeli alkaa {0} sekunnissa.";

}
