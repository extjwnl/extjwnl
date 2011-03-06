package net.didion.jwnl.princeton.data;

import net.didion.jwnl.data.Adjective;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.util.factory.Param;

import java.util.Map;

/**
 * <code>FileDictionaryElementFactory</code> that produces elements for Princeton's release of WordNet v 1.6
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class PrincetonWN16FileDictionaryElementFactory extends AbstractPrincetonFileDictionaryElementFactory {

    public PrincetonWN16FileDictionaryElementFactory(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary);
    }

    protected Word createWord(Synset synset, int index, String lemma) {
        if (synset.getPOS().equals(POS.ADJECTIVE)) {
            Adjective.AdjectivePosition adjectivePosition = Adjective.NONE;
            if (lemma.charAt(lemma.length() - 1) == ')' && lemma.indexOf('(') > 0) {
                int lparen = lemma.indexOf('(');
                String marker = lemma.substring(lparen + 1, lemma.length() - 1);
                adjectivePosition = Adjective.getAdjectivePositionForKey(marker);
                lemma = lemma.substring(0, lparen);
            }
            return new Adjective(dictionary, synset, index, lemma, adjectivePosition);
        } else {
            return super.createWord(synset, index, lemma);
        }
    }
}
