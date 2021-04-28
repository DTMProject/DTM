package dtmproject.common.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dtmproject.common.DTM;
import lombok.Getter;

public class EditModeCommand implements CommandExecutor {
    private final DTM pl;

    private final HashMap<UUID, String> editModePlayers = new HashMap<>();

    @Getter
    private Set<CommandSender> pendingList = new HashSet<>();

    public EditModeCommand(DTM pl) {
	this.pl = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
	if (!sender.isOp()) {
	    sender.sendMessage("§4>§c> §8- §7Sulla ei ole permejä muokkaustilaan senkin pelle.");
	    return true;
	}

	if (!(sender instanceof Player)) {
	    sender.sendMessage("§4>§c> §8- §7Et voi tehd§ tätä komentoa.");
	    return true;
	}

	Player p = (Player) sender;
	if (args.length > 0) {

	    boolean foundMapID = false;
	    for (String map : pl.getActiveMapList()) {
		if (map.equalsIgnoreCase(args[0])) {
		    foundMapID = true;
		    setEditModeWorld(p, map);
		    break;
		}
	    }
	    if (!foundMapID) {
		sender.sendMessage("§4>§c> §8- §7Mappia ei löytynyt.");
	    }

	}
	sender.sendMessage("§3>§b> §8+ §7Muokkaustilasi on maailma " + getEditWorld(p));
	sender.sendMessage("§3>§b> §8+ §7Voit vaihtaa muokkaustilaa komennolla: /editmode <Map ID>");
	return true;
    }

    public String getEditWorld(Player p) {
	if (editModePlayers.get(p.getUniqueId()) == null)
	    return p.getWorld().getName();
	else
	    return editModePlayers.get(p.getUniqueId());
    }

    public void setEditModeWorld(Player p, String mapID) {
	editModePlayers.put(p.getUniqueId(), mapID);
    }
}
