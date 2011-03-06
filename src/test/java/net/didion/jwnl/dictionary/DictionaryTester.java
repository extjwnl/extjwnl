package net.didion.jwnl.dictionary;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
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
 * @author bwalenz
 * @author Aliaksandr Autayeu avtaev@gmail.com
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
        //TODO test specific pointers
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
        int[] indices = synset.getVerbFrameIndices();
        Assert.assertNotNull(indices);
        Assert.assertEquals("Verb synset frame size test", 2, indices.length);
        Assert.assertEquals("Verb synset frame test", 2, indices[0]);
        Assert.assertEquals("Verb synset frame test", 33, indices[1]);
    }
}