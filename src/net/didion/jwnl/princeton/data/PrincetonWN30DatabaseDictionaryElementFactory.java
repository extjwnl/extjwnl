package net.didion.jwnl.princeton.data;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.*;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.util.factory.Param;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.BitSet;
import java.util.Map;

/**
 * Supports the wordnet 3.0 database, including sense key and usage count information.
 *
 * @author brett
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class PrincetonWN30DatabaseDictionaryElementFactory extends AbstractPrincetonDatabaseDictionaryElementFactory {

    public PrincetonWN30DatabaseDictionaryElementFactory(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary);
    }

    /**
     * Create a synset.
     *
     * @param pos        the part of speech
     * @param offset     the file offset
     * @param synsets    the result set
     * @param words      the words composing the synset
     * @param pointers   the pointers
     * @param verbFrames the verbFrames
     * @return Synset the created synset
     * @throws SQLException SQLException
     */
    public Synset createSynset(POS pos, long offset, ResultSet synsets, ResultSet words, ResultSet pointers, ResultSet verbFrames)
            throws SQLException, JWNLException {
        synsets.next();
        Synset synset = new Synset(dictionary, pos, offset);
        boolean isAdjectiveCluster = synsets.getBoolean(1);
        synset.setIsAdjectiveCluster(isAdjectiveCluster);

        String gloss = synsets.getString(2);
        synset.setGloss(gloss);

        while (words.next()) {
            String lemma = words.getString(1);
            int index = words.getInt(2);
            String senseKey = words.getString(3);
            int usageCnt = words.getInt(4);
            synset.getWords().add(createWord(synset, index, lemma, senseKey, usageCnt));
        }

        while (pointers.next()) {
            PointerType type = PointerType.getPointerTypeForKey(pointers.getString(1));
            long targetOffset = pointers.getLong(2);
            POS targetPOS = POS.getPOSForKey(pointers.getString(3));
            //int sourceIndex = pointers.getInt(4);
            int targetIndex = pointers.getInt(5);
            synset.getPointers().add(new Pointer(synset, type, targetPOS, targetOffset, targetIndex));
        }

        if (POS.VERB.equals(pos)) {
            BitSet vFrames = new BitSet();
            while (verbFrames.next()) {
                int frameNumber = verbFrames.getInt(1);
                int wordIndex = verbFrames.getInt(2);
                if (wordIndex > 0) {
                    ((MutableVerb) synset.getWords().get(wordIndex - 1)).setVerbFrameFlag(frameNumber);
                } else {
                    for (Word w : synset.getWords()) {
                        ((MutableVerb) w).setVerbFrameFlag(frameNumber);
                    }
                    vFrames.set(frameNumber);
                }
            }
            synset.setVerbFrameFlags(vFrames);
        }

        return synset;
    }

    /**
     * Creates a word object from.
     *
     * @param synset   - the synset this word belongs to
     * @param index    - the index of this word
     * @param lemma    - phrase definition
     * @param senseKey - the sense key
     * @param usageCnt - the tagged usage count
     * @return word
     */
    protected Word createWord(Synset synset, int index, String lemma, String senseKey, int usageCnt) {
        Word w;
        if (synset.getPOS().equals(POS.VERB)) {
            w = new MutableVerb(dictionary, synset, index, lemma);
        } else {
            w = new Word(dictionary, synset, index, lemma);

        }
        return w;
    }

}