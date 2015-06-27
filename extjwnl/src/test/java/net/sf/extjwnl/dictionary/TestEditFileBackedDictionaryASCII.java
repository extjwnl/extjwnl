package net.sf.extjwnl.dictionary;

import java.io.InputStream;

/**
 * Tests FileBackedDictionary editing.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestEditFileBackedDictionaryASCII extends TestEditFileBackedDictionary {


    public TestEditFileBackedDictionaryASCII() {
        second = "second";
        third = "third";
        encoding = "US-ASCII";
        physical_entityLemma = "physical entity";
        exception2 = new String[]{"aides-de-camp", "aide-de-camp"};
    }

    @Override
    protected InputStream getProperties() {
        return TestEditFileBackedDictionaryASCII.class.getResourceAsStream("/test_clean_file_ascii.xml");
    }
}
