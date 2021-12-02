package dtmproject.common.data.impl;

import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

import dtmproject.api.data.IDTMDataHandler;
import dtmproject.common.DTM;
import dtmproject.common.data.DTMMap;
import dtmproject.common.data.DTMPlayerData;

public class MongoDBDatabaseImpl implements IDTMDataHandler<DTMPlayerData, DTMMap> {

    public MongoDBDatabaseImpl(DTM dtm) {
	// TODO Auto-generated constructor stub
    }

    @Override
    public void init() {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void loadMaps() {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void loadPlayerData(UUID uuid, String lastSeenName) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public DTMPlayerData getPlayerData(UUID uuid) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void savePlayerData(UUID uuid) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void unloadPlayerdata(UUID uuid, boolean save) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public DTMMap createMapIfNotExists(String mapID) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public DTMMap getMap(String mapID) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void saveMap(DTMMap map) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public LinkedList<DTMPlayerData> getLeaderboard(int count, int season) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean mapExists(String req) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public Set<String> getLoadedMaps() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void shutdown() {
	// TODO Auto-generated method stub
	
    }

    @Override
    public Double[] getWinLossDistribution() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void updateWinLossDistributionCache() {
	// TODO Auto-generated method stub
	
    }}
