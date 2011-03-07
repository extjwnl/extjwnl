package net.sf.extjwnl.princeton.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.dictionary.Dictionary;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

/**
 * Base class for element factories.
 *
 * @author John Didion <jdidion@users.sourceforge.net>
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public abstract class AbstractPrincetonDatabaseDictionaryElementFactory implements DatabaseDictionaryElementFactory {

    protected Dictionary dictionary;

    public AbstractPrincetonDatabaseDictionaryElementFactory(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public IndexWord createIndexWord(POS pos, String lemma, ResultSet rs) throws SQLException, JWNLException {
        List<Long> offsets = new ArrayList<Long>();
        while (rs.next()) {
            offsets.add(rs.getLong(1));
        }
        if (offsets.isEmpty()) {
            return null;
        }

        long[] offsetArray = new long[offsets.size()];
        Iterator itr = offsets.iterator();
        for (int i = 0; itr.hasNext(); i++) {
            offsetArray[i] = (Long) itr.next();
        }

        return new IndexWord(dictionary, lemma, pos, offsetArray);
    }

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
            Word word = createWord(synset, index, lemma);
            word.setUseCount(words.getInt(3));
            synset.getWords().add(word);
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

    protected Word createWord(Synset synset, int index, String lemma) {
        if (synset.getPOS().equals(POS.VERB)) {
            return new MutableVerb(dictionary, synset, index, lemma);
        } else {
            return new Word(dictionary, synset, index, lemma);
        }
    }

    public Exc createExc(POS pos, String derivation, ResultSet rs) throws SQLException, JWNLException {
        List<String> exceptions = new ArrayList<String>();
        while (rs.next()) {
            exceptions.add(rs.getString(1));
        }
        return new Exc(dictionary, pos, derivation, exceptions);
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }
}