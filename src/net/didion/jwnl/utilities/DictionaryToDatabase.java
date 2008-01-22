
package net.didion.jwnl.utilities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.Exc;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Verb;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.AbstractCachingDictionary;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.dictionary.database.ConnectionManager;
import net.didion.jwnl.util.MessageLog;
import net.didion.jwnl.util.MessageLogLevel;

/**
 * DictionaryToDatabase is used to transfer a WordNet file database into an actual
 * database structure. 
 * @author brett
 *
 */
public class DictionaryToDatabase
{

    /**
     * Our message log. 
     */
    private static final MessageLog LOG;

    private static int INTERNAL_ID = 0;
    private static long TIME = 0L;
    
    /**
     * The database connection. 
     */
    private Connection connection;
    /**
     * Mapping of database id's to synset offset id's. 1 to 1.
     */
    private Map idToSynsetOffset;
    
    /**
     * Mapping of synset offset id's to database id's. 1:1.
     */
    private Map synsetOffsetToId;
    
    
    
    /**
     * Run the program, requires 4 arguments. See DictionaryToDatabase.txt for more documentation. 
     * @param args
     */
    public static void main(String args[])
    {
        if(args.length < 4)
        {
            System.out.println("java net.didion.jwnl.utilities.DictionaryToDatabase <property file> <create tables script> <driver class> <connection url> [username [password]]");
            System.exit(-1);
        }
        try
        {
            JWNL.initialize(new FileInputStream(args[0]));
            
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            System.exit(-1);
        }
        Connection conn = null;
        
        try
        {
            String scriptFileName = args[1];
            ConnectionManager mgr = new ConnectionManager(args[2], args[3], args.length <= 4 ? null : args[4], args.length <= 5 ? null : args[5]);
            conn = mgr.getConnection();
            DictionaryToDatabase d2d = new DictionaryToDatabase(conn);
            d2d.createTables(scriptFileName);
            d2d.insertData();
            
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
        finally
        {
            if(conn != null)
                try
                {
                    conn.close();
                }
                catch(SQLException ex) {
                    ex.printStackTrace();
                }
        }
    }

    private static synchronized int nextId()
    {
        INTERNAL_ID++;
        if(LOG.isLevelEnabled(MessageLogLevel.DEBUG) && INTERNAL_ID % 1000 == 0)
        {
            long temp = System.currentTimeMillis();
            LOG.log(MessageLogLevel.DEBUG, "inserted " + INTERNAL_ID + "th entry");
            LOG.log(MessageLogLevel.DEBUG, "free memory: " + Runtime.getRuntime().freeMemory());
            LOG.log(MessageLogLevel.DEBUG, "time: " + (temp - TIME));
            TIME = System.currentTimeMillis();
        }
        return INTERNAL_ID;
    }

    /**
     * Create a new DictionaryToDatabase with a database connection. JWNL already initialized.
     * @param conn - the database connection
     */
    public DictionaryToDatabase(Connection conn)
    {
        idToSynsetOffset = new HashMap();
        synsetOffsetToId = new HashMap();
        connection = conn;
        ((AbstractCachingDictionary)Dictionary.getInstance()).setCachingEnabled(false);
    }

    /**
     * Create the database tables. 
     * @param scriptFilePath - the sql script filename
     * @throws IOException
     * @throws SQLException
     */
    public void createTables(String scriptFilePath)
        throws IOException, SQLException
    {
        LOG.log(MessageLogLevel.INFO, "creating tables");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(scriptFilePath)));
        StringBuffer buf = new StringBuffer();
        for(String line = reader.readLine(); line != null; line = reader.readLine())
        {
            line = line.trim();
            if(line.length() <= 0)
                continue;
            buf.append(line);
            if(line.endsWith(";"))
            {
                System.out.println(buf.toString());
                connection.prepareStatement(buf.toString()).execute();
                buf = new StringBuffer();
            } else
            {
                buf.append(" ");
            }
        }

