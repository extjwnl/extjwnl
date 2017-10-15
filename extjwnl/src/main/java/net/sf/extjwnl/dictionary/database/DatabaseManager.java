package net.sf.extjwnl.dictionary.database;

import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.util.factory.Owned;

import java.sql.SQLException;

/**
 * Interface for database managers.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface DatabaseManager extends Owned {
    Query getIndexWordSynsetsQuery(POS pos, String lemma) throws SQLException;

    Query getIndexWordLemmasQuery(POS pos) throws SQLException;

    Query getIndexWordLemmasQuery(POS pos, String substring) throws SQLException;

    Query getRandomIndexWordQuery(POS pos) throws SQLException;

    Query getSynsetQuery(POS pos, long offset) throws SQLException;

    Query getSynsetWordQuery(POS pos, long offset) throws SQLException;

    Query getPointerQuery(POS pos, long offset) throws SQLException;

    Query getVerbFrameQuery(POS pos, long offset) throws SQLException;

    Query getSynsetsQuery(POS pos) throws SQLException;

    Query getExceptionQuery(POS pos, String derivation) throws SQLException;

    Query getExceptionsQuery(POS pos) throws SQLException;

    /**
     * Closes the connections and frees associated resources.
     */
    void close();
}