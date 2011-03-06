package net.sf.extjwnl.utilities;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.database.ConnectionManager;
import net.sf.extjwnl.util.TokenizerParser;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * DictionaryToDatabase is used to transfer a WordNet file database into an actual database structure.
 *
 * @author brett
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class DictionaryToDatabaseWithUsageCount extends DictionaryToDatabase {

    /**
     * Maps the usage. The key is 'offset:lemma', the object[] contains
     * the sense key (string) and the usage count (integer).
     */
    protected Map<String, Object[]> usageMap;

    /**
     * Run the program, requires 4 arguments. See DictionaryToDatabase.txt for more documentation.
     *
     * @param args args
     */
    public static void main(String args[]) {
        if (args.length < 4) {
            System.out.println("DictionaryToDatabaseWithUsageCount <property file> <index.sense file> <create tables script> <driver class> <connection url> [username [password]]");
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
            String indexSenseFileName = args[1];
            String scriptFileName = args[2];
            ConnectionManager mgr = new ConnectionManager(args[3], args[4], args.length <= 5 ? null : args[5], args.length <= 6 ? null : args[6]);
            conn = mgr.getConnection();
            DictionaryToDatabaseWithUsageCount d2d = new DictionaryToDatabaseWithUsageCount(dictionary, conn);
            d2d.loadSenseKeyAndUsage(indexSenseFileName);
            d2d.createTables(scriptFileName);
            d2d.insertData();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public DictionaryToDatabaseWithUsageCount(Dictionary dictionary, Connection conn) {
        super(dictionary, conn);
        usageMap = new HashMap<String, Object[]>();
    }

    @Override
    protected PreparedStatement getSynsetWordStmt() throws SQLException {
        return connection.prepareStatement("INSERT INTO SynsetWord VALUES(?,?,?,?,?,?)");
    }

    @Override
    protected void fillSynsetWordStmt(PreparedStatement synsetWordStmt, Synset synset, Word word) throws SQLException {
        super.fillSynsetWordStmt(synsetWordStmt, synset, word);
        String synsetString = synset.getOffset() + ":" + word.getLemma();
        Object[] arr = usageMap.get(synsetString);
        String senseKey = "";
        int usageCnt = 0;
        if (arr != null) {
            senseKey = (String) arr[0];
            usageCnt = (Integer) arr[1];
        }

        synsetWordStmt.setString(5, senseKey);
        synsetWordStmt.setInt(6, usageCnt);
    }

    /**
     * loads the sense key usage from a file.
     *
     * @param filename usage file
     * @throws SQLException SQLException
     */
    private void loadSenseKeyAndUsage(String filename) throws SQLException {
        System.out.println("loading sense key usage");
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            int count = 0;
            while (in.ready()) {
                String indexLine = in.readLine();
                TokenizerParser tokenizer = new TokenizerParser(indexLine, " ");
                String senseKey = tokenizer.nextToken();
                String[] lemmaKey = senseKey.split("%");
                String lemma = lemmaKey[0];
                long ofs = tokenizer.nextLong();
                tokenizer.nextInt();
                String synsetString = ofs + ":" + lemma;
                if (count++ % 1000 == 0) {
                    System.out.println("sense key and usage: " + count);
                }
                String senseCount = tokenizer.nextToken();
                String[] sc;

                if (dictionary.getVersion().getNumber() < 2.1 && JWNL.getOS().equals(JWNL.WINDOWS)) {
                    sc = senseCount.split("\\r\\n");
                } else {
                    sc = senseCount.split("\\n");
                }
                if (sc != null) {
                    int cnt = Integer.parseInt(sc[0]);
                    Object[] arr = new Object[2];
                    arr[0] = senseKey;
                    arr[1] = cnt;
                    usageMap.put(synsetString, arr);
                }
            }
            System.out.println("loaded sense key usage");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}