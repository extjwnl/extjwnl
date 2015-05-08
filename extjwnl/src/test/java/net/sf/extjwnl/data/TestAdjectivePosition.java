package net.sf.extjwnl.data;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestAdjectivePosition {

    @Test
    public void testGetAdjectivePositionForKey() {
        Assert.assertEquals(AdjectivePosition.NONE, AdjectivePosition.getAdjectivePositionForKey("none"));
        Assert.assertEquals(AdjectivePosition.PREDICATIVE, AdjectivePosition.getAdjectivePositionForKey("p"));
        Assert.assertEquals(AdjectivePosition.ATTRIBUTIVE, AdjectivePosition.getAdjectivePositionForKey("a"));
        Assert.assertEquals(AdjectivePosition.IMMEDIATE_POSTNOMINAL, AdjectivePosition.getAdjectivePositionForKey("ip"));
        Assert.assertNull(AdjectivePosition.getAdjectivePositionForKey("h"));
    }

    @Test
    public void testGetToString() {
        Assert.assertNotNull(AdjectivePosition.NONE.toString());
    }

    @Test
    public void testGetLabel() {
        Assert.assertEquals("none", AdjectivePosition.NONE.getLabel());
    }
}
