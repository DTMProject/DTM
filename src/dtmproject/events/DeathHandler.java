package dtmproject.events;

import java.util.HashMap;
import java.util.Optional;
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
import org.bukkit.scoreboard.Team;

import com.juubes.nexus.NexusLocation;

import dtmproject.DTM;
import dtmproject.playerdata.DTMPlayerData;
import dtmproject.setup.DTMTeam;
import dtmproject.setup.Monument;

public class DeathHandler implements Listener {
	private final DTM dtm;
	private static boolean broadcastMessages = true;

	public DeathHandler(DTM dtm) {
		this.dtm = dtm;
	}

	public void fakeKillPlayer(Player p) {
		// Drop inventory
		if (p.getGameMode().equals(GameMode.SURVIVAL))
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
		if (p.getGameMode().equals(GameMode.SURVIVAL))
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

		DTMPlayerData playerData = dtm.getDataHandler().getPlayerData(p.getUniqueId());

		// Reset
		dtm.getLogicHandler().sendPlayerToGame(p, playerData.team);
		p.setGameMode(GameMode.SPECTATOR);

		if (playerData.lastDamager.isPresent())
			p.teleport(playerData.lastDamager.get());

		// Respawn after 6 seconds
		Bukkit.getScheduler().runTaskLater(dtm, () -> {
			if (dtm.getLogicHandler().getGameState() != GameState.RUNNING)
				return;
			if (!p.isOnline())
				return;
			if (playerData.team == null)
				return;
			if (p.getGameMode() != GameMode.SPECTATOR)
				return;
			else {
				World world = Bukkit.getWorld(dtm.getGameWorldManager().getCurrentMapID());
				NexusLocation spawn = playerData.team.getSpawn();
				Location realSpawn = spawn.toLocation(world);
				p.teleport(realSpawn);
			}
			playerData.killStreak = 0;
			playerData.lastDamager = null;
			playerData.lastRespawn = System.currentTimeMillis();

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

		DTMPlayerData shooterData = dtm.getDataHandler().getPlayerData(shooter);
		DTMPlayerData targetData = dtm.getDataHandler().getPlayerData(target);

		// Shot one of their teammate -> cancel event
		if (shooterData.team == targetData.team) {
			// If someone shoots themselves just let it hit
			if (shooter != target) {
				e.setCancelled(true);
				return;
			}
		}

		if (shooter != target)
			targetData.lastDamager = Optional.of(shooter.getUniqueId());

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
				shooterData.emeralds = shooterData.emeralds + 1;
				shooter.sendMessage("§a+1 emerald");
			}

			// Update stats
			shooterData.seasonStats.get(dtm.getSeason()).kills++;
			targetData.seasonStats.get(dtm.getSeason()).deaths++;
		}
	}

