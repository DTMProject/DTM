package dtmproject.common.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dtmproject.common.DTM;
import dtmproject.common.data.DTMPlayerData;

public class StatsCommand implements CommandExecutor {
	private final DTM pl;

	public StatsCommand(DTM pl) {
		this.pl = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (args.length == 0) {
			// Print own stats
			if (!(sender instanceof Player)) {
				sender.sendMessage("§3>§b> §8+ §7Dude... Ei sul oo statsei.");
				sender.sendMessage("§3>§b> §8+ §7/stats <nimi>");
				return true;
			}

			Player p = (Player) sender;
			DTMPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
			sender.sendMessage("§3>§b> §8+ §7§l    Kausi");
			sender.sendMessage(pd.getSeasonStats().toString());
			sender.sendMessage("§eElo-pisteet: §4[" + pd.getRatingLevel() + "§4]§e" + (int) pd.getEloRating());
			// TODO: all stats have to be loaded from db if they want to be shown
//			sender.sendMessage("§e§l Yhteensä");
//			sender.sendMessage(pd.getTotalStats().toString());
		} else {
			// Print targets' stats
			for (String arg : args) {
				if (arg.length() <= 16 && arg.length() >= 3) {
					sender.sendMessage("§eAnnoit huonon nimen.");
					return true;
				}
				
				
				DTMPlayerData pd = pl.getDataHandler().getOfflineData(arg);
				sender.sendMessage("§3>§b> §8+ §7Ei löydetty statseja pelaajalle " + args[0] + ".");

			}
			return true;
		}
		return true;
	}
}
