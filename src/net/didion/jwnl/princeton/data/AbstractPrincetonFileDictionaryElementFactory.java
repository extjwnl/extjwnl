package net.didion.jwnl.princeton.data;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.*;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.util.MessageLog;
import net.didion.jwnl.util.MessageLogLevel;
import net.didion.jwnl.util.TokenizerParser;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <code>FileDictionaryElementFactory</code> that parses lines from the dictionary files distributed by the
 * WordNet team at Princeton's Cognitive Science department.
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public abstract class AbstractPrincetonFileDictionaryElementFactory implements FileDictionaryElementFactory {

    private static final MessageLog log = new MessageLog(AbstractPrincetonFileDictionaryElementFactory.class);
    protected Dictionary dictionary;

    protected AbstractPrincetonFileDictionaryElementFactory(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public IndexWord createIndexWord(POS pos, String line) throws JWNLException {
        TokenizerParser tokenizer = new TokenizerParser(line, " ");
        //String lemma = tokenizer.nextToken().replace('_', ' ');
        String lemma = tokenizer.nextToken();//keep lemmas with underscores everywhere
        tokenizer.nextToken(); // pos
        tokenizer.nextToken();    // poly_cnt
        int pointerCount = tokenizer.nextInt();
        // TODO: can we do anything interesting with these?
        for (int i = 0; i < pointerCount; ++i) {
            tokenizer.nextToken();    // ptr_symbol
        }
        int senseCount = tokenizer.nextInt();
        tokenizer.nextInt(); // tagged sense count
        long[] synsetOffsets = new long[senseCount];
        for (int i = 0; i < senseCount; i++) {
            synsetOffsets[i] = tokenizer.nextLong();
        }
        if (log.isLevelEnabled(MessageLogLevel.TRACE)) {
            log.log(MessageLogLevel.TRACE, "PRINCETON_INFO_003", new Object[]{lemma, pos});
        }
        return new IndexWord(dictionary, lemma, pos, synsetOffsets);
    }

    public Synset createSynset(POS pos, String line) throws JWNLException {
        TokenizerParser tokenizer = new TokenizerParser(line, " ");

        long offset = tokenizer.nextLong();
        long lexFileNum = tokenizer.nextLong();
        String synsetPOS = tokenizer.nextToken();

        Synset synset = new Synset(dictionary, POS.getPOSForKey(synsetPOS), offset);
        synset.setLexFileNum(lexFileNum);

        boolean isAdjectiveCluster = false;
        if ("s".equals(synsetPOS)) {
            isAdjectiveCluster = true;
        }
        synset.setIsAdjectiveCluster(isAdjectiveCluster);

        int wordCount = tokenizer.nextHexInt();
        ArrayList<Word> words = new ArrayList<Word>(wordCount);
        for (int i = 0; i < wordCount; i++) {
            String lemma = tokenizer.nextToken();

            int lexId = tokenizer.nextHexInt(); // lex id

            //NB index: Word numbers are assigned to the word fields in a synset, from left to right, beginning with 1
            Word w = createWord(synset, i + 1, lemma);
            w.setLexId(lexId);
            words.add(w);
        }
        synset.getWords().addAll(words);

        int pointerCount = tokenizer.nextInt();
        ArrayList<Pointer> pointers = new ArrayList<Pointer>(pointerCount);
        for (int i = 0; i < pointerCount; i++) {
            String pt = tokenizer.nextToken();
            PointerType pointerType = PointerType.getPointerTypeForKey(pt);
            long targetOffset = tokenizer.nextLong();
            POS targetPOS = POS.getPOSForKey(tokenizer.nextToken());
            int linkIndices = tokenizer.nextHexInt();
            int sourceIndex = linkIndices / 256;
            int targetIndex = linkIndices & 255;
            PointerTarget source = (sourceIndex == 0) ? synset : synset.getWords().get(sourceIndex - 1);

            Pointer p = new Pointer(source, pointerType, targetPOS, targetOffset, targetIndex);
            pointers.add(p);
        }
        synset.getPointers().addAll(pointers);

        if (pos == POS.VERB) {
            BitSet verbFrames = new BitSet();
            int verbFrameCount = tokenizer.nextInt();
            for (int i = 0; i < verbFrameCount; i++) {
                tokenizer.nextToken();    // "+"
                int frameNumber = tokenizer.nextInt();
                int wordIndex = tokenizer.nextHexInt();
                if (wordIndex > 0) {
                    ((MutableVerb) synset.getWords().get(wordIndex - 1)).setVerbFrameFlag(frameNumber);
                } else {
                    for (Word w : synset.getWords()) {
                        ((MutableVerb) w).setVerbFrameFlag(frameNumber);
                    }
                    verbFrames.set(frameNumber);
                }
            }
            synset.setVerbFrameFlags(verbFrames);
        }

        String gloss = null;
        int index = line.indexOf('|');
        if (index > 0) {
            gloss = line.substring(index + 2).trim();
        }
        synset.setGloss(gloss);

        if (log.isLevelEnabled(MessageLogLevel.TRACE)) {
            log.log(MessageLogLevel.TRACE, "PRINCETON_INFO_002", new Object[]{pos, offset});
        }
        return synset;
    }

    /**
     * Creates a word, also access the sense.idx file.
     *
     * @param synset synset
     * @param index  index
     * @param lemma  lemma
     * @return word
     */
    protected Word createWord(Synset synset, int index, String lemma) {
        Word word;
        if (synset.getPOS().equals(POS.VERB)) {
            word = new MutableVerb(dictionary, synset, index, lemma);
        } else {
            word = new Word(dictionary, synset, index, lemma);
        }

        return word;
    }

    public Exc createExc(POS pos, String line) throws JWNLException {
        StringTokenizer st = new StringTokenizer(line);
        String lemma = st.nextToken().replace('_', ' ');
        List<String> exceptions = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            exceptions.add(st.nextToken().replace('_', ' '));
        }
        if (log.isLevelEnabled(MessageLogLevel.TRACE)) {
            log.log(MessageLogLevel.TRACE, "PRINCETON_INFO_001", new Object[]{pos, lemma});
        }
        return new Exc(dictionary, pos, lemma, exceptions);
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }
}