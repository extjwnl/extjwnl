package net.sf.extjwnl.data;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Brett Walenz <bwalenz@users.sourceforge.net>
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public class TestDictionaryElementType {

    @Test
    public void testGetAllDictionaryElementTypes() {
        Assert.assertEquals(3, DictionaryElementType.getAllDictionaryElementTypes().size());
    }

    @Test
    public void testGetName() {
        List<String> types = new ArrayList<String>();
        types.add("EXCEPTION");
        types.add("INDEX_WORD");
        types.add("SYNSET");

        for (DictionaryElementType type : DictionaryElementType.getAllDictionaryElementTypes()) {
            Assert.assertTrue(types.contains(type.getName()));
        }
    }
}