	// Just a lazy command
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		if (e.getMessage().equals("/teams")) {
			e.setCancelled(true);
			e.getPlayer().sendMessage("§ePelaajamäärät:");
			for (Team team : dtm.getGameWorldHandler().getCurrentMap().getTeams())
				e.getPlayer().sendMessage(team.getDisplayName() + ": " + team.getPlayers().size());
		} else if (e.getMessage().equals("/restoremonuments") && e.getPlayer().isOp()) {
			e.setCancelled(true);
			for (Team team : dtm.getGameWorldHandler().getCurrentMap().getTeams()) {
				for (Monument mon : ((DTMTeam) team).getMonuments()) {
					mon.repair(dtm.getGameWorldHandler().getCurrentMap().getWorld());
				}
			}

			dtm.getScoreboardHandler().updateScoreboard();
		} else if (e.getMessage().equals("/ram") && e.getPlayer().isOp()) {
			e.setCancelled(true);
			e.getPlayer().sendMessage("§e" + ((int) ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime()
					.freeMemory()) / 1000000)) + "/" + (int) (Runtime.getRuntime().maxMemory() / 1000000));
		}
	}

	private HashMap<UUID, Long> lastHits = new HashMap<>();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSwordPVP(EntityDamageByEntityEvent e) {
		if (dtm.getLogicHandler().getGameState() != GameState.RUNNING) {
			e.setCancelled(true);
			return;
		}
		if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player))
			return;
		Player attacker = (Player) e.getDamager();
		Player target = (Player) e.getEntity();

		DTMPlayerData attackerData = dtm.getDataHandler().getPlayerData(attacker.getUniqueId());
		DTMPlayerData targetData = dtm.getDataHandler().getPlayerData(target.getUniqueId());

		if (lastHits.containsKey(attacker.getUniqueId())) {
			// 10 CPS limit lolzzzz
			if (lastHits.get(attacker.getUniqueId()) + 5E8 > System.nanoTime()) {
				e.setCancelled(true);
				return;
			}
		}

		lastHits.put(attacker.getUniqueId(), System.nanoTime());

		// Hit one of their teammate -> cancel event
		if (attackerData.team == targetData.team) {
			e.setCancelled(true);
			return;
		}

		if (System.currentTimeMillis() < targetData.lastRespawn + 2000) {
			attacker.playSound(target.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
			e.setCancelled(true);
			return;
		}

		targetData.lastDamager = Optional.of(attacker.getUniqueId());

		if (target.getHealth() - e.getFinalDamage() < 0) {
			if (broadcastMessages) {
				Bukkit.broadcastMessage(attackerData.nick + " §eteurasti pelaajan " + targetData.nick + "§e.");
			} else {
				target.sendMessage("§eSinut tappoi pelaaja " + attackerData.nick + "§e.");
				attacker.sendMessage("§eTapoit pelaajan " + targetData.nick + "§e.");
			}

			fakeKillPlayer(target);
			e.setCancelled(true);

			// Add point to killer
			attackerData.emeralds = attackerData.emeralds + 1;
			attacker.sendMessage("§a+1 emerald");

			// Update stats
			attackerData.seasonStats.get(dtm.getSeason()).kills++;
			targetData.seasonStats.get(dtm.getSeason()).deaths++;
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
		if (e.getTo().getY() < 0) {
			if (e.getPlayer().getGameMode() == GameMode.SURVIVAL)
				Bukkit.getPluginManager().callEvent(new EntityDamageEvent(e.getPlayer(), DamageCause.VOID, 100));
			else
				e.getPlayer().teleport(dtm.getGameWorldHandler().getCurrentMap().getLobby().toLocation(dtm
						.getLogicHandler().getCurrentMap().getWorld()));
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
			e.setDamage(0);
			fakeKillPlayer(target);
			DTMPlayerData targetData = dtm.getDataHandler().getPlayerData(target.getUniqueId());
			Optional<Player> damager = Optional.ofNullable(Bukkit.getPlayer(targetData.lastDamager.get()));
			switch (e.getCause()) {
			case VOID:
				if (damager.isPresent()) {
					DTMPlayerData damagerData = dtm.getDataHandler().getPlayerData(damager.get().getUniqueId());
					if (broadcastMessages)
						Bukkit.broadcastMessage(targetData.nick + "§e putosi maailmasta. " + damagerData.nick
								+ " §esai kunnian.");

					else {
						target.sendMessage("§eSinut tappoi pelaaja " + damagerData.nick + "§e.");
						damager.get().sendMessage("§eTapoit pelaajan " + targetData.nick + "§e.");
					}

					// TODO Pointless line?
					if (damagerData != null) {
						damagerData.emeralds = damagerData.emeralds + 1;

						damager.get().sendMessage("§a+1 emerald");
						// Update stats
						damagerData.seasonStats.get(dtm.getSeason()).kills++;
						targetData.seasonStats.get(dtm.getSeason()).deaths++;
					}
				} else {
					if (broadcastMessages)
						Bukkit.broadcastMessage(targetData.nick + " §eputosi maailmasta.");
				}
				break;
			case FALL:
				if (damager.isPresent()) {
					DTMPlayerData damagerData = dtm.getDataHandler().getPlayerData(damager.get().getUniqueId());
					if (broadcastMessages)
						Bukkit.broadcastMessage(targetData.nick + " §eosui maahan liian kovaa. " + damagerData.nick
								+ " §esai kunnian.");
					else {
						target.sendMessage("§eSinut tappoi pelaaja " + damagerData.nick);
						damager.get().sendMessage("§eTapoit pelaajan " + targetData.nick);
					}
					/* TODO: error */
					damagerData.emeralds = damagerData.emeralds + 1;
					damager.get().sendMessage("§a+1 emerald");
					// Update stats
					damagerData.seasonStats.get(dtm.getSeason()).kills++;
					targetData.seasonStats.get(dtm.getSeason()).deaths++;
				} else {
					if (broadcastMessages)
						Bukkit.broadcastMessage(targetData.nick + " §eosui maahan liian kovaa.");
				}
				break;
			case STARVATION:
				Bukkit.broadcastMessage(targetData.nick + " §eei viitsinyt syödä ja kuoli nälkään.");
				break;
			case ENTITY_EXPLOSION:
				if (damager.isPresent()) {
					DTMPlayerData damagerData = dtm.getDataHandler().getPlayerData(damager.get().getUniqueId());
					if (broadcastMessages)
						Bukkit.broadcastMessage(targetData.nick + " §eräjähti. " + damagerData.nick
								+ "§e sai kunnian.");
					else {
						target.sendMessage("§eSinut tappoi pelaaja " + damagerData.nick + "§e.");
						damager.sendMessage("§eTapoit pelaajan " + targetData.nick + "§e.");
					}
					damagerData.emeralds = damagerData.emeralds + 1;
					damager.get().sendMessage("§a+1 emerald");
					// Update stats
					damagerData.seasonStats.get(dtm.getSeason()).kills++;
					targetData.seasonStats.get(dtm.getSeason()).deaths++;
				} else {
					if (broadcastMessages)
						Bukkit.broadcastMessage(targetData.nick + " §eräjähti. ");
				}
				break;
			case DROWNING:
				if (broadcastMessages)
					Bukkit.broadcastMessage(targetData.nick + " §eyritti hengittää vettä.");
				break;
			case CONTACT:
				if (broadcastMessages)
					Bukkit.broadcastMessage(targetData.nick + " §ehalasi kaktusta ja kuoli.");
				break;
			case LAVA:
				if (broadcastMessages)
					Bukkit.broadcastMessage(targetData.nick + " §epaloi hengiltä.");
				break;
			case FIRE:
				if (broadcastMessages)
					Bukkit.broadcastMessage(targetData.nick + " §epaloi hengiltä.");
				break;
			case SUFFOCATION:
				if (broadcastMessages)
					Bukkit.broadcastMessage(target.getDisplayName() + "§e haisteli maata ja tukehtui palikkaan");
				break;
			case FIRE_TICK:
				if (damager != null) {
					DTMPlayerData damagerData = dtm.getDataHandler().getPlayerData(damager.get().getUniqueId());
					if (broadcastMessages)
						Bukkit.broadcastMessage(targetData.nick + " §epaloi hengiltä. " + damagerData.nick
								+ " §esai kunnian.");
					else {
						target.sendMessage("§eSinut tappoi pelaaja " + damagerData.nick + "§e.");
						damager.sendMessage("§eTapoit pelaajan " + damagerData.nick + "§e.");
					}

					damagerData.setEmeralds(damagerData.emeralds + 1);
					damager.sendMessage("§a+1 emerald");
					// Update stats
					damagerData.seasonStats.get(dtm.getSeason()).kills++;
					targetData.seasonStats.get(dtm.getSeason()).deaths++;
					break;
				} else {
					if (broadcastMessages)
						Bukkit.broadcastMessage(targetData.nick + " §epaloi elävältä.");
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
		if (Bukkit.getOnlinePlayers().size() > 14 + 1) {
			broadcastMessages = false;
		} else {
			broadcastMessages = true;
		}
	}

	public void clearLastHits(Player p) {
		lastHits.remove(p.getUniqueId());
	}
}
