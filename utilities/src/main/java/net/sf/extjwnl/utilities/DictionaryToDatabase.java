package net.sf.extjwnl.utilities;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.dictionary.AbstractCachingDictionary;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.database.ConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * DictionaryToDatabase is used to transfer a WordNet file database into an actual database structure.
 *
 * @author Brett Walenz <bwalenz@users.sourceforge.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class DictionaryToDatabase {

    private static final Log log = LogFactory.getLog(DictionaryToDatabase.class);

    protected static int INTERNAL_ID = 0;
    protected static long TIME = 0L;

    protected static final String[] tables = {
            "indexword",
            "synset",
            "synsetword",
            "synsetpointer",
            "synsetverbframe",
            "indexwordsynset",
            "exceptions"
    };

    /**
     * The database connection.
     */
    protected final Connection connection;
    /**
     * Mapping of database id's to synset offset id's. 1 to 1.
     */
    protected final Map<Integer, long[]> idToSynsetOffset;

    /**
     * Mapping of synset offset id's to database id's. 1:1.
     */
    protected final Map<Long, Integer> synsetOffsetToId;

    protected final Dictionary dictionary;

    /**
     * Run the program, requires 4 arguments. See DictionaryToDatabase.txt for more documentation.
     *
     * @param args args
     */
    public static void main(String args[]) {
        if (args.length < 4) {
            System.out.println("Usage: DictionaryToDatabase <property file> <create tables script> <driver class> <connection url> [username [password]]");
            System.exit(0);
        }
        Dictionary dictionary = null;
        try {
            dictionary = Dictionary.getInstance(new FileInputStream(args[0]));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        Connection conn = null;

        try {
            String scriptFileName = args[1];
            ConnectionManager mgr = new ConnectionManager(args[2], args[3], args.length <= 4 ? null : args[4], args.length <= 5 ? null : args[5]);
            conn = mgr.getConnection();
            conn.setReadOnly(false);
            DictionaryToDatabase d2d = new DictionaryToDatabase(dictionary, conn);
            d2d.createTables(scriptFileName);
            d2d.insertData();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected static synchronized int nextId() {
        INTERNAL_ID++;
        if (log.isDebugEnabled() && INTERNAL_ID % 1000 == 0) {
            long temp = System.currentTimeMillis();
            log.debug("inserted " + INTERNAL_ID + "th entry");
            log.debug("free memory: " + Runtime.getRuntime().freeMemory());
            log.debug("time: " + (temp - TIME));
            TIME = System.currentTimeMillis();
        }
        return INTERNAL_ID;
    }

    /**
     * Create a new DictionaryToDatabase with a database connection. JWNL already initialized.
     *
     * @param dictionary the dictionary
     * @param conn       the database connection
     */
    public DictionaryToDatabase(Dictionary dictionary, Connection conn) {
        this.dictionary = dictionary;
        idToSynsetOffset = new HashMap<Integer, long[]>();
        synsetOffsetToId = new HashMap<Long, Integer>();
        connection = conn;
        if (dictionary instanceof AbstractCachingDictionary) {
            ((AbstractCachingDictionary) dictionary).setCachingEnabled(false);
        }
    }

    /**
     * Create the database tables.
     *
     * @param scriptFilePath - the sql script filename
     * @throws IOException  IOException
     * @throws SQLException SQLException
     */
    public void createTables(String scriptFilePath) throws IOException, SQLException {
        log.info("creating tables");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(scriptFilePath)));
        StringBuilder buf = new StringBuilder();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            line = line.trim();
            if (line.length() <= 0) {
                continue;
            }
            buf.append(line);
            if (line.endsWith(";")) {
                log.debug(buf.toString());
                connection.prepareStatement(buf.toString()).execute();
                buf = new StringBuilder();
            } else {
                buf.append(" ");
            }
        }

        log.info("created tables");
    }

    /**
     * Inserts the data into the database. Iterates through the various POS,
     * then stores all the index words, synsets, exceptions of that POS.
     *
     * @throws JWNLException JWNLException
     * @throws SQLException  SQLException
     */
    public void insertData() throws JWNLException, SQLException {
        if (null != connection.getMetaData() && connection.getMetaData().getDatabaseProductName().contains("MySQL")) {

            Statement s = connection.createStatement();
            log.info("disabling autocommit...");
            connection.setAutoCommit(false);
            log.info("disabling keys...");

            for (String table : tables) {
                String sql = "ALTER TABLE `" + table + "` DISABLE KEYS;";
                log.debug(sql);
                s.execute(sql);
            }
        }
        TIME = System.currentTimeMillis();
        for (POS pos : POS.getAllPOS()) {
            log.info("inserting data for pos " + pos);
            storeIndexWords(dictionary.getIndexWordIterator(pos));
            storeSynsets(dictionary.getSynsetIterator(pos));
            storeIndexWordSynsets();
            storeExceptions(dictionary.getExceptionIterator(pos));
            idToSynsetOffset.clear();
            synsetOffsetToId.clear();
            log.info("done inserting data for pos " + pos);
        }
        if (null != connection.getMetaData() && connection.getMetaData().getDatabaseProductName().contains("MySQL")) {
            Statement s = connection.createStatement();
            log.info("enabling keys...");
            for (String table : tables) {
                String sql = "ALTER TABLE `" + table + "` ENABLE KEYS;";
                log.debug(sql);
                s.execute(sql);
            }
            log.info("committing...");
            connection.commit();
        }
    }

    /**
     * Store all the index words.
     *
     * @param itr - the index word iterator
     * @throws SQLException SQLException
     */
    protected void storeIndexWords(Iterator<IndexWord> itr) throws SQLException {
        log.info("storing index words");
        PreparedStatement iwStmt = connection.prepareStatement("INSERT INTO indexword VALUES(?,?,?)");
        int count = 0;
        while (itr.hasNext()) {
            if (count % 1000 == 0) {
                log.info("indexword: " + count);
            }
            count++;
            IndexWord iw = itr.next();
            int id = nextId();
            iwStmt.setInt(1, id);
            iwStmt.setString(2, iw.getLemma());
            iwStmt.setString(3, iw.getPOS().getKey());
            iwStmt.execute();
            idToSynsetOffset.put(id, iw.getSynsetOffsets());
        }
        log.info("indexword: " + count);
        log.info("stored index words");
    }

    /**
     * Store all of the synsets in the database.
     *
     * @param itr itr
     * @throws SQLException SQLException
     */
    protected void storeSynsets(Iterator<Synset> itr) throws SQLException {
        PreparedStatement synsetStmt = connection.prepareStatement("INSERT INTO synset VALUES(?,?,?,?,?,?)");
        PreparedStatement synsetWordStmt = connection.prepareStatement("INSERT INTO synsetword VALUES(?,?,?,?,?,?)");
        PreparedStatement synsetPointerStmt = connection.prepareStatement("INSERT INTO synsetpointer VALUES(?,?,?,?,?,?,?)");
        PreparedStatement synsetVerbFrameStmt = connection.prepareStatement("INSERT INTO synsetverbframe VALUES(?,?,?,?)");
        log.info("storing synsets");
        int count = 0;
        while (itr.hasNext()) {
            if (count % 1000 == 0) {
                log.info("synset: " + count);
            }
            count++;
            Synset synset = itr.next();
            int id = nextId();
            synsetOffsetToId.put(synset.getOffset(), id);
            synsetStmt.setInt(1, id);
            synsetStmt.setLong(2, synset.getOffset());
            synsetStmt.setLong(3, synset.getLexFileNum());
            synsetStmt.setString(4, synset.getPOS().getKey());
            synsetStmt.setBoolean(5, POS.ADJECTIVE == synset.getPOS() && synset.isAdjectiveCluster());
            synsetStmt.setString(6, synset.getGloss());
            synsetStmt.execute();
            List<Word> words = synset.getWords();
            synsetWordStmt.setInt(2, id);

            BitSet allWordFrames = null;
            if (synset instanceof VerbSynset) {
                synsetVerbFrameStmt.setInt(2, id);
                allWordFrames = synset.getVerbFrameFlags();
                synsetVerbFrameStmt.setInt(4, 0);//applicable to all words
                for (int i = allWordFrames.nextSetBit(0); i >= 0; i = allWordFrames.nextSetBit(i + 1)) {
                    synsetVerbFrameStmt.setInt(1, nextId());
                    synsetVerbFrameStmt.setInt(3, i);
                    synsetVerbFrameStmt.execute();
                }
            }

            for (Word word : words) {
                int wordId = nextId();
                synsetWordStmt.setInt(1, wordId);

                synsetWordStmt.setString(3, word.getLemma());
                synsetWordStmt.setInt(4, word.getIndex());
                synsetWordStmt.setInt(5, word.getUseCount());
                synsetWordStmt.setLong(6, word.getLexId());

                synsetWordStmt.execute();
                if (word instanceof Verb) {
                    synsetVerbFrameStmt.setInt(4, word.getIndex());
                    BitSet bits = ((Verb) word).getVerbFrameFlags();
                    for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
                        if (null != allWordFrames && !allWordFrames.get(i)) {
                            synsetVerbFrameStmt.setInt(1, nextId());
                            synsetVerbFrameStmt.setInt(3, i);
                            synsetVerbFrameStmt.execute();
                        }
                    }
                }
            }

            List<Pointer> pointers = synset.getPointers();
            synsetPointerStmt.setInt(2, id);
            for (Pointer pointer : pointers) {
                synsetPointerStmt.setInt(1, nextId());
                synsetPointerStmt.setString(3, pointer.getType().getKey());
                synsetPointerStmt.setLong(4, pointer.getTargetOffset());
                synsetPointerStmt.setString(5, pointer.getTargetPOS().getKey());
                synsetPointerStmt.setInt(6, pointer.getSourceIndex());
                synsetPointerStmt.setInt(7, pointer.getTargetIndex());
                synsetPointerStmt.execute();
            }
        }
        log.info("synset: " + count);
        log.info("stored synsets");
    }

    /**
     * Store the index word synsets.
     *
     * @throws SQLException SQLException
     */
    protected void storeIndexWordSynsets() throws SQLException {
        log.info("storing index word synsets");
        PreparedStatement iwsStmt = connection.prepareStatement("INSERT INTO indexwordsynset VALUES(?,?,?)");
        int count = 0;
        for (Map.Entry<Integer, long[]> entry : idToSynsetOffset.entrySet()) {
            if (count % 1000 == 0) {
                log.info("index word synset: " + count);
            }
            count++;

            int iwId = entry.getKey();
            iwsStmt.setInt(2, iwId);
            long offsets[] = entry.getValue();
            for (long offset : offsets) {
                int synsetId = synsetOffsetToId.get(offset);
                iwsStmt.setInt(1, nextId());
                iwsStmt.setLong(3, synsetId);
                iwsStmt.execute();
            }
        }
        log.info("index word synset: " + count);
        log.info("stored index word synsets");
    }


    /**
     * Store the exceptions file.
     *
     * @param itr iterator
     * @throws SQLException SQLException
     */
    protected void storeExceptions(Iterator<Exc> itr) throws SQLException {
        log.info("storing exceptions");
        PreparedStatement exStmt = connection.prepareStatement("INSERT INTO exceptions VALUES(?,?,?,?)");
        while (itr.hasNext()) {
            Exc exc = itr.next();
            exStmt.setString(4, exc.getLemma());
            for (Object o : exc.getExceptions()) {
                exStmt.setInt(1, nextId());
                exStmt.setString(2, exc.getPOS().getKey());
                exStmt.setString(3, (String) o);
                exStmt.execute();
            }
        }
        log.info("stored exceptions");
    }
}