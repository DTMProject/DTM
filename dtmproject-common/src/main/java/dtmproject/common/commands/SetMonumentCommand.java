package dtmproject.common.commands;

import dtmproject.common.DTM;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class SetMonumentCommand implements CommandExecutor {
    private final DTM dtm;

    public SetMonumentCommand(DTM dtm) {
	this.dtm = dtm;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
	if (!sender.isOp()) {
	    sender.sendMessage("§3>§b> §8+ §7Sulla ei ole permejä.");
	    return true;
	}

	if (!(sender instanceof Player)) {
	    sender.sendMessage("§3>§b> §8+ §7Oof I am so confused now.");
	    return true;
	}

	Player p = (Player) sender;
	if (args.length == 0) {
	    sender.sendMessage("§3>§b> §8+ §7/setmonument <team ID> <l|m|r|bl|bm|mr|...> <custom name...>");
	} else if (args.length == 1) {
	    sender.sendMessage(
		    "§3>§b> §8+ §7/setmonument " + args[0].toLowerCase() + " <l|m|r|bl|bm|mr|...> <custom name...>");
	} else if (args.length == 2) {
	    sender.sendMessage("§3>§b> §8+ §7/setmonument " + args[0].toLowerCase() + " " + args[1].toLowerCase()
		    + " <custom name...>");
	} else if (args.length > 2) {
	    // TODO: rewrite the whole part
//			String teamID = args[0].toLowerCase();
//			String pos = args[1].toLowerCase();
//			String customName = Joiner.on(' ').join(args).substring(args[0].length() + 2 + args[1].length());
//
//			if (!dtm.getDataHandler().isMapCreated(editMode)) {
//				p.sendMessage(String.format("§eMappia ei ole olemassa.", editMode));
//				return true;
//			}
//
//			boolean found = false;
//			for (String team : dtm.getDataHandler().getTeamList(editMode)) {
//				if (team.equals(teamID))
//					found = true;
//			}
//			if (!found) {
//				sender.sendMessage("§e" + args[0].toLowerCase() + " tiimiä ei ole olemassa.");
//				return true;
//			}
//
//			Monument mon = new Monument(new NexusBlockLocation(p.getTargetBlock((Set<Material>) null, 10)), pos,
//					customName);
//			dtm.getDataHandler().saveMonument(editMode, teamID, mon.position, mon);
//			sender.sendMessage(Lang.get("monument-saved"));
//			dtm.getEditModeHandler().getPendingList().add(sender);
	}
	return true;
    }
}
