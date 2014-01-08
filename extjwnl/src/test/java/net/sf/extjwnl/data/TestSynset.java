package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tests Synset functionality by creating a mock synset.
 *
 * @author Brett Walenz <bwalenz@users.sourceforge.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestSynset extends BaseData {

    private Synset testObj;
    private final static String gloss = "testGloss";
    private final static long offset = 4125;

    @Before
    public void setUp() throws JWNLException, IOException {
        super.setUp();
        testObj = new Synset(dictionary, POS.NOUN, offset);
        testObj.setGloss(gloss);
    }

    @Test
    public void testConstructor() throws JWNLException {
        testObj = new Synset(dictionary, POS.NOUN, offset);
    }

    @Test
    public void testGetPOS() {
        Assert.assertEquals(POS.NOUN, testObj.getPOS());
        Assert.assertNotNull(testObj.toString());
    }

    @Test
    public void testGetGloss() {
        Assert.assertEquals(gloss, testObj.getGloss());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetVerbFrameFlags() {
        Assert.assertTrue(testObj.getVerbFrameFlags().isEmpty());
    }

    @Test
    public void testGetOffset() {
        Assert.assertEquals(offset, testObj.getOffset());
    }

    @Test
    public void testGetWordsSize() {
        Assert.assertEquals(0, testObj.getWords().size());
    }

    @Test
    public void testGetPointersSize() throws JWNLException {
        Assert.assertEquals(0, testObj.getPointers().size());
    }

    @Test
    public void testGetPointersEmpty() throws JWNLException {
        Assert.assertTrue(testObj.getPointers().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullPointer() throws JWNLException {
        testObj.getPointers().add(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullPointer2() throws JWNLException {
        testObj.getPointers().add(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullPointer() throws JWNLException {
        testObj.getPointers().set(0, null);
    }

    @Test
    public void testAddPointer() throws JWNLException {
        Synset hyponym = new Synset(dictionary, POS.NOUN, 100);
        dictionary.edit();
        testObj.getPointers().add(new Pointer(PointerType.HYPONYM, testObj, hyponym));
        Assert.assertEquals(1, testObj.getPointers().size());
        Assert.assertEquals(1, hyponym.getPointers().size());
        Assert.assertEquals(PointerType.HYPERNYM, hyponym.getPointers().get(0).getType());
        Assert.assertEquals(testObj, hyponym.getPointers().get(0).getTarget());

        Assert.assertEquals(1, testObj.getTargets().size());
        Assert.assertEquals(hyponym, testObj.getTargets().get(0));
    }

    @Test
    public void testSetPointer() throws JWNLException {
        Synset hyponym = new Synset(dictionary, POS.NOUN, 1);
        hyponym.setGloss("hyponym");
        Synset hypernym = new Synset(dictionary, POS.NOUN, 2);
        hypernym.setGloss("hypernym");
        dictionary.edit();
        testObj.getPointers().add(new Pointer(PointerType.HYPERNYM, testObj, hypernym));
        testObj.getPointers().set(0, new Pointer(PointerType.HYPONYM, testObj, hyponym));
        Assert.assertEquals(1, testObj.getPointers().size());
        Assert.assertEquals(1, hyponym.getPointers().size());
        Assert.assertEquals(0, hypernym.getPointers().size());
        Assert.assertEquals(PointerType.HYPERNYM, hyponym.getPointers().get(0).getType());
        Assert.assertEquals(testObj, hyponym.getPointers().get(0).getTarget());

        Assert.assertEquals(1, testObj.getTargets().size());
        Assert.assertEquals(hyponym, testObj.getTargets().get(0));
    }

    @Test
    public void testAddPointer2() throws JWNLException {
        Synset hyponym = new Synset(dictionary, POS.NOUN, 100);
        dictionary.edit();
        testObj.getPointers().add(0, new Pointer(PointerType.HYPONYM, testObj, hyponym));
        Assert.assertEquals(1, testObj.getPointers().size());
        Assert.assertEquals(1, hyponym.getPointers().size());
        Assert.assertEquals(PointerType.HYPERNYM, hyponym.getPointers().get(0).getType());
        Assert.assertEquals(testObj, hyponym.getPointers().get(0).getTarget());
    }

    @Test
    public void testAddPointer3() throws JWNLException {
        Synset hyponym = new Synset(dictionary, POS.NOUN, 100);
        testObj.getPointers().add(0, new Pointer(PointerType.HYPONYM, testObj, hyponym));
        Assert.assertEquals(1, testObj.getPointers().size());
        Assert.assertEquals(0, hyponym.getPointers().size());
    }

    @Test
    public void testRemovePointer() throws JWNLException {
        Synset hyponym = new Synset(dictionary, POS.NOUN, 100);
        dictionary.edit();
        testObj.getPointers().add(new Pointer(PointerType.HYPONYM, testObj, hyponym));
        hyponym.getPointers().remove(0);

        Assert.assertEquals(0, testObj.getPointers().size());
        Assert.assertEquals(0, hyponym.getPointers().size());
    }

    @Test
    public void testRemovePointer2() throws JWNLException {
        Synset hyponym = new Synset(dictionary, POS.NOUN, 100);
        dictionary.edit();
        testObj.getPointers().add(new Pointer(PointerType.HYPONYM, testObj, hyponym));
        hyponym.getPointers().remove(hyponym.getPointers().get(0));

        Assert.assertEquals(0, testObj.getPointers().size());
        Assert.assertEquals(0, hyponym.getPointers().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetWord() throws JWNLException {
        testObj.getWords().set(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetWord2() throws JWNLException {
        testObj.getWords().set(0, new Word(dictionary, new Synset(dictionary, POS.NOUN), 0, "test"));
    }

    @Test
    public void testSetWord3() throws JWNLException {
        dictionary.edit();
        testObj.getWords().add(new Word(dictionary, testObj, 1, "test2"));
        testObj.getWords().set(0, new Word(dictionary, testObj, 1, "test"));

        Assert.assertEquals(1, testObj.getWords().size());
        Assert.assertEquals("test", testObj.getWords().get(0).getLemma());

        List<IndexWord> indexWords = new ArrayList<IndexWord>(1);
        Iterator<IndexWord> i = dictionary.getIndexWordIterator(POS.NOUN);
        while (i.hasNext()) {
            indexWords.add(i.next());
        }
        Assert.assertEquals(1, indexWords.size());
        Assert.assertEquals("test", indexWords.get(0).getLemma());
        Assert.assertEquals(1, indexWords.get(0).getSenses().size());
        Assert.assertEquals(testObj, indexWords.get(0).getSenses().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddWord() throws JWNLException {
        dictionary.edit();
        testObj.getWords().add(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddWord2() throws JWNLException {
        testObj.getWords().add(new Word(null, new Synset(dictionary, POS.NOUN), 0, "test"));
    }

    @Test
    public void testAddWord3() throws JWNLException {
        dictionary.edit();
        testObj.getWords().add(new Word(dictionary, testObj, 1, "test"));
        Assert.assertEquals(1, testObj.getWords().size());
        Assert.assertEquals("test", testObj.getWords().get(0).getLemma());

        List<IndexWord> indexWords = new ArrayList<IndexWord>(1);
        Iterator<IndexWord> i = dictionary.getIndexWordIterator(POS.NOUN);
        while (i.hasNext()) {
            indexWords.add(i.next());
        }
        Assert.assertEquals(1, indexWords.size());
        Assert.assertEquals("test", indexWords.get(0).getLemma());
        Assert.assertEquals(1, indexWords.get(0).getSenses().size());
        Assert.assertEquals(testObj, indexWords.get(0).getSenses().get(0));
    }

    @Test
    public void testRemoveWord() throws JWNLException {
        dictionary.edit();
        testObj.getWords().add(new Word(dictionary, testObj, 1, "test"));
        testObj.getWords().remove(0);

        Assert.assertEquals(0, testObj.getWords().size());

        List<IndexWord> indexWords = new ArrayList<IndexWord>(1);
        Iterator<IndexWord> i = dictionary.getIndexWordIterator(POS.NOUN);
        while (i.hasNext()) {
            indexWords.add(i.next());
        }
        Assert.assertEquals(0, indexWords.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateSynset() throws JWNLException {
        new Synset(dictionary, null);
    }

    @Test
    public void testElementType() throws JWNLException {
        Assert.assertEquals(DictionaryElementType.SYNSET, testObj.getType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetGloss() throws JWNLException {
        testObj.setGloss(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAdjectiveCluster() throws JWNLException {
        testObj.isAdjectiveCluster();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testVerbFrames() throws JWNLException {
        testObj.getVerbFrames();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testVerbFrameIndices() throws JWNLException {
        testObj.getVerbFrameIndices();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetAdjectiveCluster() throws JWNLException {
        testObj.setIsAdjectiveCluster(true);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetVerbFramesFlags() throws JWNLException {
        testObj.setVerbFrameFlags(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void containsWord() throws JWNLException {
        testObj.containsWord(null);
    }

    @Test
    public void containsWord2() throws JWNLException {
        Assert.assertFalse(testObj.containsWord("test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void indexOfWord() throws JWNLException {
        testObj.indexOfWord(null);
    }

    @Test
    public void testSetDictionary() throws JWNLException {
        dictionary.edit();
        mapDictionary.edit();
        testObj.setDictionary(mapDictionary);

        List<Synset> synsets = new ArrayList<Synset>(0);
        Iterator<Synset> i = dictionary.getSynsetIterator(POS.NOUN);
        while (i.hasNext()) {
            synsets.add(i.next());
        }
        Assert.assertEquals(0, synsets.size());

        synsets = new ArrayList<Synset>(0);
        i = mapDictionary.getSynsetIterator(POS.NOUN);
        while (i.hasNext()) {
            synsets.add(i.next());
        }
        Assert.assertEquals(1, synsets.size());
        Assert.assertEquals(testObj, synsets.get(0));
    }
}