package net.didion.jwnl.data;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bwalenz
 */
@SuppressWarnings({"FieldCanBeLocal", "FieldCanBeLocal"})
public class TestExc extends TestCase {

    /**
     * The private test object.
     */
    private Exc testObj;

    /**
     * Lemma representing this exception.
     */
    private String lemma = "alam";

    /**
     * List of exceptions from the lemma.
     */
    private List exceptions;

    /**
     * First notional exception.
     */
    private String exc1 = "exc1";

    /* (non-Javadoc)
      * @see junit.framework.TestCase#setUp()
      */
    protected void setUp() throws Exception {
        exceptions = new ArrayList();
        exceptions.add(exc1);
        exceptions.add("exc2");
        testObj = new Exc(POS.NOUN, lemma, exceptions);
    }

    /**
     * Test method for {@link net.didion.jwnl.data.Exc#getPOS()}.
     */
    public void testGetPOS() {
        assertTrue(testObj.getPOS().equals(POS.NOUN));
    }

    /**
     * Test method for {@link net.didion.jwnl.data.Exc#getLemma()}.
     */
    public void testGetLemma() {
        assertTrue(testObj.getLemma().equals(lemma));
    }

    /**
     * Test method for {@link net.didion.jwnl.data.Exc#getException(int)}.
     */
    public void testGetException() {
        assertTrue(testObj.getException(0).equals(exc1));
    }

    /**
     * Test method for {@link net.didion.jwnl.data.Exc#getExceptionsSize()}.
     */
    public void testGetExceptionsSize() {
        assertTrue(testObj.getExceptionsSize() == 2);
    }

}
