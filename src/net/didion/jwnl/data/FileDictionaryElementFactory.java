/**
 * Java WordNet Library (JWNL)
 * See the documentation for copyright information.
 */
package net.didion.jwnl.data;

import java.util.List;
import java.util.Map;

import net.didion.jwnl.util.factory.Createable;

/**
 * Factory class for creating <code>DictionaryElement</code>s (<code>Synset</code>s, <code>Exception</codes,
 * and <code>IndexWord</code>s). Using a factory class rather than individual parsing methods in each class
 * facilitates using multiple versions of WordNet, or using a propritary data format.
 */
public interface FileDictionaryElementFactory extends Createable {
	/** Create an Exc from a line in an exception file. */
	public Exc createExc(POS pos, String line);
	/** Create a Synset from a line in a data file */
	public Synset createSynset(POS pos, String line);
	/** Create an IndexWord from a line in an index file. */
	public IndexWord createIndexWord(POS pos, String line);
	
	/**
	 * A mapping of terms. Key is synsetOffset+lemma.
	 * @param senseMap
	 */
	public void setSenseMapping(Map<String, Word> senseMap);
}