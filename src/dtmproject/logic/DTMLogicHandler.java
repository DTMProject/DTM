package dtmproject.logic;

import java.util.AbstractMap;
import java.util.Optional;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.juubes.nexus.data.AbstractPlayerData;
import com.juubes.nexus.data.AbstractTeam;
import com.juubes.nexus.logic.AbstractLogicHandler;
import com.juubes.nexus.logic.GameState;

import dtmproject.DTM;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

public class DTMLogicHandler {
	private final DTM pl;

	@Getter
	private GameState gameState;

	public DTMLogicHandler(DTM pl) {
		this.pl = pl;
	}

	public void startGame(Optional<String> mapRequest) {
		if (!mapRequest.isPresent()) {
			pl.getGameWorldHandler().nextMap(mapRequest.get());
		}

		this.gameState = GameState.RUNNING;
	}

	public void togglePause() {
		throw new NotImplementedException();
	}

	public void endGame(AbstractTeam winnerTeam) {
		throw new NotImplementedException();
	}

	public void sendPlayerToGame(Player p) {
		AbstractPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());

	}

	public void sendToSpectate(Player p) {
		AbstractPlayerData pd = pl.getDataHandler().getPlayerData(p.getUniqueId());
		pd.team = null;

		p.setGameMode(GameMode.SPECTATOR);

		// Teleport to lobby
		AbstractMap currentMap = pl.getGameWorldHandler().getCurrentMap();
		World currentWorld = pl.getGameWorldHandler().getCurrentWorld();
		Location lobby = currentMap.lobby.toLocation(currentWorld);
		p.teleport(lobby);

		if (p.getWorld() != currentWorld)
			return;

		if (lobby != null) {
			p.teleport(lobby);
		} else {
			System.err.println("Lobby null for map " + currentMap.displayName);
			p.teleport(new Location(currentWorld, 0, 100, 0));
		}
		p.setGameMode(GameMode.SPECTATOR);
		p.getInventory().clear();

		// Handle appropriate nametag colours
		p.setDisplayName("§7" + p.getName());
		p.setPlayerListName("§8[" + ChatColor.translateAlternateColorCodes('&', pd.prefix) + "§8] §7" + p.getName());
		p.setCustomName(pd.team.teamColor + p.getName());
		p.setCustomNameVisible(false);

	}

	public void setPlayerToSmallestTeam(Player p) {

	}

}
