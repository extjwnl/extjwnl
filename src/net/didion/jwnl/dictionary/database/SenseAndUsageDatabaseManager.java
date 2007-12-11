package net.didion.jwnl.dictionary.database;

import java.util.Map;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.util.factory.Createable;
import net.didion.jwnl.util.factory.Param;

/**
 * Database Manager that handles the extended database containing sense key and usage statistics.
 * @author brett
 *
 */
public class SenseAndUsageDatabaseManager extends DatabaseManagerImpl implements Createable {

    /**
     * The SQL statement to grab a synset word. 
     */
    protected static final String SENSE_SYNSET_WORD_SQL =
        "SELECT sw.word, sw.word_index, sw.sense_key, sw.usage_cnt " +
        "FROM Synset s, SynsetWord sw " +
        "WHERE s.synset_id = sw.synset_id AND s.pos = ? AND s.file_offset = ?";
    
    /**
     * Create a new database manager with no connection.  
     *
     */
    public SenseAndUsageDatabaseManager() {
    }

    /**
     * Create a new database manager with a connection. 
     * @param connectionManager - the connection manager. 
     */
    public SenseAndUsageDatabaseManager(ConnectionManager connectionManager) {
        _connectionManager = connectionManager;
    }

    /**
     * Creates a new database manager from the parameters. 
     */
    public Object create(Map params) throws JWNLException {
        String driverClassName = ((Param) params.get(DRIVER)).getValue();
        String url = ((Param) params.get(URL)).getValue();
        String userName = params.containsKey(USERNAME) ? ((Param) params.get(USERNAME)).getValue() : null;
        String password = params.containsKey(PASSWORD) ? ((Param) params.get(PASSWORD)).getValue() : null;
        return new SenseAndUsageDatabaseManager(new ConnectionManager(driverClassName, url, userName, password));
    }
    
    /**
     * {@inheritDoc}
     */
    public Query getSynsetWordQuery(POS pos, long offset) throws JWNLException {
        return createPOSOffsetQuery(pos, offset, SENSE_SYNSET_WORD_SQL);
    }
    
}
