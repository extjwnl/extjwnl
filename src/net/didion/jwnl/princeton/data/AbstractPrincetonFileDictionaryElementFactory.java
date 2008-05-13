/**
 * Java WordNet Library (JWNL)
 * See the documentation for copyright information.
 */
package net.didion.jwnl.princeton.data;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.StringTokenizer;

import net.didion.jwnl.data.Exc;
import net.didion.jwnl.data.FileDictionaryElementFactory;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerTarget;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.SynsetProxy;
import net.didion.jwnl.data.Verb;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.util.MessageLog;
import net.didion.jwnl.util.MessageLogLevel;
import net.didion.jwnl.util.TokenizerParser;

/**
 * <code>FileDictionaryElementFactory</code> that parses lines from the dictionary files distributed by the
 * WordNet team at Princeton's Cognitive Science department.
 */
public abstract class AbstractPrincetonFileDictionaryElementFactory implements FileDictionaryElementFactory {
	private static final MessageLog _log = new MessageLog(AbstractPrincetonFileDictionaryElementFactory.class);

	
	
	protected AbstractPrincetonFileDictionaryElementFactory() {
	}

	public IndexWord createIndexWord(POS pos, String line) {
        TokenizerParser tokenizer = new TokenizerParser(line, " ");
        String lemma = tokenizer.nextToken().replace('_', ' ');
        tokenizer.nextToken(); // pos
        tokenizer.nextToken();	// poly_cnt
        int pointerCount = tokenizer.nextInt();
        // TODO: can we do anything interesting with these?
        for (int i = 0; i < pointerCount; ++i) tokenizer.nextToken();	// ptr_symbol
        int senseCount = tokenizer.nextInt();
        tokenizer.nextInt(); // tagged sense count
        long[] synsetOffsets = new long[senseCount];
        for (int i = 0; i < senseCount; i++) {
            synsetOffsets[i] = tokenizer.nextLong();
        }
        if (_log.isLevelEnabled(MessageLogLevel.TRACE)) {
            _log.log(MessageLogLevel.TRACE, "PRINCETON_INFO_003", new Object[]{lemma, pos});
        }
        return new IndexWord(lemma, pos, synsetOffsets);
	}

	public Synset createSynset(POS pos, String line) {
        TokenizerParser tokenizer = new TokenizerParser(line, " ");

        long offset = tokenizer.nextLong();
        /**
         * Grab the filenum token here. 
         */
        //String lexFileNameId = tokenizer.nextToken();	// lex_filenum
        long lexFileNameId = tokenizer.nextLong();
       
        String synsetPOS = tokenizer.nextToken();
        boolean isAdjectiveCluster = false;
        if (synsetPOS.equals("s")) {
            isAdjectiveCluster = true;
        }

        SynsetProxy proxy = new SynsetProxy(pos);

        int wordCount = tokenizer.nextHexInt();
        Word[] words = new Word[wordCount];
        for (int i = 0; i < wordCount; i++) {
            String lemma = tokenizer.nextToken();
            
            tokenizer.nextHexInt(); // lex id
            
          
            
            words[i] = createWord(proxy, i, lemma);
            
        }

        int pointerCount = tokenizer.nextInt();
        Pointer[] pointers = new Pointer[pointerCount];
        for (int i = 0; i < pointerCount; i++) {
            String pt = tokenizer.nextToken();
            PointerType pointerType = PointerType.getPointerTypeForKey(pt);
            long targetOffset = tokenizer.nextLong();
            POS targetPOS = POS.getPOSForKey(tokenizer.nextToken());
            int linkIndices = tokenizer.nextHexInt();
            int sourceIndex = linkIndices / 256;
            int targetIndex = linkIndices & 255;
            PointerTarget source = (sourceIndex == 0) ? (PointerTarget) proxy : (PointerTarget) words[sourceIndex - 1];

            pointers[i] = new Pointer(source, i, pointerType, targetPOS, targetOffset, targetIndex);
        }

        if (pos == POS.VERB) {
            int verbFrameCount = tokenizer.nextInt();
            for (int i = 0; i < verbFrameCount; i++) {
                tokenizer.nextToken();	// "+"
                int frameNumber = tokenizer.nextInt();
                int wordIndex = tokenizer.nextHexInt();
                if (wordIndex > 0) {
                    ((MutableVerb) words[wordIndex - 1]).setVerbFrameFlag(frameNumber);
                } else {
                    for (int j = 0; j < words.length; ++j)
                        ((MutableVerb) words[j]).setVerbFrameFlag(frameNumber);
                }
            }
        }

        String gloss = null;
        int index = line.indexOf('|');
        if (index > 0) {
            gloss = line.substring(index + 2).trim();
        }

        BitSet verbFrames = new BitSet();
        for (int i = 0; i < words.length; i++)
            if (words[i] instanceof Verb)
                verbFrames.or(((Verb)words[i]).getVerbFrameFlags());

        Synset synset = new Synset(pos, offset, words, pointers, gloss, verbFrames, isAdjectiveCluster);
        
        //set the lexicographer file identifier
        synset.setLexFileId(lexFileNameId);
        proxy.setSource(synset);
        if (_log.isLevelEnabled(MessageLogLevel.TRACE)) {
            _log.log(MessageLogLevel.TRACE, "PRINCETON_INFO_002", new Object[]{pos, new Long(offset)});
        }
        return proxy;
	}

	/**
	 * Creates a word, also access the sense.idx file. 
	 * @param synset
	 * @param index
	 * @param lemma
	 * @return
	 */
	protected Word createWord(Synset synset, int index, String lemma) {
		Word word; 
		if (synset.getPOS().equals(POS.VERB)) {
            word = new MutableVerb(synset, index, lemma);
        } else {
            word = new Word(synset, index, lemma);
        }

		
		return word;
	}

	public Exc createExc(POS pos, String line) {
        StringTokenizer st = new StringTokenizer(line);
        String lemma = st.nextToken().replace('_', ' ');
        List exceptions = new ArrayList();
        while (st.hasMoreTokens()) {
            exceptions.add(st.nextToken().replace('_', ' '));
        }
        if (_log.isLevelEnabled(MessageLogLevel.TRACE)) {
            _log.log(MessageLogLevel.TRACE, "PRINCETON_INFO_001", new Object[]{pos, lemma});
        }
        return new Exc(pos, lemma, exceptions);
	}
	

}