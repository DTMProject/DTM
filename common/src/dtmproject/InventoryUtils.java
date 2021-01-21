package dtmproject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public final class InventoryUtils {

    public static String inventoryToString(Inventory inventory) {
	try {
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

	    // Write the size of the inventory
	    dataOutput.writeInt(inventory.getSize());

	    // Save every element in the list
	    for (int i = 0; i < inventory.getSize(); i++) {
		dataOutput.writeObject(inventory.getItem(i));
	    }

	    // Serialize that array
	    dataOutput.close();
	    return Base64Coder.encodeLines(outputStream.toByteArray());
	} catch (Exception e) {
	    throw new IllegalStateException("Unable to save itemstacks.", e);
	}
    }

    public static Inventory inventoryFromString(String data) {
	try {
	    ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
	    BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
	    Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt());

	    // Read the serialized inventory
	    for (int i = 0; i < inventory.getSize(); i++) {
		inventory.setItem(i, (ItemStack) dataInput.readObject());
	    }
	    dataInput.close();
	    return inventory;
	} catch (Exception e) {
	    throw new RuntimeException("Inventory failed to load");
	}

    }
}