        LOG.log(MessageLogLevel.INFO, "creating tables");
    }

    /**
     * Inserts the data into the database. Iterates through the various POS, 
     * then stores all the index words, synsets, exceptions of that POS. 
     * @throws Exception
     */
    public void insertData()
        throws Exception
    {
        TIME = System.currentTimeMillis();
        POS pos;
        for(Iterator posItr = POS.getAllPOS().iterator(); posItr.hasNext(); LOG.log(MessageLogLevel.INFO, "done inserting data for pos " + pos))
        {
            pos = (POS)posItr.next();
            LOG.log(MessageLogLevel.INFO, "inserting data for pos " + pos);
            storeIndexWords(Dictionary.getInstance().getIndexWordIterator(pos));
            storeSynsets(Dictionary.getInstance().getSynsetIterator(pos));
            storeIndexWordSynsets();
            storeExceptions(Dictionary.getInstance().getExceptionIterator(pos));
            idToSynsetOffset.clear();
            synsetOffsetToId.clear();
        }
   
    }

    /**
     * Store all the index words. 
     * @param itr - the part of speech iterator 
     * @throws SQLException
     */
    private void storeIndexWords(Iterator itr)
        throws SQLException
    {
        LOG.log(MessageLogLevel.INFO, "storing index words");
        PreparedStatement iwStmt = connection.prepareStatement("INSERT INTO IndexWord VALUES(?,?,?)");
        int count = 0;
        do
        {
            if(!itr.hasNext()) {
                break;
            }
            IndexWord iw = (IndexWord)itr.next();
            int id = nextId();
            iwStmt.setInt(1, id);
            iwStmt.setString(2, iw.getLemma());
            iwStmt.setString(3, iw.getPOS().getKey());
            iwStmt.execute();
            idToSynsetOffset.put(new Integer(id), iw.getSynsetOffsets());
            if(count++ % 1000 == 0)
                System.out.println(count);
        } while(true);
    }

    /**
     * Store all of the synsets in the database. 
     * @param itr
     * @throws SQLException
     */
    private void storeSynsets(Iterator itr)
        throws SQLException
    {
        PreparedStatement synsetStmt = connection.prepareStatement("INSERT INTO Synset VALUES(?,?,?,?,?)");
        PreparedStatement synsetWordStmt = connection.prepareStatement("INSERT INTO SynsetWord VALUES(?,?,?,?)");
        PreparedStatement synsetPointerStmt = connection.prepareStatement("INSERT INTO SynsetPointer VALUES(?,?,?,?,?,?,?)");
        PreparedStatement synsetVerbFrameStmt = connection.prepareStatement("INSERT INTO SynsetVerbFrame VALUES(?,?,?,?)");
        LOG.log(MessageLogLevel.INFO, "storing synsets");
        int count = 0;
        while(itr.hasNext()) 
        {
            if(count++ % 1000 == 0)
                System.out.println("synset: " + count);
            Synset synset = (Synset)itr.next();
            int id = nextId();
            synsetOffsetToId.put(new Long(synset.getOffset()), new Integer(id));
            synsetStmt.setInt(1, id);
            synsetStmt.setLong(2, synset.getOffset());
            synsetStmt.setString(3, synset.getPOS().getKey());
            synsetStmt.setBoolean(4, synset.isAdjectiveCluster());
            synsetStmt.setString(5, synset.getGloss());
            synsetStmt.execute();
            Word words[] = synset.getWords();
            synsetWordStmt.setInt(2, id);
            synsetVerbFrameStmt.setInt(2, id);
            for(int i = 0; i < words.length; i++)
            {
                int wordId = nextId();
               
                synsetWordStmt.setInt(1, wordId);
                synsetWordStmt.setString(3, words[i].getLemma());
                synsetWordStmt.setInt(4, words[i].getIndex());
                
                
                synsetWordStmt.execute();
                if(!(words[i] instanceof Verb))
                    continue;
                synsetVerbFrameStmt.setInt(4, words[i].getIndex());
                int flags[] = ((Verb)words[i]).getVerbFrameIndicies();
                for(int j = 0; j < flags.length; j++)
                {
                    synsetVerbFrameStmt.setInt(1, nextId());
                    synsetVerbFrameStmt.setInt(3, flags[j]);
                    synsetVerbFrameStmt.execute();
                }

            }

            Pointer pointers[] = synset.getPointers();
            synsetPointerStmt.setInt(2, id);
            int i = 0;
            while(i < pointers.length) 
            {
                synsetPointerStmt.setInt(1, nextId());
                synsetPointerStmt.setString(3, pointers[i].getType().getKey());
                synsetPointerStmt.setLong(4, pointers[i].getTargetOffset());
                synsetPointerStmt.setString(5, pointers[i].getTargetPOS().getKey());
                synsetPointerStmt.setInt(6, pointers[i].getSourceIndex());
                synsetPointerStmt.setInt(7, pointers[i].getTargetIndex());
                synsetPointerStmt.execute();
                i++;
            }
        }
    }

    /**
     * Store the index word synsets.
     * @throws SQLException
     */
    private void storeIndexWordSynsets()
        throws SQLException
    {
        LOG.log(MessageLogLevel.INFO, "storing index word synsets");
        PreparedStatement iwsStmt = connection.prepareStatement("INSERT INTO IndexWordSynset VALUES(?,?,?)");
        for(Iterator itr = idToSynsetOffset.entrySet().iterator(); itr.hasNext();)
        {
            java.util.Map.Entry entry = (java.util.Map.Entry)itr.next();
            int iwId = ((Integer)entry.getKey()).intValue();
            iwsStmt.setInt(2, iwId);
            long offsets[] = (long[])entry.getValue();
            int i = 0;
            while(i < offsets.length) 
            {
                Integer offset = (Integer)synsetOffsetToId.get(new Long(offsets[i]));
                int synsetId = offset.intValue();
                iwsStmt.setInt(1, nextId());
                iwsStmt.setLong(3, synsetId);
                iwsStmt.execute();
                i++;
            }
        }

    }
    
   
    /**
     * Store the exceptions file. 
     * @param itr
     * @throws SQLException
     */
    private void storeExceptions(Iterator itr)
        throws SQLException
    {
        LOG.log(MessageLogLevel.INFO, "storing exceptions");
        PreparedStatement exStmt = connection.prepareStatement("INSERT INTO SynsetException VALUES(?,?,?,?)");
        while(itr.hasNext()) 
        {
            Exc exc = (Exc)itr.next();
            exStmt.setString(4, exc.getLemma());
            Iterator excItr = exc.getExceptions().iterator();
            while(excItr.hasNext()) 
            {
                exStmt.setInt(1, nextId());
                exStmt.setString(2, exc.getPOS().getKey());
                exStmt.setString(3, (String)excItr.next());
                exStmt.execute();
            }
        }
    }

    static 
    {
        LOG = new MessageLog(net.didion.jwnl.utilities.DictionaryToDatabase.class);
    }
}
