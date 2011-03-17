package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.relationship.RelationshipFinder;
import net.sf.extjwnl.data.relationship.RelationshipList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * DictionaryTester is a test suite for dictionary methods
 * but requires an implementation of a specific dictionary to
 * function.
 *
 * @author Brett Walenz <bwalenz@users.sourceforge.net>
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public abstract class DictionaryTester {

    /**
     * The offset for wn30.
     */
    protected long wn30TankOffset = 4389033;

    /**
     * The offset for wn2.1.
     */
    protected long wn21TankOffset = 4337089;

    /**
     * The offset for wn 2.0.
     */
    protected long wn20TankOffset = 4219085;

    /**
     * Synset for "complete/finish" for wn2.0
     */
    protected long wn20VerbOffset = 470712;

    /**
     * Synset for "complete/finish" for wn2.1
     */
    protected long wn21VerbOffset = 479055;

    /**
     * Synset for "complete/finish" for wn3.0
     */
    protected long wn30VerbOffset = 484166;

    protected List<Long> verbOffsets = Arrays.asList(wn20VerbOffset, wn21VerbOffset, wn30VerbOffset);

    protected List<Long> nounOffsets = Arrays.asList(wn20TankOffset, wn21TankOffset, wn30TankOffset);

    String glossDefinition = "an enclosed armored military vehicle; has a cannon and moves on caterpillar treads";

    protected List<String> lemmas = Arrays.asList("tank", "army tank", "armored combat vehicle", "armoured combat vehicle");

    protected String[] exceptions = {"bicennaries", "bicentenary", "bicentennial"};

    protected Dictionary dictionary;

    @Before
    public abstract void initDictionary() throws IOException, JWNLException;

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
        Assert.assertEquals("Use count testing", 52, synset.getWords().get(0).getUseCount());
        int[] indices = synset.getVerbFrameIndices();
        Assert.assertNotNull(indices);
        Assert.assertEquals("Verb synset frame size test", 2, indices.length);
        Assert.assertEquals("Verb synset frame test", 2, indices[0]);
        Assert.assertEquals("Verb synset frame test", 33, indices[1]);
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

        RelationshipList list = RelationshipFinder.findRelationships(sU, sB, PointerType.NOMINALIZATION);
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
}