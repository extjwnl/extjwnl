package net.didion.jwnl.dictionary.database;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.util.factory.Owned;
import net.didion.jwnl.util.factory.Param;

import java.util.Map;

/**
 * Database Manager that handles the extended database containing sense key and usage statistics.
 *
 * @author brett
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class SenseAndUsageDatabaseManager extends DatabaseManagerImpl implements Owned {

    /**
     * The SQL statement to grab a synset word.
     */
    protected static final String SENSE_SYNSET_WORD_SQL =
            "SELECT sw.word, sw.word_index, sw.sense_key, sw.usage_cnt " +
                    "FROM Synset s, SynsetWord sw " +
                    "WHERE s.synset_id = sw.synset_id AND s.pos = ? AND s.file_offset = ?";

    public SenseAndUsageDatabaseManager(Dictionary dictionary, Map<String, Param> params) throws JWNLException {
        super(dictionary, params);
    }

    public Query getSynsetWordQuery(POS pos, long offset) throws JWNLException {
        return createPOSOffsetQuery(pos, offset, SENSE_SYNSET_WORD_SQL);
    }
}