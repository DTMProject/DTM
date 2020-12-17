package dtmproject.events;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

import dtmproject.DTM;
import dtmproject.data.DTMPlayerData;
import dtmproject.setup.DTMTeam;

public class InstakillTNTHandler implements Listener {

	private final DTM dtm;

	/** TNT, Player */
	private final HashMap<UUID, UUID> tnts = new HashMap<>();

	public InstakillTNTHandler(DTM dtm) {
		this.dtm = dtm;
	}

	@EventHandler
	public void setTNT(BlockPlaceEvent e) {
		Block b = e.getBlock();
		if (b.getType() != Material.TNT)
			return;

		DTMPlayerData pd = dtm.getDataHandler().getPlayerData(e.getPlayer());

		DTMTeam team = pd.getTeam();
		if (team == null) {
			e.setCancelled(true);
			return;
		}

		b.setType(Material.AIR);

		Location tntLocation = b.getLocation().clone();
		tntLocation.add(new Vector(0.5, 0.5, 0.5));

		World world = dtm.getMapHandler().getCurrentWorld();
		TNTPrimed tnt = (TNTPrimed) world.spawnEntity(b.getLocation(), EntityType.PRIMED_TNT);
		tnt.setFuseTicks(80);
		tnts.put(tnt.getUniqueId(), e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void entityExplodeEvent(EntityExplodeEvent e) {
		if (e.getEntityType() != EntityType.PRIMED_TNT)
			return;

		TNTPrimed tnt = (TNTPrimed) e.getEntity();

		e.setCancelled(true);
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.playSound(tnt.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
		}

		if (!tnts.containsKey(tnt.getUniqueId()))
			return;

		int killedTeammates = 0;
		UUID exploderUUID = tnts.get(tnt.getUniqueId());
		DTMPlayerData exploderData = dtm.getDataHandler().getPlayerData(exploderUUID);
		// Instakill 10 block radius
		for (Player target : Bukkit.getOnlinePlayers()) {
			if (target.getUniqueId() == exploderUUID)
				continue;
			if (target.getLocation().distance(tnt.getLocation()) > 7)
				continue;

			DTMPlayerData targetData = dtm.getDataHandler().getPlayerData(target.getUniqueId());
			if (targetData == null)
				continue;

			if (targetData.getTeam() == null)
				continue;

			// If kills own teammate, kills themselves
			if (targetData.getTeam() == exploderData.getTeam()) {
				killedTeammates++;
				target.damage(10);
				Bukkit.broadcastMessage(exploderData.getLastSeenName() + " räjäytti oman " + target.getName());
			} else {
				// Leave 0.5hp from full
				target.damage(200);
				Bukkit.broadcastMessage(exploderData.getLastSeenName() + " räjäytti pelaajan " + target.getName());
			}
		}

		if (killedTeammates > 0) {
			Player p = Bukkit.getPlayer(exploderUUID);
			if (p != null) {
				if (exploderData.getTeam() != null) {
					p.damage(10 * killedTeammates);
					p.sendMessage("§eTapoit oman tiimiläisesi, joten otat itsekin vahinkoa!");
				}
			}
		}

		tnts.remove(tnt.getUniqueId());
	}

}
