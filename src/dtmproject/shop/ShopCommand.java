package dtmproject.shop;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dtmproject.DTM;
import dtmproject.logic.GameState;
import dtmproject.playerdata.DTMPlayerData;

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
			p.sendMessage("§ePeli ei ole käynnissä.");
			return true;
		}

		if (pd.getTeam() == null) {
			p.sendMessage("§eLiity peliin komennolla /join.");
			return true;
		}

		if (p.getGameMode() != GameMode.SURVIVAL) {
			p.sendMessage("§eEt ole oikeassa pelitilassa.");
			return true;
		}

		p.openInventory(pl.getShopHandler().getShopInventory(pd));
		return true;
	}

}
