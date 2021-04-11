package dtmproject.shop;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dtmproject.DTM;
import dtmproject.data.DTMPlayerData;
import dtmproject.logic.GameState;

public class ShopCommand implements CommandExecutor {

    private final DTM pl;

    public ShopCommand(DTM pl) {
	this.pl = pl;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	if (!(sender instanceof Player))
	    return true;

	Player p = (Player) sender;
	DTMPlayerData pd = pl.getDataHandler().getPlayerData(p);
	if (pl.getLogicHandler().getGameState() != GameState.RUNNING) {
	    p.sendMessage("3>§b> §8+ §7Peli ei ole käynnissä.");
	    return true;
	}

	if (pd.getTeam() == null) {
	    p.sendMessage("3>§b> §8+ §7Liity peliin komennolla /liity.");
	    return true;
	}

	if (p.getGameMode() != GameMode.SURVIVAL) {
	    p.sendMessage("3>§b> §8+ §7Et ole oikeassa pelitilassa.");
	    return true;
	}

	pl.getShopHandler().openShop(p);
	return true;
    }

}
