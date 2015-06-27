package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Tests the word functionality.
 *
 * @author Brett Walenz (bwalenz@users.sourceforge.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestWord extends BaseData {

    private Word word;

    private String lemma = "testLemma";

    @Before
    public void setUp() throws JWNLException, IOException {
        super.setUp();
        word = new Word(dictionary, new Synset(dictionary, POS.NOUN), lemma);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNullSynset() {
        word = new Word(dictionary, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateIndex() throws JWNLException {
        word = new Word(dictionary, new Synset(dictionary, POS.NOUN), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNullLemma() throws JWNLException {
        word = new Word(dictionary, new Synset(dictionary, POS.NOUN), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEmptyLemma() throws JWNLException {
        word = new Word(dictionary, new Synset(dictionary, POS.NOUN), "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateLemmaPrefix() throws JWNLException {
        word = new Word(dictionary, new Synset(null, POS.NOUN), " a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateLemmaSuffix() throws JWNLException {
        word = new Word(dictionary, new Synset(dictionary, POS.NOUN), "a ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAlienSynset() throws JWNLException {
        word = new Word(dictionary, new Synset(mapDictionary, POS.NOUN), "a ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate2NullSynset() {
        word = new Word(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate2Index() throws JWNLException {
        word = new Word(null, new Synset(null, POS.NOUN), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate2NullLemma() throws JWNLException {
        word = new Word(null, new Synset(null, POS.NOUN), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate2EmptyLemma() throws JWNLException {
        word = new Word(null, new Synset(null, POS.NOUN), "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate2LemmaPrefix() throws JWNLException {
        word = new Word(null, new Synset(null, POS.NOUN), " a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate2LemmaSuffix() throws JWNLException {
        word = new Word(null, new Synset(null, POS.NOUN), "a ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate2AlienSynset() throws JWNLException {
        word = new Word(null, new Synset(mapDictionary, POS.NOUN), "a ");
    }

    @Test
    public void testGetIndex() {
        Assert.assertEquals(0, word.getIndex());
    }

    @Test
    public void testGetLemma() {
        Assert.assertEquals(lemma, word.getLemma());
    }

    @Test
    public void testGetPointers() {
        Assert.assertEquals(0, word.getPointers().size());

        Synset synset = word.getSynset();
        synset.getPointers().add(new Pointer(PointerType.ANTONYM, synset, synset));
        Assert.assertEquals(0, word.getPointers().size());
        synset.getPointers().add(new Pointer(PointerType.ANTONYM, word, synset));
        Assert.assertEquals(1, word.getPointers().size());
    }

    @Test
    public void testHashCode() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN);
        Word w = new Word(dictionary, s, "lemma");
        Word ww = new Word(dictionary, s, "gemma");

        Assert.assertNotEquals(w.hashCode(), ww.hashCode());
    }
}