package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestVerbSynset extends BaseData {

    @Test
    public void testGetVerbFrames() throws JWNLException {
        VerbSynset verbSynset = new VerbSynset(dictionary, 0);
        Assert.assertNotNull(verbSynset.getVerbFrameFlags());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetVerbFrames() throws JWNLException {
        VerbSynset verbSynset = new VerbSynset(dictionary, 0);
        verbSynset.setVerbFrameFlags(null);
    }
}