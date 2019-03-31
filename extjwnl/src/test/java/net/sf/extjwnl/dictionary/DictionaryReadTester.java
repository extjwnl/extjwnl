package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.relationship.RelationshipFinder;
import net.sf.extjwnl.data.relationship.RelationshipList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * DictionaryReadTester is a test suite for dictionary methods
 * but requires an implementation of a specific dictionary to
 * function.
 *
 * @author Brett Walenz (bwalenz@users.sourceforge.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
@Ignore
public class DictionaryReadTester {

    /**
     * The offset for wn2.1.
     */
    protected final long wn21TankOffset = 4337089;

    /**
     * The offset for wn30.
     */
    protected final long wn30TankOffset = 4389033;

    /**
     * The offset for wn31.
     */
    protected final long wn31TankOffset = 4396120;

    /**
     * Synset for "complete/finish" for wn2.1
     */
    protected final long wn21VerbOffset = 479055;

    /**
     * Synset for "complete/finish" for wn3.0
     */
    protected final long wn30VerbOffset = 484166;

    /**
     * Synset for "complete/finish" for wn3.1
     */
    protected final long wn31VerbOffset = 485097;

    /**
     * Synset for "bright" (adj) for wn2.1
     */
    protected final long wn21BrightOffset = 301258;

    /**
     * Synset for "bright" (adj) for wn3.0
     */
    protected final long wn30BrightOffset = 278551;

    /**
     * Synset for "bright" (adj) for wn3.1
     */
    protected final long wn31BrightOffset = 279417;

    /**
     * Synset for "dissilient" (adj) for wn2.1
     */
    protected final long wn21DissilientOffset = 3681;

    /**
     * Synset for "dissilient" (adj) for wn3.0
     */
    protected final long wn30DissilientOffset = 3700;

    /**
     * Synset for "dissilient" (adj) for wn3.1
     */
    protected final long wn31DissilientOffset = 3699;

    /**
     * Synset for "bright" (adv) for wn2.1
     */
    protected final long wn21BrightAdvOffset = 77569;

    /**
     * Synset for "bright" (adv) for wn3.0
     */
    protected final long wn30BrightAdvOffset = 77168;

    /**
     * Synset for "bright" (adv) for wn3.1
     */
    protected final long wn31BrightAdvOffset = 77434;

    protected final List<Long> verbOffsets = Arrays.asList(wn21VerbOffset, wn30VerbOffset, wn31VerbOffset);

    protected final List<Long> nounOffsets = Arrays.asList(wn21TankOffset, wn30TankOffset, wn31TankOffset);

    protected final List<Long> adjOffsets = Arrays.asList(wn21BrightOffset, wn30BrightOffset, wn31BrightOffset);

    protected final List<Long> adjSOffsets = Arrays.asList(wn21DissilientOffset, wn30DissilientOffset, wn31DissilientOffset);

    protected final List<Long> advOffsets = Arrays.asList(wn21BrightAdvOffset, wn30BrightAdvOffset, wn31BrightAdvOffset);

    final String glossDefinition = "an enclosed armored military vehicle; has a cannon and moves on caterpillar treads";

    protected final List<String> lemmas = Arrays.asList("tank", "army tank", "armored combat vehicle", "armoured combat vehicle");

    protected final List<String> adjLemmas = Arrays.asList("bright");

    protected final List<String> advLemmas = Arrays.asList("brilliantly", "brightly", "bright");

    protected final String[] exceptions = {"bicennaries", "bicentenary", "bicentennial"};

    protected final String iteratorLemma = "stop";
    protected final String iteratorSpacedLemma = "zoom in";

    protected static Dictionary s_dictionary;
    protected Dictionary dictionary;

    @Before
    public void setup() {
        if (null != s_dictionary) {
            dictionary = s_dictionary;
        }
    }

    public DictionaryReadTester() {
    }

    public DictionaryReadTester(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Test
    public void testTank() throws JWNLException {
        IndexWord iw = dictionary.getIndexWord(POS.NOUN, "tank");
        Assert.assertNotNull("IndexWord loaded", iw);
        Synset synset = null;
        for (Synset s : iw.getSenses()) {
            if (nounOffsets.contains(s.getOffset())) {
                synset = s;
                break;
            }
        }
        Assert.assertNotNull("Synset search", synset);
        Assert.assertEquals("Pointer testing", 7, synset.getPointers().size());
        Assert.assertEquals("Synset gloss test", glossDefinition, synset.getGloss());
        for (Word w : synset.getWords()) {
            Assert.assertTrue("Synset word loading: " + w.getLemma(), lemmas.contains(w.getLemma()));
        }
        Assert.assertEquals("Use count testing", 7, synset.getWords().get(0).getUseCount());
    }

    @Test
    public void testBrightAdj() throws JWNLException {
        IndexWord iw = dictionary.getIndexWord(POS.ADJECTIVE, "bright");
        Assert.assertNotNull("IndexWord loaded", iw);
        Synset synset = null;
        for (Synset s : iw.getSenses()) {
            if (adjOffsets.contains(s.getOffset())) {
                synset = s;
                break;
            }
        }

        Assert.assertNotNull("Synset search", synset);
        Assert.assertEquals("Pointer testing", 22, synset.getPointers().size());
        for (Word w : synset.getWords()) {
            Assert.assertTrue("Synset word loading: " + w.getLemma(), adjLemmas.contains(w.getLemma()));
        }
    }

    @Test
    public void testRedAdj() throws JWNLException {
        IndexWord iw = dictionary.getIndexWord(POS.ADJECTIVE, "red");
        Assert.assertNotNull("IndexWord loaded", iw);
        Synset synset = iw.getSenses().get(0);
        Word word = synset.getWords().get(0);
        Assert.assertEquals("Use count testing", 43, word.getUseCount());
        Assert.assertEquals("Sense key testing", "red%5:00:01:chromatic:00", word.getSenseKeyWithAdjClass());
    }

    @Test
    public void testDissilientAdjS() throws JWNLException {
        IndexWord iw = dictionary.getIndexWord(POS.ADJECTIVE, "dissilient");
        Assert.assertNotNull("IndexWord loaded", iw);
        Synset synset = null;
        for (Synset s : iw.getSenses()) {
            if (adjSOffsets.contains(s.getOffset())) {
                synset = s;
                break;
            }
        }

        Assert.assertNotNull("Synset search", synset);
        Assert.assertEquals("Pointer testing", 2, synset.getPointers().size());
    }

    @Test
    public void testBrightAdv() throws JWNLException {
        IndexWord iw = dictionary.getIndexWord(POS.ADVERB, "bright");
        Assert.assertNotNull("IndexWord loaded", iw);
        Synset synset = null;
        for (Synset s : iw.getSenses()) {
            if (advOffsets.contains(s.getOffset())) {
                synset = s;
                break;
            }
        }

        Assert.assertNotNull("Synset search", synset);
        Assert.assertEquals("Pointer testing", 2, synset.getPointers().size());
        for (Word w : synset.getWords()) {
            Assert.assertTrue("Synset word loading: " + w.getLemma(), advLemmas.contains(w.getLemma()));
        }
    }

    @Test
    public void testRandomIndexWord() throws JWNLException {
        Assert.assertNotNull(dictionary.getRandomIndexWord(POS.NOUN));
    }

    @Test
    public void testComplete() throws JWNLException {
        IndexWord iw = dictionary.getIndexWord(POS.VERB, "complete");
        Assert.assertNotNull("IndexWord loaded", iw);
        Synset synset = null;
        for (Synset s : iw.getSenses()) {
            if (verbOffsets.contains(s.getOffset())) {
                synset = s;
                break;
            }
        }
        Assert.assertNotNull("Synset search", synset);
        Assert.assertEquals("Words testing", 2, synset.getWords().size());
        final Word word = synset.getWords().get(0);
        Assert.assertEquals("Use count testing", 52, word.getUseCount());
        int[] indices = synset.getVerbFrameIndices();
        Assert.assertNotNull(indices);
        Assert.assertEquals("Verb synset frame size test", 2, indices.length);
        Assert.assertEquals("Verb synset frame test", 2, indices[0]);
        Assert.assertEquals("Verb synset frame test", 33, indices[1]);
        Assert.assertTrue(synset.getVerbFrameFlags().get(2));
        Assert.assertTrue(synset.getVerbFrameFlags().get(33));
        Assert.assertEquals(2, synset.getVerbFrames().length);

        Assert.assertTrue(word instanceof Verb);
        Verb v = (Verb) word;
        Assert.assertEquals(4, v.getVerbFrameIndices().length);
        Assert.assertEquals(4, v.getVerbFrames().length);
        Assert.assertNotNull(v.toString());
    }

    @Test
    public void testCycles() throws JWNLException {
        IndexWord index = dictionary.lookupIndexWord(POS.VERB, "contain");
        List<Synset> senses = index.getSenses();
        Assert.assertTrue(2 < senses.size());
        PointerUtils.getHypernymTree(senses.get(2));
    }

    @Test
    public void testLexFileNumber() throws JWNLException {
        IndexWord iwU = dictionary.getIndexWord(POS.ADJECTIVE, "ugly");
        Assert.assertNotNull(iwU);
        Assert.assertTrue(1 < iwU.getSenses().size());
        for (Synset synset : iwU.getSenses()) {
            Assert.assertEquals(0, synset.getLexFileNum());
            Assert.assertEquals("adj.all", synset.getLexFileName());
        }
    }

    private Synset getSynsetBySenseKey(String senseKey) throws JWNLException {
        return dictionary.getWordBySenseKey(senseKey).getSynset();
    }

    @Test
    public void testAntonym() throws JWNLException, CloneNotSupportedException {
        Synset sB = getSynsetBySenseKey("beautiful%3:00:00::");
        Synset sU = getSynsetBySenseKey("ugly%3:00:00::");

        RelationshipList list = RelationshipFinder.findRelationships(sB, sU, PointerType.ANTONYM);
        Assert.assertNotNull(list);
        Assert.assertTrue(0 < list.size());
        Assert.assertEquals(PointerType.ANTONYM, list.get(0).getType());
        Assert.assertEquals(sB, list.get(0).getSourceSynset());
        Assert.assertEquals(sU, list.get(0).getTargetSynset());
    }

    @Test
    public void testExceptions() throws JWNLException {
        Exc e = dictionary.getException(POS.NOUN, exceptions[0]);
        Assert.assertNotNull(e);
        Assert.assertEquals(POS.NOUN, e.getPOS());
        Assert.assertEquals(exceptions[0], e.getLemma());
        Assert.assertEquals(2, e.getExceptions().size());
        Assert.assertEquals(exceptions[1], e.getExceptions().get(0));
        Assert.assertEquals(exceptions[2], e.getExceptions().get(1));
    }

    @Test
    public void testDerivedForms() throws JWNLException, CloneNotSupportedException {
        Synset sB = getSynsetBySenseKey("inventor%1:18:00::");
        Synset sU = getSynsetBySenseKey("invent%2:36:00::");

        RelationshipList list = RelationshipFinder.findRelationships(sU, sB, PointerType.DERIVATION);
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(PointerType.DERIVATION, list.get(0).getType());
        Assert.assertEquals(sU.getOffset(), list.get(0).getSourceSynset().getOffset());
        Assert.assertEquals(sB.getOffset(), list.get(0).getTargetSynset().getOffset());

        sB = getSynsetBySenseKey("adduct%2:35:00::");
        sU = getSynsetBySenseKey("adducent%3:00:00::");

        list = RelationshipFinder.findRelationships(sU, sB, PointerType.DERIVATION);
        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(PointerType.DERIVATION, list.get(0).getType());
        Assert.assertEquals(sU.getOffset(), list.get(0).getSourceSynset().getOffset());
        Assert.assertEquals(sB.getOffset(), list.get(0).getTargetSynset().getOffset());
    }

    @Test
    public void testRunningAway() throws JWNLException {
        IndexWord iw = dictionary.lookupIndexWord(POS.VERB, "running-away");
        Assert.assertNotNull(iw);
    }

    @Test
    public void testFairSenseKey() throws JWNLException {
        Synset synset = getSynsetBySenseKey("fair%5:00:00:feminine:01");
        Assert.assertNotNull(synset);
    }

    @Test
    public void testOnline() throws JWNLException {
        IndexWordSet iws = dictionary.lookupAllIndexWords("on-line");
        Assert.assertNotNull(iws);
        Assert.assertTrue(0 < iws.size());
        IndexWord word = dictionary.lookupIndexWord(POS.ADJECTIVE, "on-line");
        Assert.assertNotNull(word);
        iws = dictionary.lookupAllIndexWords("online");
        Assert.assertNotNull(iws);
        Assert.assertTrue(0 < iws.size());
        word = dictionary.lookupIndexWord(POS.ADJECTIVE, "online");
        Assert.assertNotNull(word);
    }

    @Test
    public void testVerbFrames() throws JWNLException {
        Synset synset = getSynsetBySenseKey("complete%2:30:02::");
        Assert.assertNotNull(synset);
        Assert.assertEquals(2, synset.getVerbFrameFlags().cardinality());
        Assert.assertTrue(synset.getVerbFrameFlags().get(2));
        Assert.assertTrue(synset.getVerbFrameFlags().get(33));
        Word w = dictionary.getWordBySenseKey("complete%2:30:02::");
        Assert.assertTrue(w instanceof Verb);
        Verb v = (Verb) w;
        Assert.assertTrue(v.getVerbFrameFlags().get(8));
        Assert.assertTrue(v.getVerbFrameFlags().get(11));
    }

    @Test
    public void testNonExistentIndexWordIterator() throws JWNLException {
        Iterator<IndexWord> it = dictionary.getIndexWordIterator(POS.VERB, "??????????????");
        Assert.assertNotNull(it);
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        Assert.assertEquals(0, count);
    }

    @Test
    public void testIndexWordIterator() throws JWNLException {
        Iterator<IndexWord> it = dictionary.getIndexWordIterator(POS.VERB, iteratorLemma);
        Assert.assertNotNull(it);
        int count = 0;
        while (it.hasNext()) {
            IndexWord iw = it.next();
            Assert.assertTrue(iw.getLemma().contains(iteratorLemma));
            count++;
        }
        Assert.assertEquals(9, count);
    }

    @Test
    public void testSpacedIndexWordIterator() throws JWNLException {
        Iterator<IndexWord> it = dictionary.getIndexWordIterator(POS.VERB, iteratorSpacedLemma);
        Assert.assertNotNull(it);
        int count = 0;
        while (it.hasNext()) {
            IndexWord iw = it.next();
            Assert.assertTrue(iw.getLemma().contains(iteratorSpacedLemma));
            count++;
        }
        Assert.assertEquals(1, count);
    }

    @Test
    public void testMorphoLookupBaseFormNull() throws JWNLException {
        Assert.assertNull(dictionary.getMorphologicalProcessor().lookupBaseForm(null, null));
    }

    @Test
    public void testMorphoLookupBaseForm() throws JWNLException {
        IndexWord iw = dictionary.getMorphologicalProcessor().lookupBaseForm(POS.NOUN, "copies");
        Assert.assertNotNull(iw);
        Assert.assertEquals("copy", iw.getLemma());
    }

    @Test
    public void testMorphoLookupAllBaseFormsNull() throws JWNLException {
        Assert.assertEquals(0, dictionary.getMorphologicalProcessor().lookupAllBaseForms(null, null).size());
    }

    @Test
    public void testMorphoLookupAllBaseForms() throws JWNLException {
        List<String> baseForms = dictionary.getMorphologicalProcessor().lookupAllBaseForms(POS.NOUN, "copies");
        Assert.assertNotNull(baseForms);
        Assert.assertEquals(1, baseForms.size());
        Assert.assertTrue(baseForms.contains("copy"));
    }

    @Test
    public void testExceptionIterator() throws JWNLException {
        Iterator<Exc> i = dictionary.getExceptionIterator(POS.NOUN);
        Assert.assertNotNull(i);
        Assert.assertTrue(i.hasNext());
        Exc e = i.next();
        Assert.assertNotNull(e);
        Assert.assertNotNull(e.getLemma());
        Assert.assertNotNull(e.getExceptions());
        Assert.assertTrue(0 < e.getExceptions().size());
        Assert.assertEquals(POS.NOUN, e.getPOS());
        Assert.assertEquals(DictionaryElementType.EXCEPTION, e.getType());

        e = i.next();
        Assert.assertNotNull(e);
        Assert.assertNotNull(e.getLemma());
        Assert.assertNotNull(e.getExceptions());
        Assert.assertTrue(0 < e.getExceptions().size());
        Assert.assertEquals(POS.NOUN, e.getPOS());
        Assert.assertEquals(DictionaryElementType.EXCEPTION, e.getType());

        i = dictionary.getExceptionIterator(POS.VERB);
        Assert.assertNotNull(i);
        Assert.assertTrue(i.hasNext());
        e = i.next();
        Assert.assertNotNull(e);
        Assert.assertNotNull(e.getLemma());
        Assert.assertNotNull(e.getExceptions());
        Assert.assertTrue(0 < e.getExceptions().size());
        Assert.assertEquals(POS.VERB, e.getPOS());
        Assert.assertEquals(DictionaryElementType.EXCEPTION, e.getType());

        e = i.next();
        Assert.assertNotNull(e);
        Assert.assertNotNull(e.getLemma());
        Assert.assertNotNull(e.getExceptions());
        Assert.assertTrue(0 < e.getExceptions().size());
        Assert.assertEquals(POS.VERB, e.getPOS());
        Assert.assertEquals(DictionaryElementType.EXCEPTION, e.getType());
    }

    @Test
    public void testSynsetIterator() throws JWNLException {
        Iterator<Synset> i = dictionary.getSynsetIterator(POS.NOUN);
        Assert.assertNotNull(i);
        Assert.assertTrue(i.hasNext());
        Synset s = i.next();
        Assert.assertNotNull(s);
        Assert.assertNotNull(s.getGloss());
        Assert.assertNotNull(s.getOffset());
        Assert.assertEquals(POS.NOUN, s.getPOS());
        Assert.assertEquals(DictionaryElementType.SYNSET, s.getType());

        s = i.next();
        Assert.assertNotNull(s);
        Assert.assertNotNull(s.getGloss());
        Assert.assertNotNull(s.getOffset());
        Assert.assertEquals(POS.NOUN, s.getPOS());
        Assert.assertEquals(DictionaryElementType.SYNSET, s.getType());

        i = dictionary.getSynsetIterator(POS.VERB);
        Assert.assertNotNull(i);
        Assert.assertTrue(i.hasNext());
        s = i.next();
        Assert.assertNotNull(s);
        Assert.assertNotNull(s.getGloss());
        Assert.assertNotNull(s.getOffset());
        Assert.assertEquals(POS.VERB, s.getPOS());
        Assert.assertEquals(DictionaryElementType.SYNSET, s.getType());

        s = i.next();
        Assert.assertNotNull(s);
        Assert.assertNotNull(s.getGloss());
        Assert.assertNotNull(s.getOffset());
        Assert.assertEquals(POS.VERB, s.getPOS());
        Assert.assertEquals(DictionaryElementType.SYNSET, s.getType());
    }

    @Test
    public void testGetWordBySenseKeyNoPercent() throws JWNLException {
        Assert.assertNull(dictionary.getWordBySenseKey(""));
    }

    @Test
    public void testGetWordBySenseKeyPercentOnly() throws JWNLException {
        Assert.assertNull(dictionary.getWordBySenseKey("%"));
    }

    @Test
    public void testGetWordBySenseKeyNoSynsetType() throws JWNLException {
        Assert.assertNull(dictionary.getWordBySenseKey("%:"));
    }

    @Test
    public void testGetWordBySenseKeyBadSynsetType() throws JWNLException {
        Assert.assertNull(dictionary.getWordBySenseKey("%9:"));
    }

    @Test
    public void testGetWordBySenseKeyMissingWord() throws JWNLException {
        Assert.assertNull(dictionary.getWordBySenseKey("©%1:"));
    }

    protected void runAllTests() throws JWNLException, CloneNotSupportedException {
        testTank();
        testBrightAdj();
        testDissilientAdjS();
        testBrightAdv();
        testRandomIndexWord();
        testComplete();
        testCycles();
        testLexFileNumber();
        testAntonym();
        testExceptions();
        testDerivedForms();
        testRunningAway();
        testFairSenseKey();
        testOnline();
        testVerbFrames();
        testNonExistentIndexWordIterator();
        testIndexWordIterator();
        testSpacedIndexWordIterator();
        testMorphoLookupBaseFormNull();
        testMorphoLookupBaseForm();
        testMorphoLookupAllBaseFormsNull();
        testMorphoLookupAllBaseForms();
        testExceptionIterator();
        testSynsetIterator();
        testGetWordBySenseKeyNoPercent();
    }
}
