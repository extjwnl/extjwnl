package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import org.junit.Assert;
import org.junit.Test;

import java.util.BitSet;

/**
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestVerbSynset extends BaseData {

    @Test
    public void testConstructor() throws JWNLException {
        VerbSynset verbSynset = new VerbSynset(dictionary);
        Assert.assertNotNull(verbSynset.getVerbFrameFlags());
    }

    @Test
    public void testGetVerbFrames() throws JWNLException {
        VerbSynset verbSynset = new VerbSynset(dictionary, 0);
        Assert.assertNotNull(verbSynset.getVerbFrameFlags());
        Assert.assertNotNull(verbSynset.getVerbFrames());
    }

    @Test
    public void testGetVerbFrames2() throws JWNLException {
        BitSet verbFrameFlags = new BitSet(1);
        verbFrameFlags.set(1);
        Synset s = new VerbSynset(null);
        s.setVerbFrameFlags(verbFrameFlags);
        String[] frames = s.getVerbFrames();
        Assert.assertNotNull(frames);
        Assert.assertEquals(1, frames.length);
        Assert.assertEquals(Verb.frames[0], frames[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetVerbFrames() throws JWNLException {
        VerbSynset verbSynset = new VerbSynset(dictionary, 0);
        verbSynset.setVerbFrameFlags(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetVerbFrames2() throws JWNLException {
        VerbSynset verbSynset = new VerbSynset(null);
        verbSynset.setVerbFrameFlags(null);
    }
}