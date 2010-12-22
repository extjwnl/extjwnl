package net.didion.jwnl.data;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bwalenz
 */
public class TestDictionaryElementType extends TestCase {

    /**
     * Internal types list.
     */
    List types;

    /* (non-Javadoc)
      * @see junit.framework.TestCase#setUp()
      */
    protected void setUp() throws Exception {
        types = new ArrayList();
        types.add("EXCEPTION");
        types.add("INDEX_WORD");
        types.add("SYNSET");
    }

    /**
     * Test method for {@link net.didion.jwnl.data.DictionaryElementType#getAllDictionaryElementTypes()}.
     */
    public void testGetAllDictionaryElementTypes() {
        List types = DictionaryElementType.getAllDictionaryElementTypes();

        assertTrue(types.size() == 3);
    }


    /**
     * Test method for {@link net.didion.jwnl.data.DictionaryElementType#getName()}.
     */
    public void testGetName() {
        List types = DictionaryElementType.getAllDictionaryElementTypes();

        for (int i = 0; i < types.size(); i++) {
            DictionaryElementType type = (DictionaryElementType) types.get(i);
            if (!this.types.contains(type.getName())) {
                fail("Type definitions");
            }
        }

    }

}
