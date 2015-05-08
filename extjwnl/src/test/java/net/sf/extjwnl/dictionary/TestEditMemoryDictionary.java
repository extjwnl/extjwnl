package net.sf.extjwnl.dictionary;

import java.io.InputStream;

/**
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestEditMemoryDictionary extends DictionaryEditTester {

    @Override
    protected InputStream getProperties() {
        return Dictionary.class.getResourceAsStream("/net/sf/extjwnl/dictionary/mem_properties.xml");
    }
}
