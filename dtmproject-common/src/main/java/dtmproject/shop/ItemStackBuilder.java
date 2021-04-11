package dtmproject.shop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackBuilder {
    private final ItemStack item;

    public ItemStackBuilder(ItemStack item) {
	this.item = item;
    }

    public ItemStackBuilder(Material mat) {
	this.item = new ItemStack(mat);
    }

    public ItemStackBuilder(Material mat, int amount) {
	this.item = new ItemStack(mat, amount);
    }

    public ItemStackBuilder(Material mat, int amount, byte data) {
	this.item = new ItemStack(mat, amount, data);
    }

    public ItemStackBuilder hideEnchantments(boolean b) {
	ItemMeta meta = item.getItemMeta();
	if (b)
	    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
	else
	    meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
	item.setItemMeta(meta);
	return this;
    }

    public ItemStackBuilder enchant(Enchantment enc, int level) {
	item.addUnsafeEnchantment(enc, level);
	return this;
    }

    public ItemStack build() {
	return item;
    }

    public ItemStackBuilder setLore(String... lines) {
	ItemMeta meta = item.getItemMeta();
	meta.setLore(Arrays.asList(lines));
	item.setItemMeta(meta);
	return this;
    }

    public ItemStackBuilder setDisplayName(String name) {
	ItemMeta meta = item.getItemMeta();
	meta.setDisplayName(name);
	item.setItemMeta(meta);
	return this;
    }

    public ItemStackBuilder addLore(String... lines) {
	List<String> lore = item.getItemMeta().getLore();
	if (lore == null)
	    lore = new ArrayList<>();
	for (String line : lines) {
	    lore.add(line);
	}
	ItemMeta meta = item.getItemMeta();
	meta.setLore(lore);
	item.setItemMeta(meta);
	return this;
    }

    public ItemStackBuilder fakeEnchant() {
	return this.enchant(Enchantment.PROTECTION_FALL, 1).hideEnchantments(true);
    }
}
