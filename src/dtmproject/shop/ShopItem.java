package dtmproject.shop;

import org.bukkit.inventory.ItemStack;

public class ShopItem {
	private ItemStack receivedItem, displayItem;
	private int price;

	public ShopItem(int price, ItemStack receivedItem, ItemStack displayedItem) {
		this.price = price;
		this.receivedItem = receivedItem;
		this.displayItem = displayedItem;
	}

	public ShopItem(int price, ItemStack receivedItem) {
		this.price = price;
		this.receivedItem = receivedItem;
		this.displayItem = new ItemStackBuilder(receivedItem.clone()).addLore("", "§aMaksaa " + price + " emeraldia.")
				.build();
	}

	public ItemStack getReceivedItem() {
		return receivedItem;
	}

	public ItemStack getDisplayedItem() {
		return displayItem;
	}

	public int getPrice() {
		return price;
	}
}
