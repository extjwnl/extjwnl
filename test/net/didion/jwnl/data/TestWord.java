package net.didion.jwnl.data;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Tests the word functionality.
 *
 * @author bwalenz
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class TestWord extends BaseDictionaryTest {

    private Word word;

    String lemma = "testLemma";
    int index = 1;

    @Before
    public void setUp() throws JWNLException, IOException {
        Dictionary dictionary = Dictionary.getInstance(getProperties());
        word = new Word(dictionary, new Synset(dictionary, POS.NOUN), index, lemma);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate() {
        word = new Word(null, null, -1, null);

    }

    @Test
    public void testGetIndex() {
        Assert.assertEquals(index, word.getIndex());
    }

    @Test
    public void testGetLemma() {
        Assert.assertEquals(lemma, word.getLemma());
    }
}