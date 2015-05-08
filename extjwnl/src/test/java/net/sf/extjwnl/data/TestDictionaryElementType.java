package net.sf.extjwnl.data;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestDictionaryElementType {

    @Test
    public void testToString() {
        Assert.assertNotNull(DictionaryElementType.INDEX_WORD.toString());
        Assert.assertEquals("IndexWord", DictionaryElementType.INDEX_WORD.getName());
    }
}
