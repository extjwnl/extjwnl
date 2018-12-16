package net.sf.extjwnl.princeton.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.CharSequenceParser;
import net.sf.extjwnl.util.CharSequenceTokenizer;
import net.sf.extjwnl.util.factory.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Map;

/**
 * <code>FileDictionaryElementFactory</code> that parses lines from the dictionary files distributed by the
 * WordNet team at Princeton's Cognitive Science department.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class AbstractPrincetonFileDictionaryElementFactory extends AbstractDictionaryElementFactory implements FileDictionaryElementFactory {

    private static final Logger log = LoggerFactory.getLogger(AbstractPrincetonFileDictionaryElementFactory.class);

    protected AbstractPrincetonFileDictionaryElementFactory(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary, params);
    }

    public IndexWord createIndexWord(POS pos, CharSequence line) throws JWNLException {
        CharSequenceParser p = new CharSequenceParser(line);
        String lemma = stringCache.replace(p.nextToken().replace('_', ' '));
        p.skipToken(); // pos
        p.skipToken(); // sense_cnt

        int pointerCount = p.nextInt();
        for (int i = 0; i < pointerCount; ++i) {
            p.skipToken();    // ptr_symbol
        }
        // Same as sense_cnt above. This is redundant, but the field was preserved for compatibility reasons.
        int senseCount = p.nextInt();

        // Number of senses of lemma that are ranked according to their
        // frequency of occurrence in semantic concordance texts.
        p.skipToken(); // tagged sense count

        long[] synsetOffsets = new long[senseCount];
        for (int i = 0; i < senseCount; i++) {
            synsetOffsets[i] = p.nextLong();
        }
        if (log.isTraceEnabled()) {
            log.trace(dictionary.getMessages().resolveMessage("PRINCETON_INFO_003", new Object[]{lemma, pos}));
        }
        return new IndexWord(dictionary, lemma, pos, synsetOffsets);
    }

    public Synset createSynset(POS pos, CharSequence line) throws JWNLException {
        CharSequenceParser p = new CharSequenceParser(line);

        long offset = p.nextLong();
        long lexFileNum = p.nextLong();
        char synsetPOS = p.nextChar();

        Synset synset;
        if (POS.VERB == pos) {
            synset = new VerbSynset(dictionary, offset);
        } else if (POS.ADJECTIVE == pos) {
            synset = new AdjectiveSynset(dictionary, offset);
            if ('s' == synsetPOS) {
                synset.setIsAdjectiveCluster(true);
            }
        } else {
            synset = new Synset(dictionary, POS.getPOSForKey(synsetPOS), offset);
        }

        synset.setLexFileNum(lexFileNum);

        int wordCount = p.nextHexInt();
        for (int i = 0; i < wordCount; i++) {
            String lemma = stringCache.replace(p.nextToken().replace('_', ' '));

            int lexId = p.nextHexInt(); // lex id

            // NB index: Word numbers are assigned to the word fields in a synset, from left to right, beginning with 1
            Word w = createWord(synset, lemma);
            w.setLexId(lexId);
            synset.getWords().add(w);
        }
        if (synset.getWords() instanceof ArrayList) {
            ((ArrayList) synset.getWords()).trimToSize();
        }

        int pointerCount = p.nextInt();
        for (int i = 0; i < pointerCount; i++) {
            CharSequence pt = ((CharSequenceTokenizer) p).nextToken();
            PointerType pointerType = PointerType.getPointerTypeForKey(pt);
            long targetOffset = p.nextLong();
            POS targetPOS = POS.getPOSForKey(p.nextChar());
            int linkIndices = p.nextHexInt();
            int sourceIndex = linkIndices / 256;
            int targetIndex = linkIndices & 255;
            PointerTarget source = (sourceIndex == 0) ? synset : synset.getWords().get(sourceIndex - 1);

            Pointer pointer = new Pointer(source, pointerType, targetPOS, targetOffset, targetIndex);
            synset.getPointers().add(pointer);
        }
        if (synset.getPointers() instanceof ArrayList) {
            ((ArrayList) synset.getPointers()).trimToSize();
        }

        if (POS.VERB == pos) {
            BitSet verbFrames = new BitSet();
            int verbFrameCount = p.nextInt();
            for (int i = 0; i < verbFrameCount; i++) {
                p.skipToken();    // "+"
                int frameNumber = p.nextInt();
                int wordIndex = p.nextHexInt();
                initVerbFrameFlags(synset, verbFrames, frameNumber, wordIndex);
            }
            synset.setVerbFrameFlags(verbFrames);
        }

        p.skipToken(); // |
        CharSequence gloss = ((CharSequenceTokenizer) p).remainder();
        synset.setGloss(gloss.subSequence(0, gloss.length() - 2).toString());

        Long mOffset = maxOffset.get(synset.getPOS());
        if (null == mOffset) {
            maxOffset.put(synset.getPOS(), synset.getOffset());
        } else {
            if (mOffset < synset.getOffset()) {
                maxOffset.put(synset.getPOS(), synset.getOffset());
            }
        }

        if (log.isTraceEnabled()) {
            log.trace(dictionary.getMessages().resolveMessage("PRINCETON_INFO_002", new Object[]{pos, offset}));
        }
        return synset;
    }

    public Exc createExc(POS pos, CharSequence line) throws JWNLException {
        CharSequenceParser p = new CharSequenceParser(line);
        String lemma = stringCache.replace(p.nextToken().replace('_', ' '));
        ArrayList<String> exceptions = new ArrayList<>();
        while (p.hasMoreTokens()) {
            exceptions.add(stringCache.replace(p.nextToken().replace('_', ' ')));
        }
        exceptions.trimToSize();
        if (log.isTraceEnabled()) {
            log.trace(dictionary.getMessages().resolveMessage("PRINCETON_INFO_001", new Object[]{pos, lemma}));
        }
        return new Exc(dictionary, pos, lemma, exceptions);
    }
}