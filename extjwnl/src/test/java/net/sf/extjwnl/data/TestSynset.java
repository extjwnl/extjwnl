package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

/**
 * Tests Synset functionality by creating a mock synset.
 *
 * @author Brett Walenz (bwalenz@users.sourceforge.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
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
    public void testConstructorNullDictionary() throws JWNLException {
        testObj = new Synset(null, POS.NOUN, offset);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullPOS() throws JWNLException {
        testObj = new Synset(dictionary, null, offset);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullPOSNullD() throws JWNLException {
        testObj = new Synset(null, null, offset);
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
        dictionary.edit();
        testObj.setDictionary(null);
        testObj.getPointers().add(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullPointerIndexed() throws JWNLException {
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
    public void testPointersLastIndexOf() throws JWNLException {
        dictionary.edit();
        Synset hyponym = new Synset(dictionary, POS.NOUN, 100);
        Pointer hyponymPtr = new Pointer(PointerType.HYPONYM, testObj, hyponym);
        testObj.getPointers().add(hyponymPtr);
        Assert.assertEquals(0, testObj.getPointers().lastIndexOf(hyponymPtr));
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
        dictionary.edit();
        Synset hyponym = new Synset(dictionary, POS.NOUN, 100);
        testObj.getPointers().add(0, new Pointer(PointerType.HYPONYM, testObj, hyponym));
        Assert.assertEquals(1, testObj.getPointers().size());
        Assert.assertEquals(1, hyponym.getPointers().size());
    }

    @Test
    public void testPointerListIterator() throws JWNLException {
        Synset hyponym = new Synset(dictionary, POS.NOUN, 100);
        Pointer hyponymPtr = new Pointer(PointerType.HYPONYM, testObj, hyponym);
        testObj.getPointers().add(0, hyponymPtr);
        ListIterator<Pointer> i = testObj.getPointers().listIterator();
        Assert.assertTrue(i.hasNext());
        Assert.assertEquals(hyponymPtr, i.next());
        Assert.assertFalse(i.hasNext());
    }

    @Test
    public void testAddPointerAddAll() throws JWNLException {
        dictionary.edit();
        Synset hyponym = new Synset(dictionary, POS.NOUN, 100);
        Synset hypernym = new Synset(dictionary, POS.NOUN, 200);

        Assert.assertTrue(
                testObj.getPointers().addAll(Arrays.asList(
                        new Pointer(PointerType.HYPONYM, testObj, hyponym),
                        new Pointer(PointerType.HYPERNYM, testObj, hypernym)))
        );

        Assert.assertEquals(2, testObj.getPointers().size());
        Assert.assertEquals(1, hyponym.getPointers().size());
        Assert.assertEquals(1, hypernym.getPointers().size());
    }

    @Test
    public void testAddPointerAddAllNull() throws JWNLException {
        dictionary.edit();
        testObj.setDictionary(null);
        Synset hyponym = new Synset(null, POS.NOUN, 100);
        Synset hypernym = new Synset(null, POS.NOUN, 200);

        Assert.assertTrue(
                testObj.getPointers().addAll(Arrays.asList(
                        new Pointer(PointerType.HYPONYM, testObj, hyponym),
                        new Pointer(PointerType.HYPERNYM, testObj, hypernym)))
        );

        Assert.assertEquals(2, testObj.getPointers().size());
        Assert.assertEquals(0, hyponym.getPointers().size());
        Assert.assertEquals(0, hypernym.getPointers().size());
    }

    @Test
    public void testPointerSubList() throws JWNLException {
        Synset hyponym = new Synset(dictionary, POS.NOUN, 100);
        Synset hypernym = new Synset(dictionary, POS.NOUN, 200);

        Pointer hyponymPtr = new Pointer(PointerType.HYPONYM, testObj, hyponym);
        Pointer hypernymPtr = new Pointer(PointerType.HYPERNYM, testObj, hypernym);
        testObj.getPointers().addAll(Arrays.asList(hyponymPtr, hypernymPtr));

        List<Pointer> subList = testObj.getPointers().subList(0, 1);
        Assert.assertEquals(1, subList.size());
        Assert.assertEquals(hyponymPtr, subList.get(0));
    }

    @Test
    public void testPointerContainsAll() throws JWNLException {
        Synset hyponym = new Synset(dictionary, POS.NOUN, 100);
        Synset hypernym = new Synset(dictionary, POS.NOUN, 200);

        Pointer hyponymPtr = new Pointer(PointerType.HYPONYM, testObj, hyponym);
        Pointer hypernymPtr = new Pointer(PointerType.HYPERNYM, testObj, hypernym);
        testObj.getPointers().addAll(Arrays.asList(hyponymPtr, hypernymPtr));

        Assert.assertTrue(testObj.getPointers().containsAll(Arrays.asList(hyponymPtr, hypernymPtr)));
    }

    @Test
    public void testPointerRemoveAllEdit() throws JWNLException {
        dictionary.edit();
        Synset hyponym = new Synset(dictionary, POS.NOUN, 100);
        Synset hypernym = new Synset(dictionary, POS.NOUN, 200);

        Pointer hyponymPtr = new Pointer(PointerType.HYPONYM, testObj, hyponym);
        Pointer hypernymPtr = new Pointer(PointerType.HYPERNYM, testObj, hypernym);
        testObj.getPointers().addAll(Arrays.asList(hyponymPtr, hypernymPtr));

        Assert.assertTrue(testObj.getPointers().removeAll(Arrays.asList(hyponymPtr, hypernymPtr)));
        Assert.assertTrue(testObj.getPointers().isEmpty());
        Assert.assertTrue(hyponym.getPointers().isEmpty());
        Assert.assertTrue(hypernym.getPointers().isEmpty());
    }

    @Test
    public void testPointerRemoveAll() throws JWNLException {
        dictionary.edit();
        Synset hyponym = new Synset(dictionary, POS.NOUN, 100);
        Synset hypernym = new Synset(dictionary, POS.NOUN, 200);

        Pointer hyponymPtr = new Pointer(PointerType.HYPONYM, testObj, hyponym);
        Pointer hypernymPtr = new Pointer(PointerType.HYPERNYM, testObj, hypernym);
        testObj.getPointers().addAll(Arrays.asList(hyponymPtr, hypernymPtr));

        Assert.assertEquals(1, hyponym.getPointers().size());
        Assert.assertEquals(1, hypernym.getPointers().size());

        Assert.assertTrue(testObj.getPointers().removeAll(Arrays.asList(hyponymPtr, hypernymPtr)));
        Assert.assertTrue(testObj.getPointers().isEmpty());
        Assert.assertTrue(hyponym.getPointers().isEmpty());
        Assert.assertTrue(hypernym.getPointers().isEmpty());
    }

    @Test
    public void testPointerRemoveAllNull() throws JWNLException {
        dictionary.edit();
        testObj.setDictionary(null);
        Synset hyponym = new Synset(null, POS.NOUN, 100);
        Synset hypernym = new Synset(null, POS.NOUN, 200);

        Pointer hyponymPtr = new Pointer(PointerType.HYPONYM, testObj, hyponym);
        Pointer hypernymPtr = new Pointer(PointerType.HYPERNYM, testObj, hypernym);
        testObj.getPointers().addAll(Arrays.asList(hyponymPtr, hypernymPtr));

        Assert.assertTrue(hyponym.getPointers().isEmpty());
        Assert.assertTrue(hypernym.getPointers().isEmpty());

        Assert.assertTrue(testObj.getPointers().removeAll(Arrays.asList(hyponymPtr, hypernymPtr)));
        Assert.assertTrue(testObj.getPointers().isEmpty());
        Assert.assertTrue(hyponym.getPointers().isEmpty());
        Assert.assertTrue(hypernym.getPointers().isEmpty());
    }


    @Test
    public void testPointerRetainAllEdit() throws JWNLException {
        dictionary.edit();
        Synset hyponym = dictionary.createSynset(POS.NOUN);
        Synset hypernym = dictionary.createSynset(POS.NOUN);

        Pointer hyponymPtr = new Pointer(PointerType.HYPONYM, testObj, hyponym);
        Pointer hypernymPtr = new Pointer(PointerType.HYPERNYM, testObj, hypernym);
        Assert.assertEquals(0, testObj.getPointers().size());
        testObj.getPointers().addAll(Arrays.asList(hyponymPtr, hypernymPtr));
        Assert.assertEquals(2, testObj.getPointers().size());

        Assert.assertTrue(testObj.getPointers().retainAll(Arrays.asList(hyponymPtr)));
        Assert.assertEquals(1, testObj.getPointers().size());
        Assert.assertEquals(1, hyponym.getPointers().size());
        Assert.assertEquals(hyponymPtr, testObj.getPointers().get(0));
        Assert.assertTrue(hypernym.getPointers().isEmpty());
    }

    @Test
    public void testPointerRetainAllNull() throws JWNLException {
        dictionary.edit();
        testObj.setDictionary(null);
        Synset hyponym = new Synset(null, POS.NOUN, 100);
        Synset hypernym = new Synset(null, POS.NOUN, 200);

        Pointer hyponymPtr = new Pointer(PointerType.HYPONYM, testObj, hyponym);
        Pointer hypernymPtr = new Pointer(PointerType.HYPERNYM, testObj, hypernym);
        testObj.getPointers().addAll(Arrays.asList(hyponymPtr, hypernymPtr));

        Assert.assertTrue(testObj.getPointers().retainAll(Arrays.asList(hyponymPtr)));
        Assert.assertEquals(1, testObj.getPointers().size());
        Assert.assertEquals(hyponymPtr, testObj.getPointers().get(0));
    }

    @Test
    public void testAddPointerAddAllEdit() throws JWNLException {
        dictionary.edit();
        Synset hyponym = new Synset(dictionary, POS.NOUN, 100);
        Synset hypernym = new Synset(dictionary, POS.NOUN, 200);

        Assert.assertTrue(
                testObj.getPointers().addAll(Arrays.asList(
                        new Pointer(PointerType.HYPONYM, testObj, hyponym),
                        new Pointer(PointerType.HYPERNYM, testObj, hypernym)))
        );

        Assert.assertEquals(2, testObj.getPointers().size());
        Assert.assertEquals(1, hyponym.getPointers().size());
        Assert.assertEquals(1, hypernym.getPointers().size());
    }

    @Test
    public void testAddPointerAddAllIndexedEdit() throws JWNLException {
        dictionary.edit();
        Synset hyponym = new Synset(dictionary, POS.NOUN, 100);
        Synset hypernym = new Synset(dictionary, POS.NOUN, 200);

        Pointer hyponymPtr = new Pointer(PointerType.HYPONYM, testObj, hyponym);
        testObj.getPointers().add(hyponymPtr);

        Pointer hypernymPtr = new Pointer(PointerType.HYPERNYM, testObj, hypernym);
        Assert.assertTrue(
                testObj.getPointers().addAll(0, Arrays.asList(
                        hypernymPtr))
        );

        Assert.assertEquals(2, testObj.getPointers().size());
        Assert.assertEquals(hypernymPtr, testObj.getPointers().get(0));
        Assert.assertEquals(1, hyponym.getPointers().size());
        Assert.assertEquals(1, hypernym.getPointers().size());
    }

    @Test
    public void testRemovePointer() throws JWNLException {
        dictionary.edit();
        Synset hyponym = new Synset(dictionary, POS.NOUN, 100);
        testObj.getPointers().add(new Pointer(PointerType.HYPONYM, testObj, hyponym));
        hyponym.getPointers().remove(0);

        Assert.assertEquals(0, testObj.getPointers().size());
        Assert.assertEquals(0, hyponym.getPointers().size());
    }

    @Test
    public void testRemovePointer2() throws JWNLException {
        dictionary.edit();
        Synset hyponym = new Synset(dictionary, POS.NOUN, 100);
        testObj.getPointers().add(new Pointer(PointerType.HYPONYM, testObj, hyponym));
        hyponym.getPointers().remove(hyponym.getPointers().get(0));

        Assert.assertEquals(0, testObj.getPointers().size());
        Assert.assertEquals(0, hyponym.getPointers().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetWord() throws JWNLException {
        testObj.getWords().set(0, null);
    }

    @Test
    public void testSetWord3() throws JWNLException {
        dictionary.edit();
        testObj.getWords().add(new Word(dictionary, testObj, "test2"));
        testObj.getWords().set(0, new Word(dictionary, testObj, "test"));

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
        testObj.getWords().add(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddWordNull() throws JWNLException {
        dictionary.edit();
        testObj.setDictionary(null);
        testObj.getWords().add(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddWord2() throws JWNLException {
        testObj.getWords().add(new Word(null, new Synset(dictionary, POS.NOUN), "test"));
    }

    @Test
    public void testAddWord3() throws JWNLException {
        dictionary.edit();
        testObj.getWords().add(new Word(dictionary, testObj, "test"));
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
    public void testAddWordNullD() throws JWNLException {
        dictionary.edit();
        testObj.setDictionary(null);
        testObj.getWords().add(new Word(null, testObj, "test"));
        Assert.assertEquals(1, testObj.getWords().size());
        Assert.assertEquals("test", testObj.getWords().get(0).getLemma());
    }

    @Test
    public void testAddWordIndexedNullD() throws JWNLException {
        dictionary.edit();
        testObj.setDictionary(null);
        testObj.getWords().add(0, new Word(null, testObj, "test"));
        Assert.assertEquals(1, testObj.getWords().size());
        Assert.assertEquals("test", testObj.getWords().get(0).getLemma());
    }

    @Test
    public void testAddWord4() throws JWNLException {
        dictionary.edit();
        testObj.getWords().add(new Word(dictionary, testObj, "test"));
        IndexWord iw = dictionary.getIndexWord(testObj.getPOS(), "test");
        Assert.assertNotNull(iw);
        iw.getSenses().add(new Synset(dictionary, testObj.getPOS()));
        iw.getSenses().remove(testObj);
        testObj.getWords().add(new Word(dictionary, testObj, "test"));
        Assert.assertEquals(1, testObj.getWords().size());
        Assert.assertEquals("test", testObj.getWords().get(0).getLemma());

        List<IndexWord> indexWords = new ArrayList<IndexWord>(1);
        Iterator<IndexWord> i = dictionary.getIndexWordIterator(POS.NOUN);
        while (i.hasNext()) {
            indexWords.add(i.next());
        }
        Assert.assertEquals(1, indexWords.size());
        Assert.assertEquals("test", indexWords.get(0).getLemma());
        Assert.assertEquals(2, indexWords.get(0).getSenses().size());
        Assert.assertEquals(testObj, indexWords.get(0).getSenses().get(1));
    }

    @Test
    public void testRemoveWord() throws JWNLException {
        dictionary.edit();
        testObj.getWords().add(new Word(dictionary, testObj, "test"));
        testObj.getWords().remove(0);

        Assert.assertEquals(0, testObj.getWords().size());

        List<IndexWord> indexWords = new ArrayList<IndexWord>(1);
        Iterator<IndexWord> i = dictionary.getIndexWordIterator(POS.NOUN);
        while (i.hasNext()) {
            indexWords.add(i.next());
        }
        Assert.assertEquals(0, indexWords.size());
    }

    @Test
    public void testRemoveWordNull() throws JWNLException {
        dictionary.edit();
        testObj.setDictionary(null);
        testObj.getWords().add(new Word(null, testObj, "test"));
        testObj.getWords().remove(0);

        Assert.assertEquals(0, testObj.getWords().size());
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

    @Test(expected = IllegalArgumentException.class)
    public void testSetGlossNullD() throws JWNLException {
        dictionary.edit();
        testObj.setDictionary(null);
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
    public void testContainsWord() throws JWNLException {
        testObj.containsWord(null);
    }

    @Test
    public void testContainsWord2() throws JWNLException {
        dictionary.edit();
        Assert.assertFalse(testObj.containsWord("test"));
    }

    @Test
    public void testContainsWord2NullD() throws JWNLException {
        dictionary.edit();
        testObj.setDictionary(null);
        Assert.assertFalse(testObj.containsWord("test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIndexOfWordNull() throws JWNLException {
        testObj.indexOfWord(null);
    }

    @Test
    public void testIndexOfWordPositive() throws JWNLException {
        testObj.getWords().add(new Word(dictionary, testObj, "lemma"));
        Assert.assertEquals(0, testObj.indexOfWord("lemma"));
    }

    @Test
    public void testIndexOfWordNegative() throws JWNLException {
        Assert.assertEquals(-1, testObj.indexOfWord("lemma"));
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

    @Test(expected = IllegalArgumentException.class)
    public void testWordListAddAlien() throws JWNLException {
        dictionary.edit();
        testObj.getWords().add(new Word(mapDictionary, new Synset(mapDictionary, POS.NOUN), "test"));
    }

    @Test
    public void testWordListAddAll() throws JWNLException {
        dictionary.edit();
        Word w = new Word(dictionary, testObj, "test");
        Word ww = new Word(dictionary, testObj, "rest");

        Assert.assertTrue(testObj.getWords().addAll(Arrays.asList(w, ww)));
        Assert.assertEquals(2, testObj.getWords().size());
        Assert.assertTrue(testObj.getWords().contains(w));
        Assert.assertTrue(testObj.getWords().contains(ww));
    }

    @Test
    public void testWordListAddAllNull() throws JWNLException {
        dictionary.edit();
        testObj.setDictionary(null);
        Word w = new Word(null, testObj, "test");
        Word ww = new Word(null, testObj, "rest");

        Assert.assertTrue(testObj.getWords().addAll(Arrays.asList(w, ww)));
        Assert.assertEquals(2, testObj.getWords().size());
        Assert.assertTrue(testObj.getWords().contains(w));
        Assert.assertTrue(testObj.getWords().contains(ww));
    }

    @Test
    public void testWordListAddAllIndex() throws JWNLException {
        dictionary.edit();
        Word w = new Word(dictionary, testObj, "test");
        Word ww = new Word(dictionary, testObj, "rest");
        testObj.getWords().add(w);

        Assert.assertTrue(testObj.getWords().addAll(0, Arrays.asList(ww)));
        Assert.assertEquals(2, testObj.getWords().size());
        Assert.assertEquals(ww, testObj.getWords().get(0));
    }

    @Test
    public void testWordListAddAllIndexNull() throws JWNLException {
        dictionary.edit();
        testObj.setDictionary(null);
        Word w = new Word(null, testObj, "test");
        Word ww = new Word(null, testObj, "rest");
        testObj.getWords().add(w);

        Assert.assertTrue(testObj.getWords().addAll(0, Arrays.asList(ww)));
        Assert.assertEquals(2, testObj.getWords().size());
        Assert.assertEquals(ww, testObj.getWords().get(0));
    }

    @Test
    public void testWordListClear() throws JWNLException {
        dictionary.edit();
        Word w = new Word(dictionary, testObj, "test");
        testObj.getWords().add(w);
        testObj.getWords().clear();

        Assert.assertTrue(testObj.getWords().isEmpty());
    }

    @Test
    public void testWordListClearNull() throws JWNLException {
        dictionary.edit();
        testObj.setDictionary(null);
        Word w = new Word(null, testObj, "test");
        testObj.getWords().add(w);
        testObj.getWords().clear();

        Assert.assertTrue(testObj.getWords().isEmpty());
    }
}