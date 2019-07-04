package com.juubes.dtmproject.events;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
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

import com.juubes.dtmproject.DTM;
import com.juubes.dtmproject.playerdata.DTMPlayerData;
import com.juubes.dtmproject.setup.DTMTeam;
import com.juubes.dtmproject.setup.Monument;
import com.juubes.nexus.logic.GameState;
import com.juubes.nexus.logic.Team;

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
					continue;
				case DIAMOND_PICKAXE:
					continue;
				case BOW:
					continue;
				case LEATHER_HELMET:
					continue;
				case LEATHER_CHESTPLATE:
					continue;
				case LEATHER_LEGGINGS:
					continue;
				case LEATHER_BOOTS:
					continue;
				case IRON_AXE:
					continue;
				case ARROW:
					item.setAmount(item.getAmount() / 3);
					if (item.getType() != org.bukkit.Material.AIR)
						p.getWorld().dropItemNaturally(p.getLocation(), item);
					continue;
				case WOOD:
					item.setAmount(item.getAmount() / 3);
					if (item.getType() != org.bukkit.Material.AIR)
						p.getWorld().dropItemNaturally(p.getLocation(), item);
					continue;
				case COOKED_BEEF:
					item.setAmount(item.getAmount() / 5);
					if (item.getType() != org.bukkit.Material.AIR)
						p.getWorld().dropItemNaturally(p.getLocation(), item);
					continue;
				case ENDER_PEARL:
					item.setAmount(item.getAmount() / 3);
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
					continue;
				case LEATHER_CHESTPLATE:
					continue;
				case LEATHER_LEGGINGS:
					continue;
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

		DTMPlayerData playerData = dtm.getDatabaseManager().getPlayerData(p);

		// Reset
		dtm.getNexus().getGameLogic().sendPlayerToGame(p, playerData.getTeam());
		p.setGameMode(GameMode.SPECTATOR);

		if (playerData.getLastDamager() != null)
			p.teleport(playerData.getLastDamager());

		// Respawn after 6 seconds
		Bukkit.getScheduler().runTaskLater(dtm, () -> {
			if (dtm.getNexus().getGameLogic().getGameState() != GameState.RUNNING)
				return;
			if (!p.isOnline())
				return;
			if (playerData.getTeam() == null)
				return;
			if (p.getGameMode() != GameMode.SPECTATOR)
				return;
			else {
				p.teleport(playerData.getTeam().getSpawn());
			}
			// Reset killstreak
			playerData.setKillStreak(0);

			p.setGameMode(GameMode.SURVIVAL);
			playerData.setLastDamager(null);
			p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 8 * 20, 2));
			playerData.setLastRespawn(System.currentTimeMillis());
		}, 5 * 20);
	}

	@EventHandler
	public void onArmourDrop(PlayerDropItemEvent e) {
		switch (e.getItemDrop().getItemStack().getType()) {
		case LEATHER_HELMET:
			e.getItemDrop().remove();
			break;
		case LEATHER_CHESTPLATE:
			e.getItemDrop().remove();
			break;
		case LEATHER_LEGGINGS:
			e.getItemDrop().remove();
			break;
		case LEATHER_BOOTS:
			e.getItemDrop().remove();
			break;
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

		DTMPlayerData shooterData = dtm.getDatabaseManager().getPlayerData(shooter);
		DTMPlayerData targetData = dtm.getDatabaseManager().getPlayerData(target);

		// Shot one of their teammate -> cancel event
		if (shooterData.getTeam() == targetData.getTeam()) {
			// If someone shoots themselves just let it hit
			if (shooter != target) {
				e.setCancelled(true);
				return;
			}
		}

		if (shooter != target)
			targetData.setLastDamager(shooter);

		if (target.getHealth() - e.getFinalDamage() < 0) {
			if (Math.random() < 0.005)
				return;
			if (broadcastMessages)
				Bukkit.broadcastMessage(shooter.getCustomName() + " �eampui pelaajan " + target.getCustomName());
			else {
				target.sendMessage("�eSinut ampui pelaaja " + shooter.getDisplayName());
				shooter.sendMessage("�eAmmuit pelaajan " + target.getDisplayName());
			}
			fakeKillPlayer(target);
			e.setCancelled(true);

			// Add point to killer
			if (shooter != target) {
				shooterData.setEmeralds(shooterData.getEmeralds() + 1);
				shooter.sendMessage("�a+1 emerald");
			}

			// Update stats
			shooterData.getSeasonStats().kills++;
			targetData.getSeasonStats().deaths++;
		}
	}

	// Just a lazy command
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		if (e.getMessage().equals("/teams")) {
			e.setCancelled(true);
			e.getPlayer().sendMessage("�ePelaajam��r�t:");
			for (Team team : dtm.getNexus().getGameLogic().getCurrentGame().getTeams())
				e.getPlayer().sendMessage(team.getDisplayName() + ": " + team.getPlayers().size());
		} else if (e.getMessage().equals("/restoremonuments") && e.getPlayer().isOp()) {
			e.setCancelled(true);
			for (Team team : dtm.getNexus().getGameLogic().getCurrentGame().getTeams()) {
				for (Monument mon : ((DTMTeam) team).getMonuments()) {
					mon.repair();
				}
			}

			dtm.getScoreboardManager().updateScoreboard();
		} else if (e.getMessage().equals("/ram") && e.getPlayer().isOp()) {
			e.setCancelled(true);
			e.getPlayer().sendMessage("�e" + ((int) ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime()
					.freeMemory()) / 1000000)) + "/" + (int) (Runtime.getRuntime().maxMemory() / 1000000));
		}
	}

	private static HashMap<Player, Long> lastHits = new HashMap<>();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSwordPVP(EntityDamageByEntityEvent e) {
		if (dtm.getNexus().getGameLogic().getGameState() != GameState.RUNNING) {
			e.setCancelled(true);
			return;
		}
		if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player))
			return;
		Player attacker = (Player) e.getDamager();
		Player target = (Player) e.getEntity();

		DTMPlayerData attackerData = dtm.getDatabaseManager().getPlayerData(attacker);
		DTMPlayerData targetData = dtm.getDatabaseManager().getPlayerData(target);

		if (lastHits.containsKey(attacker)) {
			// 10 CPS limit lolzzzz
			if (lastHits.get(attacker) + 1E8 > System.nanoTime()) {
				e.setCancelled(true);
				return;
			}
		}

		lastHits.put(attacker, System.nanoTime());

		// Hit one of their teammate -> cancel event
		if (attackerData.getTeam() == targetData.getTeam())

		{
			e.setCancelled(true);
			return;
		}

		if (System.currentTimeMillis() < targetData.getLastRespawn() + 2000) {
			attacker.playSound(target.getLocation(), Sound.ITEM_BREAK, 1, 1);
			e.setCancelled(true);
			return;
		}

		targetData.setLastDamager(attacker);

		if (target.getHealth() - e.getFinalDamage() < 0) {
			if (broadcastMessages)
				Bukkit.broadcastMessage(attackerData.getNick() + " �eteurasti pelaajan " + targetData.getNick()
						+ "�e.");
			else {
				target.sendMessage("�eSinut tappoi pelaaja " + attackerData.getNick() + "�e.");
				attacker.sendMessage("�eTapoit pelaajan " + targetData.getNick() + "�e.");
			}

			fakeKillPlayer(target);
			e.setCancelled(true);

			// Add point to killer
			attackerData.setEmeralds(attackerData.getEmeralds() + 1);
			attacker.sendMessage("�a+1 emerald");

			// Update stats
			attackerData.getSeasonStats().kills++;
			targetData.getSeasonStats().deaths++;
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
						"�cTNT-Minecarttei ei voi laittaa en��, koska er��t nimelt� mainitsemattomat JEDI ja xVolt tuhosivat spawnin!");
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onMobSpawn(EntitySpawnEvent e) {
		if (e.getEntityType() == EntityType.DROPPED_ITEM)
			return;
		if (e.getEntityType() == EntityType.PRIMED_TNT)
			return;
		if (e.getEntityType() == EntityType.RABBIT)
			return;
		if (e.getEntityType() == EntityType.CHICKEN)
			return;
		if (e.getEntityType() == EntityType.COW)
			return;
		if (e.getEntityType() == EntityType.WITHER)
			return;
		e.setCancelled(true);
	}

	@EventHandler
	public void onVoid(PlayerMoveEvent e) {
		if (e.getTo().getY() < 0) {
			if (e.getPlayer().getGameMode() == GameMode.SURVIVAL)
				Bukkit.getPluginManager().callEvent(new EntityDamageEvent(e.getPlayer(), DamageCause.VOID, 100));
			else
				e.getPlayer().teleport(dtm.getNexus().getGameLogic().getCurrentGame().getLobby());
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
			Player damager = dtm.getDatabaseManager().getPlayerData(target).getLastDamager();
			DTMPlayerData targetData = dtm.getDatabaseManager().getPlayerData(target);
			if (e.getCause() == DamageCause.VOID) {
				if (damager != null) {
					DTMPlayerData damagerData = dtm.getDatabaseManager().getPlayerData(damager);
					if (broadcastMessages)
						Bukkit.broadcastMessage(targetData.getNick() + "�e putosi maailmasta. " + damagerData.getNick()
								+ " �esai kunnian.");
					else {
						target.sendMessage("�eSinut tappoi pelaaja " + damager.getDisplayName() + "�e.");
						damager.sendMessage("�eTapoit pelaajan " + target.getDisplayName() + "�e.");
					}

					if (damagerData != null) {
						damagerData.setEmeralds(damagerData.getEmeralds() + 1);

						damager.sendMessage("�a+1 emerald");
						// Update stats
						damagerData.getSeasonStats().kills++;
						targetData.getSeasonStats().deaths++;
					}
				} else {
					if (broadcastMessages)
						Bukkit.broadcastMessage(targetData.getNick() + " �eputosi maailmasta.");

				}
			} else if (e.getCause() == DamageCause.FALL) {
				if (damager != null) {
					DTMPlayerData damagerData = dtm.getDatabaseManager().getPlayerData(damager);
					if (broadcastMessages)
						Bukkit.broadcastMessage(targetData.getNick() + " �eosui maahan liian kovaa. " + damagerData
								.getNick() + " �esai kunnian.");
					else {
						target.sendMessage("�eSinut tappoi pelaaja " + damager.getDisplayName());
						damager.sendMessage("�eTapoit pelaajan " + target.getDisplayName());
					}
					/* TODO: error */ damagerData.setEmeralds(damagerData.getEmeralds() + 1);
					damager.sendMessage("�a+1 emerald");
					// Update stats
					damagerData.getSeasonStats().kills++;
					targetData.getSeasonStats().deaths++;
				} else {
					if (broadcastMessages)
						Bukkit.broadcastMessage(targetData.getNick() + " �eosui maahan liian kovaa.");
				}
			} else if (e.getCause() == DamageCause.STARVATION) {
				Bukkit.broadcastMessage(targetData.getNick() + " �eei viitsinyt sy�d� ja kuoli n�lk��n.");
			} else if (e.getCause() == DamageCause.ENTITY_EXPLOSION) {
				if (damager != null) {
					DTMPlayerData damagerData = dtm.getDatabaseManager().getPlayerData(damager);
					if (broadcastMessages)
						Bukkit.broadcastMessage(targetData.getNick() + " �er�j�hti. " + damagerData.getNick()
								+ "�e sai kunnian.");
					else {
						target.sendMessage("�eSinut tappoi pelaaja " + damager.getDisplayName() + "�e.");
						damager.sendMessage("�eTapoit pelaajan " + target.getDisplayName() + "�e.");
					}
					damagerData.setEmeralds(damagerData.getEmeralds() + 1);
					damager.sendMessage("�a+1 emerald");
					// Update stats
					damagerData.getSeasonStats().kills++;
					targetData.getSeasonStats().deaths++;
				} else {
					if (broadcastMessages)
						Bukkit.broadcastMessage(targetData.getNick() + " �er�j�hti. ");
				}
			} else if (e.getCause() == DamageCause.DROWNING) {
				if (broadcastMessages)
					Bukkit.broadcastMessage(targetData.getNick() + " �eyritti hengitt�� vett�.");
			} else if (e.getCause() == DamageCause.CONTACT) {
				if (broadcastMessages)
					Bukkit.broadcastMessage(targetData.getNick() + " �ehalasi kaktusta ja kuoli.");
			} else if (e.getCause() == DamageCause.LAVA) {
				if (broadcastMessages)
					Bukkit.broadcastMessage(targetData.getNick() + " �epaloi hengilt�.");
			} else if (e.getCause() == DamageCause.FIRE) {
				if (broadcastMessages)
					Bukkit.broadcastMessage(targetData.getNick() + " �epaloi hengilt�.");
			} else if (e.getCause() == DamageCause.SUFFOCATION) {
				if (broadcastMessages)
					Bukkit.broadcastMessage(target.getDisplayName() + "�e haisteli maata ja tukehtui palikkaan");
			} else if (e.getCause() == DamageCause.FIRE_TICK) {
				if (damager != null) {
					DTMPlayerData damagerData = dtm.getDatabaseManager().getPlayerData(damager);
					if (broadcastMessages)
						Bukkit.broadcastMessage(targetData.getNick() + " �epaloi hengilt�. " + damagerData.getNick()
								+ " �esai kunnian.");
					else {
						target.sendMessage("�eSinut tappoi pelaaja " + damager.getDisplayName() + "�e.");
						damager.sendMessage("�eTapoit pelaajan " + target.getDisplayName() + "�e.");
					}

					damagerData.setEmeralds(damagerData.getEmeralds() + 1);
					damager.sendMessage("�a+1 emerald");
					// Update stats
					damagerData.getSeasonStats().kills++;
					targetData.getSeasonStats().deaths++;
				} else {
					if (broadcastMessages)
						Bukkit.broadcastMessage(targetData.getNick() + " �epaloi hengilt�.");
				}
			} else {
				if (broadcastMessages)
					Bukkit.broadcastMessage(target.getDisplayName() + " �edied but we don't know why");
				System.out.println("The player died to " + e.getCause().name());
			}
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if (Bukkit.getOnlinePlayers().size() > 14) {
			broadcastMessages = false;
		} else {
			broadcastMessages = true;
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if (Bukkit.getOnlinePlayers().size() > 14 + 1) {
			broadcastMessages = false;
		} else {
			broadcastMessages = true;
		}
	}
}