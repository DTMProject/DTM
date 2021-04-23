package dtmproject.common;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import dtmproject.common.data.DTMTeam;

public class TeamArmorUtils {
    public static ItemStack[] getArmorForTeam(Player p, DTMTeam team) {
	ItemStack[] items = new ItemStack[4];

	ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
	LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
	meta.setColor(team.getLeatherColor());
	helmet.setItemMeta(meta);
	items[3] = helmet;

	ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
	LeatherArmorMeta chestMeta = (LeatherArmorMeta) chestplate.getItemMeta();
	chestMeta.setColor(team.getLeatherColor());
	chestplate.setItemMeta(chestMeta);
	items[2] = chestplate;

	ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
	LeatherArmorMeta legMeta = (LeatherArmorMeta) leggings.getItemMeta();
	legMeta.setColor(team.getLeatherColor());
	leggings.setItemMeta(legMeta);
	items[1] = leggings;

	ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
	LeatherArmorMeta bootMeta = (LeatherArmorMeta) boots.getItemMeta();
	bootMeta.setColor(team.getLeatherColor());
	boots.setItemMeta(bootMeta);
	items[0] = boots;
	return items;
    }

}
