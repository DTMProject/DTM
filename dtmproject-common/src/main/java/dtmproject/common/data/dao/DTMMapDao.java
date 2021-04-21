package dtmproject.common.data.dao;

import java.sql.SQLException;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import dtmproject.common.data.DTMMap;

public class DTMMapDao extends BaseDaoImpl<DTMMap, String> {

    protected DTMMapDao(ConnectionSource connectionSource, Class<DTMMap> dataClass) throws SQLException {
	super(connectionSource, dataClass);

	
    }
    

}
