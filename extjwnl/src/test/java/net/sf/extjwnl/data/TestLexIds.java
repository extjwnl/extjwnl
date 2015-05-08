package net.sf.extjwnl.data;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestLexIds {

    @Test
    public void testLexIds() {
        Assert.assertEquals(3L, (long) LexFileNameFileIdMap.getMap().get("noun.Tops"));
        Assert.assertEquals("noun.Tops", LexFileIdFileNameMap.getMap().get(3L));
    }
}
