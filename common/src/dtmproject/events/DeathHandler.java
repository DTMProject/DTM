package dtmproject.events;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import dtmproject.DTM;
import dtmproject.WorldlessLocation;
import dtmproject.data.DTMMap;
import dtmproject.data.DTMPlayerData;
import dtmproject.logic.GameState;

public class DeathHandler implements Listener {
    private final DTM pl;
    private final HashMap<UUID, Long> lastHits = new HashMap<>();

    private boolean broadcastMessages = true;

    public DeathHandler(DTM pl) {
	this.pl = pl;
    }

    public void fakeKillPlayer(Player p) {
	// Drop inventory
	if (p.getGameMode().equals(GameMode.SURVIVAL)) {
	    for (ItemStack item : p.getInventory().getContents()) {
		if (item == null)
		    continue;
		switch (item.getType()) {
		case STONE_SWORD:
		case DIAMOND_PICKAXE:
		case BOW:
		case LEATHER_HELMET:
		case LEATHER_CHESTPLATE:
		case LEATHER_LEGGINGS:
		case LEATHER_BOOTS:
		case IRON_AXE:
		    continue;
		case ARROW:
		case WOOD:
		case ENDER_PEARL:
		    item.setAmount(item.getAmount() / 3);
		    if (item.getType() != org.bukkit.Material.AIR)
			p.getWorld().dropItemNaturally(p.getLocation(), item);
		    continue;
		case COOKED_BEEF:
		    item.setAmount(item.getAmount() / 5);
		    if (item.getType() != org.bukkit.Material.AIR)
			p.getWorld().dropItemNaturally(p.getLocation(), item);
		    continue;
		default:
		    p.getWorld().dropItemNaturally(p.getLocation(), item);
		}
	    }

	    // Drop armour if not leather
	    for (ItemStack item : p.getInventory().getArmorContents()) {
		if (item == null)
		    continue;
		switch (item.getType()) {
		case LEATHER_HELMET:
		case LEATHER_CHESTPLATE:
		case LEATHER_LEGGINGS:
		case LEATHER_BOOTS:
		    continue;
		default:
		    if (item.getType() != org.bukkit.Material.AIR)
			p.getWorld().dropItemNaturally(p.getLocation(), item);
		}
	    }
	}

	// Dupe bug fix
	p.getOpenInventory().setCursor(null);

	// Dupe bug fix 2
	try {
	    // Haven't tested this so I'm just guna be sure...
	    p.getOpenInventory().setItem(1, null);
	    p.getOpenInventory().setItem(2, null);
	    p.getOpenInventory().setItem(3, null);
	    p.getOpenInventory().setItem(4, null);
	} catch (Exception e) {
	    e.printStackTrace();
	}

	DTMPlayerData playerData = pl.getDataHandler().getPlayerData(p.getUniqueId());

	// Reset
	pl.getLogicHandler().getCurrentMap().sendPlayerToGame(p);
	p.setGameMode(GameMode.SPECTATOR);

	if (playerData.getLastDamager() != null)
	    p.teleport(playerData.getLastDamager());

	// Respawn after 6 seconds
	Bukkit.getScheduler().runTaskLater(pl, () -> {
	    if (pl.getLogicHandler().getGameState() != GameState.RUNNING)
		return;
	    if (!p.isOnline())
		return;
	    if (playerData.isSpectator())
		return;
	    if (p.getGameMode() != GameMode.SPECTATOR)
		return;
	    else {
		World world = Bukkit.getWorld(pl.getLogicHandler().getCurrentMap().getId());
		WorldlessLocation spawn = playerData.getTeam().getSpawn();
		Location realSpawn = spawn.toLocation(world);
		p.teleport(realSpawn);
	    }
	    playerData.increaseKillStreak();
	    playerData.setLastDamager(null);
	    playerData.setLastRespawn(System.currentTimeMillis());

	    p.setGameMode(GameMode.SURVIVAL);

	    p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 8 * 20, 2));
	}, 5 * 20);
    }

    @EventHandler
    public void onArmourDrop(PlayerDropItemEvent e) {
	switch (e.getItemDrop().getItemStack().getType()) {
	case LEATHER_HELMET:
	case LEATHER_CHESTPLATE:
	case LEATHER_LEGGINGS:
	case LEATHER_BOOTS:
	    e.getItemDrop().remove();
	default:
	    break;
	}
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArrowHit(EntityDamageByEntityEvent e) {
	if (!(e.getDamager() instanceof Arrow) || !(e.getEntity() instanceof Player))
	    return;
	Arrow arrow = (Arrow) e.getDamager();
	if (!(arrow.getShooter() instanceof Player))
	    return;
	Player shooter = (Player) arrow.getShooter();
	Player target = (Player) e.getEntity();

	DTMPlayerData shooterData = pl.getDataHandler().getPlayerData(shooter);
	DTMPlayerData targetData = pl.getDataHandler().getPlayerData(target);

	// Shot one of their teammate -> cancel event
	if (shooterData.getTeam() == targetData.getTeam()) {
	    // If someone shoots themselves just let it hit
	    if (shooter != target) {
		e.setCancelled(true);
		return;
	    }
	}

	if (shooter != target)
	    targetData.setLastDamager(shooter.getUniqueId());

	if (target.getHealth() - e.getFinalDamage() < 0) {
	    if (Math.random() < 0.005)
		return;
	    if (broadcastMessages)
		Bukkit.broadcastMessage(shooter.getCustomName() + " §eampui pelaajan " + target.getCustomName());
	    else {
		target.sendMessage("§eSinut ampui pelaaja " + shooter.getDisplayName());
		shooter.sendMessage("§eAmmuit pelaajan " + target.getDisplayName());
	    }
	    fakeKillPlayer(target);
	    e.setCancelled(true);

	    // Add point to killer
	    if (shooter != target) {
		shooterData.increaseEmeralds();
		shooter.sendMessage("§a+1 emerald");
	    }

	    // Update stats
	    shooterData.getSeasonStats().increaseKills();
	    targetData.getSeasonStats().increaseDeaths();
	}
    }

    // Lazy commands
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
	Player p = e.getPlayer();
	if (e.getMessage().equals("/restoremonuments") && p.isOp()) {
	    e.setCancelled(true);

	    // Repair monuments
	    pl.getLogicHandler().getCurrentMap().getTeams().forEach(team -> team.getMonuments()
		    .forEach(mon -> mon.repair(pl.getLogicHandler().getCurrentMap().getWorld())));
	    pl.getScoreboardHandler().updateScoreboard();
	} else if (e.getMessage().equals("/ram") && e.getPlayer().isOp()) {
	    e.setCancelled(true);
	    p.sendMessage(
		    "§e" + ((int) ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()) / 1000000))
			    + "/" + (int) (Runtime.getRuntime().maxMemory() / 1000000));
	}
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSwordPVP(EntityDamageByEntityEvent e) {
	if (pl.getLogicHandler().getGameState() != GameState.RUNNING) {
	    e.setCancelled(true);
	    return;
	}
	if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player))
	    return;
	Player attacker = (Player) e.getDamager();
	Player target = (Player) e.getEntity();

	DTMPlayerData attackerData = pl.getDataHandler().getPlayerData(attacker.getUniqueId());
	DTMPlayerData targetData = pl.getDataHandler().getPlayerData(target.getUniqueId());

	if (lastHits.containsKey(attacker.getUniqueId())) {
	    // 10 CPS limit lolzzzz
	    if (lastHits.get(attacker.getUniqueId()) + 5E8 > System.nanoTime()) {
		e.setCancelled(true);
		return;
	    }
	}

	lastHits.put(attacker.getUniqueId(), System.nanoTime());

	// Hit one of their teammate -> cancel event
	if (attackerData.getTeam() == targetData.getTeam()) {
	    e.setCancelled(true);
	    return;
	}

	if (System.currentTimeMillis() < targetData.getLastRespawn() + 2000) {
	    attacker.playSound(target.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
	    e.setCancelled(true);
	    return;
	}

	targetData.setLastDamager(attacker.getUniqueId());

	if (target.getHealth() - e.getFinalDamage() < 0) {
	    if (broadcastMessages) {
		Bukkit.broadcastMessage(
			attackerData.getDisplayName() + " §eteurasti pelaajan " + targetData.getDisplayName() + "§e.");
	    } else {
		target.sendMessage("§eSinut tappoi pelaaja " + attackerData.getDisplayName() + "§e.");
		attacker.sendMessage("§eTapoit pelaajan " + targetData.getDisplayName() + "§e.");
	    }

	    fakeKillPlayer(target);
	    e.setCancelled(true);

	    // Add point to killer
	    attackerData.increaseEmeralds();
	    attacker.sendMessage("§a+1 emerald");

	    // Update stats
	    attackerData.getSeasonStats().increaseKills();
	    targetData.getSeasonStats().increaseDeaths();
	    // TODO: put emeralds addings to the increasestat methods ^
	}
    }

    @EventHandler
    public void onMinecart(PlayerInteractEvent e) {
	if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
	    return;
	if (e.getItem() == null || e.getItem().getType().equals(Material.AIR))
	    return;
	if (e.getItem().getType().equals(Material.EXPLOSIVE_MINECART)) {
	    if (Math.random() < 0.1)
		e.getPlayer().sendMessage(
			"§cTNT-Minecarttei ei voi laittaa enää, koska eräät nimeltä mainitsemattomat JEDI ja xVolt tuhosivat spawnin!");
	    e.setCancelled(true);
	}
    }

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent e) {
	switch (e.getEntityType()) {
	case DROPPED_ITEM:
	case PRIMED_TNT:
	case RABBIT:
	case CHICKEN:
	case COW:
	case WITHER:
	    return;
	default:
	    break;
	}
	e.setCancelled(true);
    }

    @EventHandler
    public void onVoid(PlayerMoveEvent e) {
	Player p = e.getPlayer();
	if (e.getTo().getY() < 0) {
	    if (p.getGameMode() == GameMode.SURVIVAL)
		Bukkit.getPluginManager().callEvent(new EntityDamageEvent(e.getPlayer(), DamageCause.VOID, 100));
	    else {
		DTMMap currentMap = pl.getLogicHandler().getCurrentMap();
		World gameWorld = currentMap.getWorld();
		p.teleport(currentMap.getLobby().orElse(new WorldlessLocation(0, 100, 0)).toLocation(gameWorld));
	    }
	}
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOtherDeath(EntityDamageEvent e) {
	if (e.getEntityType() != EntityType.PLAYER)
	    return;

	// Overrides the other events without this
	if (e.getCause() == DamageCause.ENTITY_ATTACK)
	    return;
	if (e.getCause() == DamageCause.PROJECTILE)
	    return;

	Player target = (Player) e.getEntity();
	if (target.getHealth() - e.getFinalDamage() <= 0) {
	    DTMPlayerData targetData = pl.getDataHandler().getPlayerData(target.getUniqueId());
	    if (targetData.isSpectator()) {
		pl.getLogicHandler().getCurrentMap().sendToSpectate(target);
	    }

	    e.setDamage(0);
	    fakeKillPlayer(target);

	    Player damager = targetData.getLastDamager();
	    switch (e.getCause()) {
	    case VOID:
		if (damager != null) {
		    DTMPlayerData damagerData = pl.getDataHandler().getPlayerData(damager.getUniqueId());
		    if (broadcastMessages)
			Bukkit.broadcastMessage(targetData.getDisplayName() + "§e putosi maailmasta. "
				+ damagerData.getDisplayName() + " §esai kunnian.");

		    else {
			target.sendMessage("§eSinut tappoi pelaaja " + damagerData.getDisplayName() + "§e.");
			damager.sendMessage("§eTapoit pelaajan " + targetData.getDisplayName() + "§e.");
		    }

		    // TODO Pointless line?
		    if (damagerData != null) {
			damagerData.increaseEmeralds();
			damager.sendMessage("§a+1 emerald");
			// Update stats
			damagerData.getSeasonStats().increaseKills();
			targetData.getSeasonStats().increaseDeaths();
		    }
		} else {
		    if (broadcastMessages)
			Bukkit.broadcastMessage(targetData.getDisplayName() + " §eputosi maailmasta.");
		}
		break;
	    case FALL:
		if (damager != null) {
		    DTMPlayerData damagerData = pl.getDataHandler().getPlayerData(damager.getUniqueId());
		    if (broadcastMessages)
			Bukkit.broadcastMessage(targetData.getDisplayName() + " §eosui maahan liian kovaa. "
				+ damagerData.getDisplayName() + " §esai kunnian.");
		    else {
			target.sendMessage("§eSinut tappoi pelaaja " + damagerData.getDisplayName());
			damager.sendMessage("§eTapoit pelaajan " + targetData.getDisplayName());
		    }
		    /* TODO: error */
		    damagerData.increaseEmeralds();
		    damager.sendMessage("§a+1 emerald");
		    // Update stats
		    damagerData.getSeasonStats().increaseKills();
		    targetData.getSeasonStats().increaseDeaths();
		} else {
		    if (broadcastMessages)
			Bukkit.broadcastMessage(targetData.getDisplayName() + " §eosui maahan liian kovaa.");
		}
		break;
	    case STARVATION:
		Bukkit.broadcastMessage(targetData.getDisplayName() + " §eei viitsinyt syödä ja kuoli nälkään.");
		break;
	    case ENTITY_EXPLOSION:
		if (damager != null) {
		    DTMPlayerData damagerData = pl.getDataHandler().getPlayerData(damager.getUniqueId());
		    if (broadcastMessages)
			Bukkit.broadcastMessage(targetData.getDisplayName() + " §eräjähti. "
				+ damagerData.getDisplayName() + "§e sai kunnian.");
		    else {
			target.sendMessage("§eSinut tappoi pelaaja " + damagerData.getDisplayName() + "§e.");
			damager.sendMessage("§eTapoit pelaajan " + targetData.getDisplayName() + "§e.");
		    }
		    damagerData.increaseEmeralds();
		    damager.sendMessage("§a+1 emerald");
		    // Update stats
		    damagerData.getSeasonStats().increaseKills();
		    targetData.getSeasonStats().increaseDeaths();
		} else {
		    if (broadcastMessages)
			Bukkit.broadcastMessage(targetData.getDisplayName() + " §eräjähti. ");
		}
		break;
	    case DROWNING:
		if (broadcastMessages)
		    Bukkit.broadcastMessage(targetData.getDisplayName() + " §eyritti hengittää vettä.");
		break;
	    case CONTACT:
		if (broadcastMessages)
		    Bukkit.broadcastMessage(targetData.getDisplayName() + " §ehalasi kaktusta ja kuoli.");
		break;
	    case LAVA:
		if (broadcastMessages)
		    Bukkit.broadcastMessage(targetData.getDisplayName() + " §epaloi hengiltä.");
		break;
	    case FIRE:
		if (broadcastMessages)
		    Bukkit.broadcastMessage(targetData.getDisplayName() + " §epaloi hengiltä.");
		break;
	    case SUFFOCATION:
		if (broadcastMessages)
		    Bukkit.broadcastMessage(target.getDisplayName() + "§e haisteli maata ja tukehtui palikkaan");
		break;
	    case FIRE_TICK:
		if (damager != null) {
		    DTMPlayerData damagerData = pl.getDataHandler().getPlayerData(damager.getUniqueId());
		    if (broadcastMessages)
			Bukkit.broadcastMessage(targetData.getDisplayName() + " §epaloi hengiltä. "
				+ damagerData.getDisplayName() + " §esai kunnian.");
		    else {
			target.sendMessage("§eSinut tappoi pelaaja " + damagerData.getDisplayName() + "§e.");
			damager.sendMessage("§eTapoit pelaajan " + damagerData.getDisplayName() + "§e.");
		    }

		    damagerData.increaseEmeralds();
		    damager.sendMessage("§a+1 emerald");
		    // Update stats
		    damagerData.getSeasonStats().increaseKills();
		    targetData.getSeasonStats().increaseDeaths();
		    break;
		} else {
		    if (broadcastMessages)
			Bukkit.broadcastMessage(targetData.getDisplayName() + " §epaloi elävältä.");
		}
		break;
	    default:
		if (broadcastMessages)
		    Bukkit.broadcastMessage(target.getDisplayName() + " §edied but we don't know why");
		System.out.println("The player died to " + e.getCause().name());
		break;
	    }
	}
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
	// Broadcast if less than 15 players
	broadcastMessages = Bukkit.getOnlinePlayers().size() < 15;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
	// Broadcast if less than 15 players
	broadcastMessages = Bukkit.getOnlinePlayers().size() < 15;
    }

    public void clearLastHits(Player p) {
	lastHits.remove(p.getUniqueId());
    }
}
