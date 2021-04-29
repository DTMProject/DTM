package dtmproject.common.data;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.bukkit.Material;
import org.bukkit.World;

import dtmproject.api.IWorldlessBlockLocation;
import dtmproject.common.WorldlessBlockLocation;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Monuments")
public class DTMMonument implements IDTMMonument {
    @Getter
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "")
    private WorldlessBlockLocation block;

    @Getter
    @Id
    @Column(name = "MapID", nullable = false)
    private DTMMap map;

    @Getter
    @Id
    @Column(name = "TeamID", nullable = false)
    private DTMTeam team;

    @Getter
    @Setter
    @Id
    @Column(name = "Position", nullable = false, length = 5)
    private String position;

    @Getter
    @Setter
    @Column(name = "CustomName", length = 5)
    private String customName;

    @Getter
    @Setter
    @Transient
    private boolean broken = false;

    public DTMMonument(WorldlessBlockLocation block, String position, String customName) {
	this.block = (WorldlessBlockLocation) block;
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

    @Override
    public void setBlock(IWorldlessBlockLocation block) {
	this.block = (WorldlessBlockLocation) block;
    }

    public void setMap(DTMMap map) {
	this.map = map;
    }

    public void setTeam(DTMTeam team) {
	this.team = team;
    }
}
