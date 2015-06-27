package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import org.junit.Assert;
import org.junit.Test;

import java.util.BitSet;

/**
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestVerb extends BaseData {

    @Test
    public void testGetVerbFrames() throws JWNLException {
        Synset s = new Synset(null, POS.NOUN);
        BitSet verbFrameFlags = new BitSet(1);
        verbFrameFlags.set(1);
        Verb v = new Verb(null, s, "go", verbFrameFlags);
        String[] frames = v.getVerbFrames();
        Assert.assertNotNull(frames);
        Assert.assertEquals(1, frames.length);
        Assert.assertEquals(Verb.frames[0], frames[0]);
    }
}
