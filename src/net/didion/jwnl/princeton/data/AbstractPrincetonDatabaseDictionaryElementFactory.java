package net.didion.jwnl.princeton.data;

import net.didion.jwnl.data.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractPrincetonDatabaseDictionaryElementFactory implements DatabaseDictionaryElementFactory {
    public IndexWord createIndexWord(POS pos, String lemma, ResultSet rs) throws SQLException {
        List offsets = new ArrayList();
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

        return new IndexWord(lemma, pos, offsetArray);
    }

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
            synset.addWord(createWord(synset, index, lemma));
        }

        while (pointers.next()) {
            PointerType type = PointerType.getPointerTypeForKey(pointers.getString(1));
            long targetOffset = pointers.getLong(2);
            POS targetPOS = POS.getPOSForKey(pointers.getString(3));
            int sourceIndex = pointers.getInt(4);
            int targetIndex = pointers.getInt(5);
            synset.addPointer(new Pointer(synset, type, targetPOS, targetOffset, targetIndex));
        }

        BitSet vFrames = new BitSet();
        while (verbFrames.next()) {
            int frameNumber = verbFrames.getInt(1);
            int wordIndex = verbFrames.getInt(2);
            if (wordIndex > 0) {
                ((MutableVerb) synset.getWord(wordIndex - 1)).setVerbFrameFlag(frameNumber);
            } else {
                for (Word w : synset.getWords()) {
                    ((MutableVerb) w).setVerbFrameFlag(frameNumber);
                }
                vFrames.set(frameNumber);
            }
        }

        return synset;
    }

    protected Word createWord(Synset synset, int index, String lemma) {
        if (synset.getPOS().equals(POS.VERB)) {
            return new MutableVerb(synset, index, lemma);
        } else {
            return new Word(synset, index, lemma);
        }
    }

    public Exc createExc(POS pos, String derivation, ResultSet rs) throws SQLException {
        List exceptions = new ArrayList();
        while (rs.next()) {
            exceptions.add(rs.getString(1));
        }
        return new Exc(pos, derivation, exceptions);
    }
}