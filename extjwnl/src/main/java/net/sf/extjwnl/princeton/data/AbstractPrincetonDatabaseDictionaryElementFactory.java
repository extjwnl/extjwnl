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
 * Base class for database element factories.
 *
 * @author John Didion <jdidion@didion.net>
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public abstract class AbstractPrincetonDatabaseDictionaryElementFactory extends AbstractPrincetonDictionaryElementFactory implements DatabaseDictionaryElementFactory {

    public AbstractPrincetonDatabaseDictionaryElementFactory(Dictionary dictionary) {
        super(dictionary);
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
        Iterator<Long> itr = offsets.iterator();
        for (int i = 0; itr.hasNext(); i++) {
            offsetArray[i] = itr.next();
        }

        return new IndexWord(dictionary, lemma, pos, offsetArray);
    }

    public Synset createSynset(POS pos, long offset, ResultSet synsets, ResultSet words, ResultSet pointers, ResultSet verbFrames)
            throws SQLException, JWNLException {
        if (synsets.next()) {
            Synset synset = new Synset(dictionary, pos, offset);
            boolean isAdjectiveCluster = synsets.getBoolean(1);
            synset.setIsAdjectiveCluster(isAdjectiveCluster);

            String gloss = synsets.getString(2);
            synset.setGloss(gloss);

            long lexFileNum = synsets.getLong(3);
            synset.setLexFileNum(lexFileNum);

            while (words.next()) {
                String lemma = words.getString(1);
                int index = words.getInt(2);
                Word word = createWord(synset, index, lemma);
                word.setUseCount(words.getInt(3));
                word.setLexId(words.getLong(4));
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

            if (POS.VERB == pos) {
                BitSet vFrames = new BitSet();
                while (verbFrames.next()) {
                    int frameNumber = verbFrames.getInt(1);
                    int wordIndex = verbFrames.getInt(2);
                    if (wordIndex > 0) {
                        ((Verb) synset.getWords().get(wordIndex - 1)).getVerbFrameFlags().set(frameNumber);
                    } else {
                        for (Word w : synset.getWords()) {
                            ((Verb) w).getVerbFrameFlags().set(frameNumber);
                        }
                        vFrames.set(frameNumber);
                    }
                }
                synset.setVerbFrameFlags(vFrames);
            }
            return synset;
        } else {
            return null;
        }
    }

    public Exc createExc(POS pos, String derivation, ResultSet rs) throws SQLException, JWNLException {
        List<String> exceptions = new ArrayList<String>();
        while (rs.next()) {
            exceptions.add(rs.getString(1));
        }
        if (0 < exceptions.size()) {
            return new Exc(dictionary, pos, derivation, exceptions);
        } else {
            return null;
        }
    }
}