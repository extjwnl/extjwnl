package net.didion.jwnl.princeton.data;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.BitSet;
import java.util.Map;

/**
 * Supports the wordnet 3.0 database, including sense key and usage count information.
 *
 * @author brett
 */
public class PrincetonWN30DatabaseDictionaryElementFactory extends AbstractPrincetonDatabaseDictionaryElementFactory {

    /**
     * Initialize the factory.
     */
    public PrincetonWN30DatabaseDictionaryElementFactory() {

    }

    /**
     * Creates a blank factory, takes no parameters.
     */
    public Object create(Map params) throws JWNLException {
        return new PrincetonWN30DatabaseDictionaryElementFactory();
    }

    /**
     * Create a synset.
     *
     * @param pos        - the part of speech
     * @param offset     - the file offset
     * @param synsets    - the result set
     * @param words      - the words composing the synset
     * @param pointers   - the pointers
     * @param verbFrames - the verbFrames
     * @return Synset - the created synset
     * @throws SQLException
     */
    public Synset createSynset(
            POS pos, long offset, ResultSet synsets, ResultSet words, ResultSet pointers, ResultSet verbFrames)
            throws SQLException {
        synsets.next();
        Synset synset = new Synset();
        boolean isAdjectiveCluster = synsets.getBoolean(1);
        synset.setIsAdjectiveCluster(isAdjectiveCluster);

        String gloss = synsets.getString(2);
        synset.setGloss(gloss);

        synset.setPOS(pos);

        while (words.next()) {
            String lemma = words.getString(1);
            int index = words.getInt(2);
            String senseKey = words.getString(3);
            int usageCnt = words.getInt(4);
            synset.addWord(createWord(synset, index, lemma, senseKey, usageCnt));
        }

        while (pointers.next()) {
            PointerType type = PointerType.getPointerTypeForKey(pointers.getString(1));
            long targetOffset = pointers.getLong(2);
            POS targetPOS = POS.getPOSForKey(pointers.getString(3));
            int sourceIndex = pointers.getInt(4);
            int targetIndex = pointers.getInt(5);
            synset.addPointer(new Pointer(synset, type, targetPOS, targetOffset, targetIndex));
        }

        while (verbFrames.next()) {
            int frameNumber = verbFrames.getInt(1);
            int wordIndex = verbFrames.getInt(2);
            if (wordIndex > 0) {
                ((MutableVerb) synset.getWord(wordIndex - 1)).setVerbFrameFlag(frameNumber);
            } else {
                for (Word w : synset.getWords()) {
                    ((MutableVerb) w).setVerbFrameFlag(frameNumber);
                }
            }
        }

        BitSet verbFrameBits = new BitSet();
        for (Word word : synset.getWords()) {
            if (word instanceof Verb) {
                verbFrameBits.or(((Verb) word).getVerbFrameFlags());
            }
        }
        synset.setVerbFrameFlags(verbFrameBits);

        return synset;
    }

    /**
     * Creates a word object from.
     *
     * @param synset   - the synset this word belongs to
     * @param index    - the index of this word
     * @param lemma    - phrase defintion
     * @param senseKey - the sense key
     * @param usageCnt - the tagged usage count
     * @return word
     */
    protected Word createWord(Synset synset, int index, String lemma, String senseKey, int usageCnt) {
        Word w;
        if (synset.getPOS().equals(POS.VERB)) {
            w = new MutableVerb(synset, index, lemma);
        } else {
            w = new Word(synset, index, lemma);

        }
        return w;
    }

}
