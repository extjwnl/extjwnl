/**
 * Java WordNet Library (JWNL)
 * See the documentation for copyright information.
 */
package net.didion.jwnl.data;

import net.didion.jwnl.util.factory.Createable;

/**
 * Factory class for creating <code>DictionaryElement</code>s (<code>Synset</code>s, <code>Exception</codes,
 * and <code>IndexWord</code>s). Using a factory class rather than individual parsing methods in each class
 * facilitates using multiple versions of WordNet, or using a propritary data format.
 */
public interface FileDictionaryElementFactory extends Createable {
    
	/**
     * Create an Exc from a line in an exception file.
     * @param pos - the part of speech 
     * @param line - unparsed line
     * @return exception
	 */
	public Exc createExc(POS pos, String line);
	
    /**
     * Creates a synset from a line in a data file. 
     * @param pos - the part of speech
     * @param line - unparsed line
     * @return synset
     */
	public Synset createSynset(POS pos, String line);
    
	/**
     * Creates an IndexWord from a line in an index file. 
     * @param pos - the part of speech 
     * @param line - unparsed line
     * @return indexword
	 */
	public IndexWord createIndexWord(POS pos, String line);
	
}