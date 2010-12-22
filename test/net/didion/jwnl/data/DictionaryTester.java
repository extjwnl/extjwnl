package net.didion.jwnl.data;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DictionaryTester is a test suite for dictionary methods
 * but requires an implementation of a specific dictionary to
 * function.
 *
 * @author bwalenz
 */
public abstract class DictionaryTester {

    /**
     * The number of failures.
     */
    protected int failures = 0;

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

    protected long[] verbOffsets = {wn20VerbOffset, wn21VerbOffset, wn30VerbOffset};

    protected long[] nounOffsets = {wn20TankOffset, wn21TankOffset, wn30TankOffset};

    /**
     * The number of pointers the target synset has.
     */
    protected long pointers = 7;

    /**
     * Our logger.
     */
    Log log = LogFactory.getLog("jwnl.tests");

    String glossDefinition = "an enclosed armored military vehicle; has a cannon and moves on caterpillar treads";

    String[] lemmas = {"tank", "army_tank", "armored_combat_vehicle", "armoured_combat_vehicle"};


    /**
     * Inits the dictionary.
     */
    public abstract void initDictionary();

    /**
     * Tests IndexWord creation and Synset functionality.
     */
    public void test() {
        try {
            log.info("Beginning DictionaryTester...");
            IndexWord w = Dictionary.getInstance().getIndexWord(POS.NOUN, "tank");
            testIndexWord(w);

            IndexWord v = Dictionary.getInstance().getIndexWord(POS.VERB, "complete");
            testVerb(v);


        } catch (JWNLException e) {
            e.printStackTrace();
        }
        if (failures == 0) {
            log.info("Testing succeeded with no failures.");
        }
    }

    public void testVerb(IndexWord verbIndex) throws JWNLException {
        boolean verbTest = true;
        Synset[] syns = verbIndex.getSenses();

        boolean match = false;
        Synset synset = null;
        for (Synset s : syns) {
            match = contains(s, verbOffsets);
            if (match) {
                synset = s;
                break;
            }

        }
        if (match) {
            int[] indices = synset.getVerbFrameIndices();
            if (indices.length == 2) {
                log.info("Verb synset frame size test... passed.");
            } else {
                verbTest = false;
                fail("Verb synset frame size test... failed");
            }
            for (int index : indices) {
                if (index != 2 && index != 33) {
                    verbTest = false;
                    fail("Verb synset frame flags... failed");
                }
            }

        } else {
            verbTest = false;
            fail("Verb synset test... failed.");
        }
        if (verbTest) {
            log.info("Verb test... passed.");
        } else {
            fail("Verb tests... failed.");
        }

    }

    /**
     * Tests whether or not the index word has the offsets defined.
     *
     * @param iw index word
     * @throws JWNLException exception on loading
     */
    public void testIndexWord(IndexWord iw) throws JWNLException {
        boolean containsSynset = false;
        Synset armoredTankSynset = null;
        for (int i = 0; i < iw.getSenses().length; i++) {
            Synset s = iw.getSenses()[i];
            boolean found = testSynset(s);
            if (found) {
                containsSynset = true;
                armoredTankSynset = s;
            }
        }
        if (!containsSynset) {
            fail("IndexWord loading and Synset testing... failed.");
            return;
        }

        log.info("IndexWord loading and Synset testing... passed.");

        testPointers(armoredTankSynset);
        testGloss(armoredTankSynset);
        testWords(armoredTankSynset);
        //TODO test specific pointers


    }

    /**
     * Tests the words in the synset.
     *
     * @param synset synset
     * @return true if passed test
     * @throws JWNLException exception on loading
     */
    public boolean testWords(Synset synset) throws JWNLException {
        boolean result = true;
        for (int i = 0; i < synset.getWords().length; i++) {
            Word w = synset.getWords()[i];
            boolean found = false;
            for (String lemma : lemmas) {
                if (w.getLemma().equals(lemma)) {
                    found = true;
                }
            }
            if (!found) {
                fail("Synset word loading... failed.");
                result = false;
            }
        }
        if (result) {
            log.info("Synset word loading... passed.");
        }
        return result;
    }

    /**
     * Tests the gloss of the synset.
     *
     * @param synset synset
     * @return true if gloss matches definition
     * @throws JWNLException exception in loading
     */
    public boolean testGloss(Synset synset) throws JWNLException {
        boolean match = false;
        if (synset.getGloss().trim().equals(glossDefinition)) {
            match = true;
            log.info("Synset gloss test... passed.");
        } else {
            fail("Synset gloss test... failed.");
        }
        return match;
    }

    /**
     * Tests if the synset contains the offset.
     *
     * @param synset synset
     * @return true if synset has defined offset
     * @throws JWNLException exception in loading
     */
    public boolean testSynset(Synset synset) throws JWNLException {
        boolean found = false;
        if (JWNL.getVersion().getNumber() == 2.0) {
            if (synset.getOffset() == wn20TankOffset) {
                found = true;
            }
        } else if (JWNL.getVersion().getNumber() == 2.1) {
            if (synset.getOffset() == wn21TankOffset) {
                found = true;
            }
        } else if (JWNL.getVersion().getNumber() == 3.0) {
            if (synset.getOffset() == wn30TankOffset) {
                found = true;
            }
        }
        return found;
    }

    /**
     * Tests if the synset has the desired number of pointers.
     *
     * @param synset synset
     */
    public void testPointers(Synset synset) {
        if (!(synset.getPointers().length == pointers)) {
            fail("Pointer testing... failed. Info: " + synset.getPointers().length + " should be: " + pointers);
        } else {
            log.info("Pointer testing... passed.");
        }
    }

    /**
     * Simple utility function to match a synset with a collection of offsets.
     *
     * @param s       synset
     * @param offsets offsets
     * @return true if match
     */
    private boolean contains(Synset s, long[] offsets) {
        boolean rval = false;
        for (long offset : offsets) {
            if (s.getOffset() == offset) {
                rval = true;
            }
        }
        return rval;
    }

    /**
     * Utility function.
     *
     * @param testName
     */
    public void fail(String testName) {
        failures++;
        log.error(testName);
    }

}
