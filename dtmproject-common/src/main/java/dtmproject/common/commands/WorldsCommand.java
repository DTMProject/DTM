package dtmproject.common.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§eSinulla ei ole permejä.");
            return true;
        }

        if (cmd.getName().equals("worlds")) {
            sender.sendMessage("§3>§b> §8+ §7Ladatut maailmat:");
            for (World world : Bukkit.getWorlds()) {
                sender.sendMessage("§7    " + world.getName());
            }
        } else if (cmd.getName().equals("world")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§3>§b> §8+ §7Tämän komennon voi suorittaa vain pelaajana.");
                return true;
            }

            Player p = (Player) sender;
            if (args.length == 0) {
                sender.sendMessage("§3>§b> §8+ §7Maailma: " + p.getWorld().getName());
            } else {
                World world = Bukkit.getWorld(args[0]);
                if (world == null) {
                    sender.sendMessage("§3>§b> §8+ §7Maailmaa " + args[0] + " ei ole ladattu.");
                } else {
                    p.teleport(new Location(world, 0, 100, 0));
                }
            }
        }
        return true;

    }
}
