package dtmproject.common.data;

import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import dtmproject.api.IWorldlessLocation;
import dtmproject.common.DTM;
import dtmproject.common.WorldlessLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

@Entity
@Table(name = "Teams")
@AllArgsConstructor
public class DTMTeam implements IDTMTeam<DTMMonument> {
    @Transient
    private final DTM pl;

    @Getter
    @Id
    @Column(name = "MapID", nullable = false)
    private final String Id;

    @Getter
    @Setter
    @Column(name = "DisplayName", nullable = false)
    private String displayName;

    @Getter
    @Setter
    @Column(name = "TeamColor", nullable = false)
    private ChatColor teamColor;

    @Getter
    @Column(name = "Spawn")
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
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
