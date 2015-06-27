package net.sf.extjwnl.data;

import net.sf.extjwnl.dictionary.Dictionary;

/**
 * An <code>Adjective</code> is a <code>Word</code> that can have an adjective position.
 * <p>
 * Note: Adjective positions are only supported through WordNet v1.5.
 * </p>
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class Adjective extends Word {

	private static final long serialVersionUID = 5L;

	private final AdjectivePosition adjectivePosition;

	public Adjective(Dictionary dictionary, Synset synset, String lemma,
			AdjectivePosition adjectivePosition) {
		super(dictionary, synset, lemma);
		this.adjectivePosition = adjectivePosition;
	}

	public AdjectivePosition getAdjectivePosition() {
		return adjectivePosition;
	}
}