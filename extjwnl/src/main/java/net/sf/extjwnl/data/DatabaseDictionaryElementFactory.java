package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Factory interface for creating WordNet objects from a database.
 *
 * @author Brett Walenz (bwalenz@users.sourceforge.net)
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface DatabaseDictionaryElementFactory extends DictionaryElementFactory {

    /**
     * Create an IndexWord from a row in the database.
     *
     * @param pos   part of speech
     * @param lemma lemma
     * @param rs    result set
     * @return index word
     * @throws SQLException  SQLException
     * @throws JWNLException JWNLException
     */
    IndexWord createIndexWord(POS pos, String lemma, ResultSet rs) throws SQLException, JWNLException;


    /**
     * Create a Synset from a row in the database.
     *
     * @param pos        part of speech
     * @param offset     synset offset
     * @param synset     synset result set
     * @param words      words result set
     * @param pointers   pointers result set
     * @param verbFrames verb frames result set
     * @return synset
     * @throws SQLException  SQLException
     * @throws JWNLException JWNLException
     */
    Synset createSynset(
            POS pos, long offset, ResultSet synset, ResultSet words, ResultSet pointers, ResultSet verbFrames)
            throws SQLException, JWNLException;


    /**
     * Create an Exc from a row in the database.
     *
     * @param pos        the part of speech
     * @param derivation derivation
     * @param rs         result set
     * @return exception
     * @throws SQLException  SQLException
     * @throws JWNLException JWNLException
     */
    Exc createExc(POS pos, String derivation, ResultSet rs) throws SQLException, JWNLException;
}