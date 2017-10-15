package net.sf.extjwnl.dictionary;

import java.io.InputStream;

/**
 * Tests MapBackedDictionary editing.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestEditMapBackedDictionary extends DictionaryEditTester {

    @Override
    protected InputStream getProperties() {
        return TestEditMapBackedDictionary.class.getResourceAsStream("/test_clean_map.xml");
    }
}
