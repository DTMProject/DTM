package dtmproject.commands;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import dtmproject.DTM;
import dtmproject.playerdata.DTMPlayerData;
import dtmproject.playerdata.DTMSeasonStats;

public class TopCommand implements CommandExecutor {

	private final DTM dtm;
	// private LinkedList<DTMPlayerData> topListCache = new LinkedList<>();

	public TopCommand(DTM dtm) {
		this.dtm = dtm;

		// Every 2 minutes, get all data from mysql and sort again
		// for (int i = 0; i < dtm.getSeason(); i++) {
		// Bukkit.getScheduler().runTaskTimerAsynchronously(dtm, () -> {
		// topListCache = dtm.getDataHandler().getLeaderboard(100, dtm.getSeason());
		// }, 0, 20 * 120);
		// }

	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		int rawCount = 10;
		if (args.length > 0) {
			try {
				rawCount = Integer.parseInt(args[0]);
			} catch (Exception e) {
				sender.sendMessage("/top [määrä] [kausi]");
			}
		}
		final int finalCount = rawCount;

		int rawSeason = dtm.getSeason();
		if (args.length > 1) {
			try {
				rawSeason = Integer.parseInt(args[1]);
			} catch (Exception e) {
				sender.sendMessage("/top " + finalCount + " [kausi]");
			}
		}
		final int season = rawSeason;
		// God dammit Java... Rust did shading better

		sender.sendMessage("§eHaetaan parhaiden pelaajien lista " + season + ". kaudelta...");

		// TODO: Caching
		Bukkit.getScheduler().runTaskAsynchronously(dtm, () -> {
			int i = 1;
			// for (DTMPlayerData entry : topListCache) {
			for (DTMPlayerData entry : dtm.getDataHandler().getLeaderboard(100, season)) {
				DTMSeasonStats stats = Objects.requireNonNull(entry.seasonStats.get(season));
				// Player possiblePlayer = Bukkit.getPlayer(stats.uuid);

				sender.sendMessage("§e" + (i++) + ". " + entry.nick + ": §a" + stats.getSum() + " §c" + stats.kills
						+ " §4" + stats.deaths + " §7" + stats.getKDRatio());
				if (i == finalCount + 1)
					break;
			}

			sender.sendMessage("");

		});
		return true;
	}
}
