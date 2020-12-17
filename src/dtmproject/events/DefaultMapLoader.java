package dtmproject.events;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import dtmproject.DTM;
import dtmproject.NexusBlockLocation;
import dtmproject.WorldlessLocation;
import dtmproject.data.DTMMap;
import dtmproject.setup.DTMTeam;
import dtmproject.setup.Monument;
import net.md_5.bungee.api.ChatColor;

public class DefaultMapLoader {
	private final DTM pl;

	public DefaultMapLoader(DTM pl) {
		this.pl = pl;
	}

	boolean alreadyOnceLoaded = false;

	public Set<DTMMap> getMaps() {
		if (alreadyOnceLoaded)
			throw new IllegalStateException("Maps are already loaded once");

		Set<DTMMap> maps = new LinkedHashSet<>();
		maps.add(new DTMMap(pl, "hk1", "Hiekkakivet I", new WorldlessLocation(-36, 150.0, 0.5, -90, 30), 0, getTeams(
				DefaultMapID.HK1)));
		maps.add(new DTMMap(pl, "hk4", "Hiekkakivet IV", new WorldlessLocation(-36, 150, 0.5, -90, 30), 0, getTeams(
				DefaultMapID.HK4)));

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
			break;
		case HK3:
			break;
		case HK4:
			teams.add(new DTMTeam(pl, "mansikka", "Mansikka", ChatColor.RED, new WorldlessLocation(0.5, 101.0, -64.5, 0,
					0), getMonuments(id, DefaultTeamID.MANSIKKA)));
			teams.add(new DTMTeam(pl, "mustikka", "Mustikka", ChatColor.DARK_PURPLE, new WorldlessLocation(0.5, 101.0,
					65, 180, 0), getMonuments(id, DefaultTeamID.MANSIKKA)));
			break;
		}
		return teams;
	}

	public LinkedList<Monument> getMonuments(DefaultMapID id, DefaultTeamID team) {
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
			break;
		case HK3:
			break;
		case HK4:
			if (team == DefaultTeamID.MANSIKKA) {
				monuments.add(new Monument(new NexusBlockLocation(-31, 103, -65), "l", "Vasen"));
				monuments.add(new Monument(new NexusBlockLocation(0, 100, 0), "m", "Midi"));
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
	MANSIKKA, MUSTIKKA, YELLOW, PURPLE
}
