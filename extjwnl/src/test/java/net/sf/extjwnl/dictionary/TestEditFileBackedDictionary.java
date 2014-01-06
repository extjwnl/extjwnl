package net.sf.extjwnl.dictionary;

import java.io.InputStream;

/**
 * Tests FileBackedDictionary editing.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestEditFileBackedDictionary extends DictionaryEditTester {

    @Override
    protected InputStream getProperties() {
        return TestEditFileBackedDictionary.class.getResourceAsStream("/test_clean_file.xml");
    }
}
