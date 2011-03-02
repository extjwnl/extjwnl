package net.didion.jwnl.utilities;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.*;
import net.didion.jwnl.dictionary.AbstractCachingDictionary;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.dictionary.database.ConnectionManager;
import net.didion.jwnl.util.MessageLog;
import net.didion.jwnl.util.MessageLogLevel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * DictionaryToDatabase is used to transfer a WordNet file database into an actual database structure.
 *
 * @author brett
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class DictionaryToDatabase {

    private static final MessageLog log = new MessageLog(DictionaryToDatabase.class);

    protected static int INTERNAL_ID = 0;
    protected static long TIME = 0L;

    /**
     * The database connection.
     */
    protected Connection connection;
    /**
     * Mapping of database id's to synset offset id's. 1 to 1.
     */
    protected Map<Integer, long[]> idToSynsetOffset;

    /**
     * Mapping of synset offset id's to database id's. 1:1.
     */
    protected Map<Long, Integer> synsetOffsetToId;

    protected Dictionary dictionary;

    /**
     * Run the program, requires 4 arguments. See DictionaryToDatabase.txt for more documentation.
     *
     * @param args args
     */
    public static void main(String args[]) {
        if (args.length < 4) {
            System.out.println("DictionaryToDatabase <property file> <create tables script> <driver class> <connection url> [username [password]]");
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
        if (log.isLevelEnabled(MessageLogLevel.DEBUG) && INTERNAL_ID % 1000 == 0) {
            long temp = System.currentTimeMillis();
            System.out.println("inserted " + INTERNAL_ID + "th entry");
            System.out.println("free memory: " + Runtime.getRuntime().freeMemory());
            System.out.println("time: " + (temp - TIME));
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
        System.out.println("creating tables");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(scriptFilePath)));
        StringBuffer buf = new StringBuffer();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            line = line.trim();
            if (line.length() <= 0) {
                continue;
            }
            buf.append(line);
            if (line.endsWith(";")) {
                System.out.println(buf.toString());
                connection.prepareStatement(buf.toString()).execute();
                buf = new StringBuffer();
            } else {
                buf.append(" ");
            }
        }

        System.out.println("created tables");
    }

    /**
     * Inserts the data into the database. Iterates through the various POS,
     * then stores all the index words, synsets, exceptions of that POS.
     *
     * @throws JWNLException JWNLException
     * @throws SQLException  SQLException
     */
    public void insertData() throws JWNLException, SQLException {
        TIME = System.currentTimeMillis();
        for (POS pos : POS.getAllPOS()) {
            System.out.println("inserting data for pos " + pos);
            storeIndexWords(dictionary.getIndexWordIterator(pos));
            storeSynsets(dictionary.getSynsetIterator(pos));
            storeIndexWordSynsets();
            storeExceptions(dictionary.getExceptionIterator(pos));
            idToSynsetOffset.clear();
            synsetOffsetToId.clear();
            System.out.println("done inserting data for pos " + pos);
        }
    }

    /**
     * Store all the index words.
     *
     * @param itr - the index word iterator
     * @throws SQLException SQLException
     */
    protected void storeIndexWords(Iterator<IndexWord> itr) throws SQLException {
        System.out.println("storing index words");
        PreparedStatement iwStmt = connection.prepareStatement("INSERT INTO IndexWord VALUES(?,?,?)");
        int count = 0;
        while (itr.hasNext()) {
            if (count % 1000 == 0) {
                System.out.println("indexword: " + count);
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
        System.out.println("indexword: " + count);
        System.out.println("stored index words");
    }

    /**
     * Store all of the synsets in the database.
     *
     * @param itr itr
     * @throws SQLException SQLException
     * @throws JWNLException JWNLException
     */
    protected void storeSynsets(Iterator<Synset> itr) throws SQLException, JWNLException {
        PreparedStatement synsetStmt = connection.prepareStatement("INSERT INTO Synset VALUES(?,?,?,?,?)");
        PreparedStatement synsetWordStmt = getSynsetWordStmt();
        PreparedStatement synsetPointerStmt = connection.prepareStatement("INSERT INTO SynsetPointer VALUES(?,?,?,?,?,?,?)");
        PreparedStatement synsetVerbFrameStmt = connection.prepareStatement("INSERT INTO SynsetVerbFrame VALUES(?,?,?,?)");
        System.out.println("storing synsets");
        int count = 0;
        while (itr.hasNext()) {
            if (count % 1000 == 0) {
                System.out.println("synset: " + count);
            }
            count++;
            Synset synset = itr.next();
            int id = nextId();
            synsetOffsetToId.put(synset.getOffset(), id);
            synsetStmt.setInt(1, id);
            synsetStmt.setLong(2, synset.getOffset());
            synsetStmt.setString(3, synset.getPOS().getKey());
            synsetStmt.setBoolean(4, synset.isAdjectiveCluster());
            synsetStmt.setString(5, synset.getGloss());
            synsetStmt.execute();
            List<Word> words = synset.getWords();
            synsetWordStmt.setInt(2, id);
            synsetVerbFrameStmt.setInt(2, id);

            BitSet allWordFrames = synset.getVerbFrameFlags();
            synsetVerbFrameStmt.setInt(4, 0);//applicable to all words
            for (int i = allWordFrames.nextSetBit(0); i >= 0; i = allWordFrames.nextSetBit(i + 1)) {
                synsetVerbFrameStmt.setInt(1, nextId());
                synsetVerbFrameStmt.setInt(3, i);
                synsetVerbFrameStmt.execute();
            }

            for (Word word : words) {
                int wordId = nextId();
                synsetWordStmt.setInt(1, wordId);

                fillSynsetWordStmt(synsetWordStmt, synset, word);

                synsetWordStmt.execute();
                if (word instanceof Verb) {
                    synsetVerbFrameStmt.setInt(4, word.getIndex());
                    BitSet bits = ((Verb) word).getVerbFrameFlags();
                    for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
                        if (!allWordFrames.get(i)) {
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
        System.out.println("synset: " + count);
        System.out.println("stored synsets");
    }

    protected void fillSynsetWordStmt(PreparedStatement synsetWordStmt, Synset synset, Word word) throws SQLException {
        synsetWordStmt.setString(3, word.getLemma());
        synsetWordStmt.setInt(4, word.getIndex());
    }

    protected PreparedStatement getSynsetWordStmt() throws SQLException {
        return connection.prepareStatement("INSERT INTO SynsetWord VALUES(?,?,?,?)");
    }

    /**
     * Store the index word synsets.
     *
     * @throws SQLException SQLException
     */
    protected void storeIndexWordSynsets() throws SQLException {
        System.out.println("storing index word synsets");
        PreparedStatement iwsStmt = connection.prepareStatement("INSERT INTO IndexWordSynset VALUES(?,?,?)");
        for (Map.Entry<Integer, long[]> entry : idToSynsetOffset.entrySet()) {
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
        System.out.println("stored index word synsets");
    }


    /**
     * Store the exceptions file.
     *
     * @param itr iterator
     * @throws SQLException SQLException
     */
    protected void storeExceptions(Iterator<Exc> itr) throws SQLException {
        System.out.println("storing exceptions");
        PreparedStatement exStmt = connection.prepareStatement("INSERT INTO SynsetException VALUES(?,?,?,?)");
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
    }
}