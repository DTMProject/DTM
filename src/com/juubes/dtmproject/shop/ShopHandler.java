package com.juubes.dtmproject.shop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.juubes.dtmproject.DTM;
import com.juubes.dtmproject.playerdata.DTMPlayerData;

public class ShopHandler implements Listener {
	private final DTM pl;
	private final Inventory shopInventory;
	private final ShopItem[] itemsInShop;

	public ShopHandler(DTM pl) {
		this.pl = pl;
		this.shopInventory = Bukkit.createInventory(null, 27, "§eKauppa");

		this.itemsInShop = new ShopItem[27];
		int inventoryIndex = 0;

		itemsInShop[inventoryIndex++] = new ShopItem(3, new ItemStackBuilder(Material.WOOD_SWORD).enchant(
				Enchantment.DAMAGE_ALL, 3).setLore("§ePum Pum -miekka").build());

		itemsInShop[inventoryIndex++] = new ShopItem(10, new ItemStackBuilder(Material.BOW).enchant(
				Enchantment.ARROW_FIRE, 1).enchant(Enchantment.KNOCKBACK, 1).build());

		itemsInShop[inventoryIndex++] = new ShopItem(10, new ItemStackBuilder(Material.BOW).enchant(
				Enchantment.ARROW_INFINITE, 1).hideEnchantments(true).setLore("§eLoputtomien nuolien jousi").build());

		itemsInShop[inventoryIndex++] = new ShopItem(3, new ItemStack(Material.ARROW, 16), new ItemStackBuilder(
				Material.ARROW, 16).setDisplayName("§eSairaan nopeet jouseen").addLore("", "§aMaksaa 3 emeraldia.")
						.build());

		itemsInShop[inventoryIndex++] = new ShopItem(3, new ItemStack(Material.GOLDEN_APPLE, 3));

		itemsInShop[inventoryIndex++] = new ShopItem(100, new ItemStackBuilder(Material.GOLDEN_APPLE, 1, (byte) 1)
				.hideEnchantments(true).setLore("§c§lOMG-OMENA").build());

		// itemsInShop[inventoryIndex++] = new ShopItem(3, new
		// ItemStackBuilder(Material.FISHING_ROD).enchant(
		// Enchantment.DAMAGE_ALL, 5).addLore("§e500 metrinen siima vihollisen
		// tuhoamiseen.").build());

		inventoryIndex = 9;
		itemsInShop[inventoryIndex++] = new ShopItem(1, new ItemStack(Material.LOG, 4));
		itemsInShop[inventoryIndex++] = new ShopItem(1, new ItemStack(Material.GLASS, 16));
		// itemsInShop[inventoryIndex++] = new ShopItem(5, new
		// ItemStack(Material.WATER_BUCKET, 1));
		itemsInShop[inventoryIndex++] = new ShopItem(10, new ItemStack(Material.ENDER_STONE, 64));
		itemsInShop[inventoryIndex++] = new ShopItem(25, new ItemStack(Material.OBSIDIAN, 6));
		itemsInShop[inventoryIndex++] = new ShopItem(20, new ItemStack(Material.DIAMOND_BLOCK, 3));

		// inventoryIndex = 18;
		// itemsInShop[inventoryIndex++] = new ShopItem(15, new
		// ItemStackBuilder(Material.TNT, 2).setLore(
		// "§cIsi sano, että", "§cälä leiki tulella", "§cmut mä en
		// kuunnellu!").setDisplayName("§c§lPUMPUM")
		// .build());

		itemsInShop[inventoryIndex++] = new ShopItem(20, new ItemStackBuilder(Material.GOLD_SPADE).setLore(
				"§eTuu tänne niin", "§epelaan sulla pesistä!").enchant(Enchantment.KNOCKBACK, 2).hideEnchantments(true)
				.setDisplayName("§e§lPesismaila").build());

		for (int i = 0; i < itemsInShop.length; i++) {
			if (itemsInShop[i] != null)
				shopInventory.setItem(i, itemsInShop[i].getDisplayedItem());
		}
	}

	@EventHandler
	public void clickInventory(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();

		if (!e.getInventory().getName().equals(shopInventory.getName()))
			return;
		e.setCancelled(true);
		if (!e.getClick().equals(ClickType.LEFT))
			return;
		if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
			return;

		int slot = e.getSlot();
		ShopItem shopItem = itemsInShop[slot];
		DTMPlayerData pd = pl.getDatabaseManager().getPlayerData(p);

		// TODO: Nullpointer below
		if (shopItem.getPrice() > pd.getEmeralds()) {
			p.sendMessage("§eSinulla ei ole tarpeeksi emeraldeja.");
			p.playSound(p.getLocation(), Sound.ITEM_BREAK, 1, 1);
			return;
		}

		// Handle the 1000 emerald suprise
		if (shopItem.getDisplayedItem().getEnchantments().containsKey(Enchantment.DEPTH_STRIDER)) {
			Bukkit.broadcastMessage("§e§l" + p.getName() + " käytti " + shopItem.getPrice() + " emeraldia kaupassa?!");
			for (Player target : p.getWorld().getPlayers()) {
				target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 5000, 9));
				target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5000, 4));
				target.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 5000, 1));
			}

		}

		if (slot < 0 || slot >= shopInventory.getSize())
			return;
		if (e.getInventory().getItem(slot) == null)
			return;

		pd.setEmeralds(pd.getEmeralds() - shopItem.getPrice());
		p.getInventory().addItem(shopItem.getReceivedItem().clone());
		p.playSound(p.getLocation(), Sound.VILLAGER_YES, 1, 1);

		// Update emerald
		setIndexToEmerald(e.getInventory(), 8, pd.getEmeralds());
	}

	private void setIndexToEmerald(Inventory inv, int index, int emeralds) {
		ItemStack item = new ItemStack(Material.EMERALD);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§aEmeraldeja: " + emeralds);
		item.setItemMeta(meta);
		inv.setItem(index, item);
	}

	public Inventory getShopInventory(DTMPlayerData pd) {
		Inventory inv = Bukkit.createInventory(null, 27, shopInventory.getName());
		inv.setContents(shopInventory.getContents());

		// Replace last index with the current emeraldcount
		setIndexToEmerald(inv, 8, pd.getEmeralds());
		return inv;
	}
}
