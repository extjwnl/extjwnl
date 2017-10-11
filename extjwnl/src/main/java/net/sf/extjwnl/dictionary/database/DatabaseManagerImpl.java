package net.sf.extjwnl.dictionary.database;

import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.factory.Param;

import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/**
 * Database manager.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class DatabaseManagerImpl implements DatabaseManager {
    public static final String DRIVER = "driver";
    public static final String URL = "url";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    protected static final String LEMMA_FOR_INDEX_WORD_ID_SQL =
            "SELECT iw.lemma " +
                    "FROM indexword iw " +
                    "WHERE iw.pos = ? AND iw.index_word_id = ?";

    /**
     * SQL query for getting all synsets for an index word.
     */
    protected static final String SYNSET_IDS_FOR_INDEX_WORD_SQL =
            "SELECT syn.file_offset, iws.synset_id, syn.synset_id "
                    + "FROM indexwordsynset iws, indexword iw, synset syn "
                    + "WHERE iws.index_word_id = iw.index_word_id AND syn.synset_id = iws.synset_id AND iw.pos = ?  AND iw.lemma = ? ORDER BY iws.synset_rank ASC";

    protected static final String COUNT_INDEX_WORDS_SQL =
            "SELECT MIN(index_word_id), MAX(index_word_id) FROM indexword WHERE pos = ?";

    protected static final String ALL_LEMMAS_SQL =
            "SELECT lemma FROM indexword WHERE pos = ?";

    protected static final String ALL_LEMMAS_LIKE_SQL =
            "SELECT lemma FROM indexword WHERE pos = ? AND lemma LIKE ?";

    protected static final String SYNSET_SQL =
            "SELECT is_adj_cluster, gloss, lex_file_num FROM synset WHERE pos = ? AND file_offset = ?";

    protected static final String SYNSET_WORD_SQL =
            "SELECT sw.word, sw.word_index, sw.usage_cnt, sw.lex_id " +
                    "FROM synset s, synsetword sw " +
                    "WHERE s.synset_id = sw.synset_id AND s.pos = ? AND s.file_offset = ? " +
                    "ORDER BY sw.word_index";

    protected static final String SYNSET_POINTER_SQL =
            "SELECT sp.pointer_type, sp.target_offset, sp.target_pos, sp.source_index, sp.target_index " +
                    "FROM synset s, synsetpointer sp " +
                    "WHERE s.synset_id = sp.synset_id AND s.pos = ? AND s.file_offset = ?";

    protected static final String SYNSET_VERB_FRAME_SQL =
            "SELECT svf.frame_number, svf.word_index " +
                    "FROM synset s, synsetverbframe svf " +
                    "WHERE s.synset_id = svf.synset_id AND s.pos = ? AND s.file_offset = ?";

    protected static final String ALL_SYNSETS_SQL =
            "SELECT file_offset FROM synset WHERE pos = ?";

    protected static final String EXCEPTION_SQL =
            "SELECT base FROM exceptions WHERE pos = ? AND derivation = ?";

    protected static final String ALL_EXCEPTIONS_SQL =
            "SELECT derivation FROM exceptions WHERE pos = ?";

    protected static final Random rand = new Random();

    protected final ConnectionManager connectionManager;
    protected final Map<POS, MinMax> minMaxIds;
    protected final Dictionary dictionary;

    public DatabaseManagerImpl(Dictionary dictionary, Map<String, Param> params) throws SQLException {
        String driverClassName = params.containsKey(DRIVER) ? params.get(DRIVER).getValue() : null;
        String url = params.containsKey(URL) ? params.get(URL).getValue() : null;
        String userName = params.containsKey(USERNAME) ? params.get(USERNAME).getValue() : null;
        String password = params.containsKey(PASSWORD) ? params.get(PASSWORD).getValue() : null;
        connectionManager = new ConnectionManager(dictionary, driverClassName, url, userName, password);

        this.dictionary = dictionary;
        this.minMaxIds = new EnumMap<>(POS.class);
    }

    public Query getIndexWordSynsetsQuery(POS pos, String lemma) throws SQLException {
        return createPOSStringQuery(pos, lemma, SYNSET_IDS_FOR_INDEX_WORD_SQL);
    }

    public Query getIndexWordLemmasQuery(POS pos) throws SQLException {
        return createPOSQuery(pos, ALL_LEMMAS_SQL);
    }

    public Query getIndexWordLemmasQuery(POS pos, String substring) throws SQLException {
        return createPOSStringQuery(pos, "%" + substring + "%", ALL_LEMMAS_LIKE_SQL);
    }

    public synchronized Query getRandomIndexWordQuery(POS pos) throws SQLException {
        MinMax minMax = minMaxIds.get(pos);
        if (minMax == null) {
            Query query = createPOSQuery(pos, COUNT_INDEX_WORDS_SQL);
            try {
                query.execute();
                query.getResults().next();
                minMax = new MinMax(query.getResults().getInt(1), query.getResults().getInt(2));
                minMaxIds.put(pos, minMax);
            } finally {
                if (query != null) {
                    query.close();
                }
            }
        }
        int id = minMax.getMin() + rand.nextInt(minMax.getMax() - minMax.getMin());
        return createPOSIdQuery(pos, id, LEMMA_FOR_INDEX_WORD_ID_SQL);
    }

    private final static class MinMax {
        private final int min;
        private final int max;

        public MinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }
    }

    public Query getSynsetQuery(POS pos, long offset) throws SQLException {
        return createPOSOffsetQuery(pos, offset, SYNSET_SQL);
    }

    public Query getSynsetWordQuery(POS pos, long offset) throws SQLException {
        return createPOSOffsetQuery(pos, offset, SYNSET_WORD_SQL);
    }

    public Query getPointerQuery(POS pos, long offset) throws SQLException {
        return createPOSOffsetQuery(pos, offset, SYNSET_POINTER_SQL);
    }

    public Query getVerbFrameQuery(POS pos, long offset) throws SQLException {
        return createPOSOffsetQuery(pos, offset, SYNSET_VERB_FRAME_SQL);
    }

    public Query getSynsetsQuery(POS pos) throws SQLException {
        return createPOSQuery(pos, ALL_SYNSETS_SQL);
    }

    public Query getExceptionQuery(POS pos, String derivation) throws SQLException {
        return createPOSStringQuery(pos, derivation, EXCEPTION_SQL);
    }

    public Query getExceptionsQuery(POS pos) throws SQLException {
        return createPOSQuery(pos, ALL_EXCEPTIONS_SQL);
    }

    @Override
    public void close() {
        minMaxIds.clear();
        connectionManager.close();
    }

    protected Query createPOSQuery(POS pos, String sql) throws SQLException {
        Query query = null;
        try {
            query = connectionManager.getQuery(sql);
            query.getStatement().setString(1, pos.getKey());
            return query;
        } catch (SQLException e) {
            if (query != null) {
                query.close();
            }
            throw e;
        }
    }

    protected Query createPOSStringQuery(POS pos, String str, String sql) throws SQLException {
        Query query = null;
        try {
            query = connectionManager.getQuery(sql);
            query.getStatement().setString(1, pos.getKey());
            query.getStatement().setString(2, str);
            return query;
        } catch (SQLException e) {
            if (query != null) {
                query.close();
            }
            throw e;
        }
    }

    protected Query createPOSOffsetQuery(POS pos, long offset, String sql) throws SQLException {
        Query query = null;
        try {
            query = connectionManager.getQuery(sql);
            query.getStatement().setString(1, pos.getKey());
            query.getStatement().setLong(2, offset);
            return query;
        } catch (SQLException e) {
            if (query != null) {
                query.close();
            }
            throw e;
        }
    }

    protected Query createPOSIdQuery(POS pos, int id, String sql) throws SQLException {
        Query query = null;
        try {
            query = connectionManager.getQuery(sql);
            query.getStatement().setString(1, pos.getKey());
            query.getStatement().setInt(2, id);
            return query;
        } catch (SQLException e) {
            if (query != null) {
                query.close();
            }
            throw e;
        }
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        throw new UnsupportedOperationException();
    }
}