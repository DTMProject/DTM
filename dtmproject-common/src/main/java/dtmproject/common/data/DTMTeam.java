package dtmproject.common.data;

import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import dtmproject.api.IWorldlessLocation;
import dtmproject.common.DTM;
import dtmproject.common.WorldlessLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

@DatabaseTable(tableName = "Teams")
@AllArgsConstructor
public class DTMTeam implements IDTMTeam<DTMMonument> {
    private final DTM pl;

    @Getter
    @DatabaseField(id = true, columnName = "MapID", canBeNull = false)
    private final String Id;

    @Getter
    @Setter
    @DatabaseField(columnName = "DisplayName", canBeNull = false)
    private String displayName;

    @Getter
    @Setter
    private ChatColor teamColor;

    @Getter
    private WorldlessLocation spawn;

    @Getter
    @Setter
    private LinkedList<DTMMonument> monuments;

    public Set<Player> getPlayers() {
	return Bukkit.getOnlinePlayers().stream().filter(p -> pl.getDataHandler().getPlayerData(p).getTeam() == this)
		.collect(Collectors.toSet());
    }

    public Color getLeatherColor() {
	switch (this.teamColor) {
	case AQUA:
	case DARK_AQUA:
	    return Color.AQUA;
	case BLACK:
	    return Color.BLACK;
	case BLUE:
	    return Color.BLUE;
	case DARK_BLUE:
	    return Color.NAVY;
	case DARK_GRAY:
	case GRAY:
	    return Color.GRAY;
	case DARK_GREEN:
	    return Color.OLIVE;
	case DARK_PURPLE:
	case LIGHT_PURPLE:
	    return Color.PURPLE;
	case DARK_RED:
	    return Color.MAROON;
	case GOLD:
	case YELLOW:
	    return Color.YELLOW;
	case GREEN:
	    return Color.LIME;
	case RED:
	    return Color.RED;
	case WHITE:
	    return Color.WHITE;
	default:
	    break;
	}
	return null;
    }

    @Override
    public void setSpawn(IWorldlessLocation spawn) {
	this.spawn = (WorldlessLocation) spawn;
    }

}
