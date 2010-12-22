package net.didion.jwnl.data;

import junit.framework.TestCase;

/**
 * Tests the word functionality.
 *
 * @author bwalenz
 */
public class TestWord extends TestCase {

    /**
     * Test word.
     */
    Word word;
    /**
     * Default lemma.
     */
    String lemma = "testLemma";
    /**
     * Notional index.
     */
    int index = 1;

    /**
     * Creates a new word with no synset.
     */
    protected void setUp() throws Exception {
        word = new Word(null, index, lemma);
    }

    /**
     * Tests getting the index.
     */
    public void testGetIndex() {
        assertTrue(word.getIndex() == index);
    }

    /**
     * Tests getting the lemma.
     */
    public void testGetLemma() {
        assertTrue(word.getLemma().equals(lemma));
    }

}
