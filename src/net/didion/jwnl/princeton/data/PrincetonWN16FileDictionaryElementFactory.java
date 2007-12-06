/**
 * Java WordNet Library (JWNL)
 * See the documentation for copyright information.
 */
package net.didion.jwnl.princeton.data;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.Adjective;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;

import java.util.Map;

/** <code>FileDictionaryElementFactory</code> that produces elements for Princeton's release of WordNet v 1.6 */
public class PrincetonWN16FileDictionaryElementFactory extends AbstractPrincetonFileDictionaryElementFactory {
	public PrincetonWN16FileDictionaryElementFactory() {
	}

	public Object create(Map params) throws JWNLException {
		return new PrincetonWN16FileDictionaryElementFactory();
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
			return new Adjective(synset, index, lemma, adjectivePosition);
		} else {
			return super.createWord(synset, index, lemma);
		}
	}
}
