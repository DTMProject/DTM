package dtmproject.common.tests;

import java.util.LinkedList;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.entity.Player;

import dtmproject.api.WorldlessLocation;
import dtmproject.common.data.IDTMTeam;
import net.md_5.bungee.api.ChatColor;

public class TestingTeam implements IDTMTeam<TestingMonument> {

    @Override
    public String getId() {
	return "test-team-id";
    }

    @Override
    public String getDisplayName() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setDisplayName(String name) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public ChatColor getTeamColor() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setTeamColor(ChatColor color) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public WorldlessLocation getSpawn() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setSpawn(WorldlessLocation spawn) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public LinkedList<TestingMonument> getMonuments() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setMonuments(LinkedList<TestingMonument> monuments) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public Set<Player> getPlayers() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Color getLeatherColor() {
	// TODO Auto-generated method stub
	return null;
    }}
