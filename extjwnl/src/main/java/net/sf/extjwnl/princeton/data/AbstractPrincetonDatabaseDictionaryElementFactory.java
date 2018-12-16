package net.sf.extjwnl.princeton.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.factory.Param;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

/**
 * Base class for database element factories.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class AbstractPrincetonDatabaseDictionaryElementFactory extends AbstractDictionaryElementFactory implements DatabaseDictionaryElementFactory {

    public AbstractPrincetonDatabaseDictionaryElementFactory(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary, params);
    }

    public IndexWord createIndexWord(POS pos, String lemma, ResultSet rs) throws SQLException, JWNLException {
        List<Long> offsets = new ArrayList<>();
        while (rs.next()) {
            offsets.add(rs.getLong(1));
        }
        if (offsets.isEmpty()) {
            return null;
        }

        long[] offsetArray = new long[offsets.size()];
        for (int i = 0; i < offsets.size(); i++) {
            offsetArray[i] = offsets.get(i);
        }

        return new IndexWord(dictionary, stringCache.replace(lemma), pos, offsetArray);
    }

    public Synset createSynset(POS pos, long offset, ResultSet synsets, ResultSet words, ResultSet pointers, ResultSet verbFrames)
            throws SQLException, JWNLException {
        if (synsets.next()) {
            Synset synset;
            if (POS.VERB == pos) {
                synset = new VerbSynset(dictionary, offset);
            } else if (POS.ADJECTIVE == pos) {
                synset = new AdjectiveSynset(dictionary, offset);
            } else {
                synset = new Synset(dictionary, pos, offset);
            }

            if (POS.ADJECTIVE == pos) {
                synset.setIsAdjectiveCluster(synsets.getBoolean(1));
            }

            String gloss = synsets.getString(2);
            synset.setGloss(gloss);

            long lexFileNum = synsets.getLong(3);
            synset.setLexFileNum(lexFileNum);

            while (words.next()) {
                String lemma = stringCache.replace(words.getString(1));
                Word word = createWord(synset, lemma);
                word.setUseCount(words.getInt(3));
                word.setLexId(words.getInt(4));
                synset.getWords().add(word);
            }
            if (synset.getWords() instanceof ArrayList) {
                ((ArrayList) synset.getWords()).trimToSize();
            }

            while (pointers.next()) {
                PointerType type = PointerType.getPointerTypeForKey(pointers.getString(1));
                long targetOffset = pointers.getLong(2);
                POS targetPOS = POS.getPOSForKey(pointers.getString(3));
                //int sourceIndex = pointers.getInt(4);
                int targetIndex = pointers.getInt(5);
                synset.getPointers().add(new Pointer(synset, type, targetPOS, targetOffset, targetIndex));
            }
            if (synset.getPointers() instanceof ArrayList) {
                ((ArrayList) synset.getPointers()).trimToSize();
            }

            if (POS.VERB == pos) {
                BitSet vFrames = new BitSet();
                while (verbFrames.next()) {
                    int frameNumber = verbFrames.getInt(1);
                    int wordIndex = verbFrames.getInt(2);
                    initVerbFrameFlags(synset, vFrames, frameNumber, wordIndex);
                }
                synset.setVerbFrameFlags(vFrames);
            }
            return synset;
        } else {
            return null;
        }
    }

    public Exc createExc(POS pos, String derivation, ResultSet rs) throws SQLException, JWNLException {
        ArrayList<String> exceptions = new ArrayList<>();
        while (rs.next()) {
            exceptions.add(stringCache.replace(rs.getString(1)));
        }
        if (0 < exceptions.size()) {
            exceptions.trimToSize();
            return new Exc(dictionary, pos, stringCache.replace(derivation), exceptions);
        } else {
            return null;
        }
    }
}