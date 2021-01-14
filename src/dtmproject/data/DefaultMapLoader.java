package dtmproject.data;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import dtmproject.DTM;
import dtmproject.NexusBlockLocation;
import dtmproject.WorldlessLocation;
import dtmproject.setup.DTMTeam;
import dtmproject.setup.Monument;
import net.md_5.bungee.api.ChatColor;

public class DefaultMapLoader {
	public static final ItemStack[] DEFAULT_KIT = getDefaultKit();
	private final DTM pl;

	public DefaultMapLoader(DTM pl) {
		this.pl = pl;
	}

	boolean alreadyOnceLoaded = false;

	public Set<DTMMap> getMaps() {
		if (alreadyOnceLoaded)
			throw new IllegalStateException("Maps are already loaded once");

		Set<DTMMap> maps = new LinkedHashSet<>();
		maps.add(new DTMMap(pl, "HK1", "Hiekkakivet I", new WorldlessLocation(-36, 150.0, 0.5, -90, 30), 0, DEFAULT_KIT,
				getTeams(DefaultMapID.HK1)));

		maps.add(new DTMMap(pl, "HK2", "Hiekkakivet II", new WorldlessLocation(-45.5, 114, 0.5, 275, 0), 0, DEFAULT_KIT,
				getTeams(DefaultMapID.HK2)));

		maps.add(new DTMMap(pl, "HK3", "Hiekkakivet III", new WorldlessLocation(0.5, 126.0, 70.5, 180, 0), 0,
				DEFAULT_KIT, getTeams(DefaultMapID.HK3)));

		maps.add(new DTMMap(pl, "HK4", "Hiekkakivet IV", new WorldlessLocation(-57.5, 124, 0.5, -90, 10), 0,
				DEFAULT_KIT, getTeams(DefaultMapID.HK4)));

		alreadyOnceLoaded = true;
		return maps;
	}

	public LinkedHashSet<DTMTeam> getTeams(DefaultMapID id) {
		LinkedHashSet<DTMTeam> teams = new LinkedHashSet<>();
		switch (id) {
		case HK1:
			teams.add(new DTMTeam(pl, "yellow", "Keltainen", ChatColor.YELLOW, new WorldlessLocation(0.5, 100.0, -55.5,
					0, 0), getMonuments(id, DefaultTeamID.YELLOW)));
			teams.add(new DTMTeam(pl, "purple", "Purppura", ChatColor.DARK_PURPLE, new WorldlessLocation(0.5, 100.0,
					56.5, 180, 0), getMonuments(id, DefaultTeamID.PURPLE)));
			break;
		case HK2:
			teams.add(new DTMTeam(pl, "yellow", "Keltainen", ChatColor.YELLOW, new WorldlessLocation(0.5, 100.0, 51.5,
					180, 0), getMonuments(id, DefaultTeamID.YELLOW)));
			teams.add(new DTMTeam(pl, "purple", "Purppura", ChatColor.DARK_PURPLE, new WorldlessLocation(0.5, 100.0,
					-50.5, 0, 0), getMonuments(id, DefaultTeamID.PURPLE)));
			break;
		case HK3:
			teams.add(new DTMTeam(pl, "vesi", "Vesi", ChatColor.AQUA, new WorldlessLocation(55.5, 100, 0.5, 90, 0),
					getMonuments(id, DefaultTeamID.VESI)));
			teams.add(new DTMTeam(pl, "aurinko", "Aurinko", ChatColor.GOLD, new WorldlessLocation(-54.5, 100, 0.5, 270,
					0), getMonuments(id, DefaultTeamID.AURINKO)));

			break;
		case HK4:
			teams.add(new DTMTeam(pl, "mansikka", "Mansikka", ChatColor.RED, new WorldlessLocation(0.5, 101.0, -64.5, 0,
					0), getMonuments(id, DefaultTeamID.MANSIKKA)));
			teams.add(new DTMTeam(pl, "mustikka", "Mustikka", ChatColor.DARK_PURPLE, new WorldlessLocation(0.5, 101.0,
					65, 180, 0), getMonuments(id, DefaultTeamID.MUSTIKKA)));
			break;
		}
		return teams;
	}

