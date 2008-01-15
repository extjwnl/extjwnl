package net.didion.jwnl.princeton.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.SynsetProxy;
import net.didion.jwnl.data.Verb;
import net.didion.jwnl.data.Word;

/**
 * Supports the wordnet 3.0 database, including sense key and usage count information. 
 * @author brett
 *
 */
public class PrincetonWN30DatabaseDictionaryElementFactory extends AbstractPrincetonDatabaseDictionaryElementFactory {

    /**
     * Initialize the factory. 
     *
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
     * @param pos - the part of speech
     * @param offset - the file offset
     * @param synset - the result set
     * @param words - the words composing the synset
     * @param pointers - the pointers
     * @param verbFrames - the verbFrames
     * @return Synset - the created synset
     * @throws SQLException
     */
    public Synset createSynset(
            POS pos, long offset, ResultSet synset, ResultSet words, ResultSet pointers, ResultSet verbFrames)
            throws SQLException {
        synset.next();
        boolean isAdjectiveCluster = synset.getBoolean(1);
        String gloss = synset.getString(2);

        SynsetProxy proxy = new SynsetProxy(pos);

        List wordList = new ArrayList();
        while (words.next()) {
            String lemma = words.getString(1);
            int index = words.getInt(2);
            String senseKey = words.getString(3);
            int usageCnt = words.getInt(4);
            wordList.add(createWord(proxy, index, lemma, senseKey, usageCnt));
        }

        List pointerList = new ArrayList();
        while (pointers.next()) {
            PointerType type = PointerType.getPointerTypeForKey(pointers.getString(1));
            long targetOffset = pointers.getLong(2);
            POS targetPOS = POS.getPOSForKey(pointers.getString(3));
            int sourceIndex = pointers.getInt(4);
            int targetIndex = pointers.getInt(5);
            pointerList.add(new Pointer(proxy, sourceIndex, type, targetPOS, targetOffset, targetIndex));
        }

        while (verbFrames.next()) {
            int frameNumber = verbFrames.getInt(1);
            int wordIndex = verbFrames.getInt(2);
            if (wordIndex > 0) {
                ((MutableVerb) wordList.get(wordIndex - 1)).setVerbFrameFlag(frameNumber);
            } else {
                for (Iterator itr = wordList.iterator(); itr.hasNext();) {
                    ((MutableVerb) itr.next()).setVerbFrameFlag(frameNumber);
                }
            }
        }

        BitSet verbFrameBits = new BitSet();
        for (Iterator itr = wordList.iterator(); itr.hasNext();) {
            Word word = (Word) itr.next();
            if (word instanceof Verb) {
                verbFrameBits.or(((Verb) word).getVerbFrameFlags());
            }
        }

        proxy.setSource(new Synset(
                pos, offset, (Word[]) wordList.toArray(new Word[wordList.size()]),
                (Pointer[]) pointerList.toArray(new Pointer[pointerList.size()]),
                gloss, verbFrameBits, isAdjectiveCluster));

        return proxy;
    }
    
    /**
     * Creates a word object from.
     * @param synset - the synset this word belongs to
     * @param index - the index of this word
     * @param lemma - phrase defintion
     * @param senseKey - the sense key
     * @param usageCnt - the tagged usage count
     * @return word
     */
    protected Word createWord(Synset synset, int index, String lemma, String senseKey, int usageCnt) {
        Word w = null;
        if (synset.getPOS().equals(POS.VERB)) {
            w = new MutableVerb(synset, index, lemma);
        } else {
            w = new Word(synset, index, lemma);
            
        }
        w.setSenseKey(senseKey);
        w.setUsageCount(usageCnt);
        
        return w;
    }
    
}
