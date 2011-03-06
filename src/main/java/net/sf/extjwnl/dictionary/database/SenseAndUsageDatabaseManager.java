package net.sf.extjwnl.dictionary.database;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.factory.Owned;
import net.sf.extjwnl.util.factory.Param;

import java.util.Map;

/**
 * Database Manager that handles the extended database containing sense key and usage statistics.
 *
 * @author Brett Walenz <bwalenz@users.sourceforge.net>
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
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