	public static LinkedList<Monument> getMonuments(DefaultMapID id, DefaultTeamID team) {
		LinkedList<Monument> monuments = new LinkedList<>();
		switch (id) {
		case HK1:
			if (team == DefaultTeamID.YELLOW) {
				monuments.add(new Monument(new NexusBlockLocation(-32, 104, -56), "l", "Vasen"));
				monuments.add(new Monument(new NexusBlockLocation(0, 104, -88), "m", "Midi"));
				monuments.add(new Monument(new NexusBlockLocation(32, 104, -56), "r", "Oikea"));
			} else if (team == DefaultTeamID.PURPLE) {
				monuments.add(new Monument(new NexusBlockLocation(32, 104, 56), "l", "Vasen"));
				monuments.add(new Monument(new NexusBlockLocation(0, 104, 88), "m", "Midi"));
				monuments.add(new Monument(new NexusBlockLocation(-32, 104, 56), "r", "Oikea"));
			}

			break;
		case HK2:
			if (team == DefaultTeamID.YELLOW) {
				monuments.add(new Monument(new NexusBlockLocation(36, 104, 51), "l", "Vasen"));
				monuments.add(new Monument(new NexusBlockLocation(0, 104, 87), "m", "Midi"));
				monuments.add(new Monument(new NexusBlockLocation(-36, 104, 51), "r", "Oikea"));
			} else if (team == DefaultTeamID.PURPLE) {
				monuments.add(new Monument(new NexusBlockLocation(-36, 104, -51), "l", "Vasen"));
				monuments.add(new Monument(new NexusBlockLocation(0, 104, -87), "m", "Midi"));
				monuments.add(new Monument(new NexusBlockLocation(36, 104, -51), "r", "Oikea"));
			}
			break;
		case HK3:
			if (team == DefaultTeamID.VESI) {
				monuments.add(new Monument(new NexusBlockLocation(58, 102, 50), "fr", "Etu-oikea"));
				monuments.add(new Monument(new NexusBlockLocation(90, 102, 18), "br", "Taka-oikea"));
				monuments.add(new Monument(new NexusBlockLocation(58, 102, -50), "fl", "Etu-vasen"));
				monuments.add(new Monument(new NexusBlockLocation(90, 102, -18), "bl", "Taka-vasen"));
			} else if (team == DefaultTeamID.AURINKO) {
				monuments.add(new Monument(new NexusBlockLocation(-58, 102, -50), "fr", "Etu-oikea"));
				monuments.add(new Monument(new NexusBlockLocation(-90, 102, -18), "br", "Taka-oikea"));
				monuments.add(new Monument(new NexusBlockLocation(-58, 102, 50), "fl", "Etu-vasen"));
				monuments.add(new Monument(new NexusBlockLocation(-90, 102, 18), "bl", "Taka-vasen"));
			}
			break;
		case HK4:
			if (team == DefaultTeamID.MANSIKKA) {
				monuments.add(new Monument(new NexusBlockLocation(-31, 103, -65), "l", "Vasen"));
				monuments.add(new Monument(new NexusBlockLocation(0, 103, -96), "m", "Midi"));
				monuments.add(new Monument(new NexusBlockLocation(31, 100, -65), "r", "Oikea"));
			} else if (team == DefaultTeamID.MUSTIKKA) {
				monuments.add(new Monument(new NexusBlockLocation(31, 103, 65), "l", "Vasen"));
				monuments.add(new Monument(new NexusBlockLocation(0, 103, 96), "m", "Midi"));
				monuments.add(new Monument(new NexusBlockLocation(-31, 103, 65), "r", "Oikea"));
			}
			break;
		}
		return monuments;
	}

	public static ItemStack[] getDefaultKit() {
		int i = 0;

		ItemStack[] kit = new ItemStack[9];
		kit[i++] = new ItemStack(Material.IRON_AXE);
		kit[i++] = new ItemStack(Material.BOW);
		kit[i++] = new ItemStack(Material.DIAMOND_PICKAXE);
		kit[i++] = new ItemStack(Material.WOOD, 64);
		kit[i++] = new ItemStack(Material.WOOD, 64);
		kit[i++] = new ItemStack(Material.GOLDEN_APPLE, 3);
		kit[i++] = new ItemStack(Material.ARROW, 16);

		return kit;
	}

	public void copyDefaultMapFiles() {
		for (DefaultMapID defaultMapID : DefaultMapID.values()) {
			String mapName = defaultMapID.toString();
			File outputFolder = new File(pl.getDataFolder(), "maps/" + mapName);

			if (outputFolder.exists())
				continue;

			ZipInputStream zis = new ZipInputStream(pl.getResource(mapName + ".zip"));
			try {
				ZipEntry entry;
				while ((entry = zis.getNextEntry()) != null) {
					File f = new File(outputFolder, entry.getName());
					if (entry.isDirectory()) {
						f.mkdirs();
						continue;
					}

					FileOutputStream fos = new FileOutputStream(f);
					f.createNewFile();

					byte[] buffer = new byte[4096];
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}

					fos.close();
				}
				zis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

enum DefaultMapID {
	HK1, HK2, HK3, HK4
}

enum DefaultTeamID {
	MANSIKKA, MUSTIKKA, YELLOW, PURPLE, VESI, AURINKO
}
