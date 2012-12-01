package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Creates a DatabaseBackedDictionary and creates all the test cases.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestReadDatabaseBackedDictionary extends DictionaryReadTester {

    /**
     * Properties location.
     */
    protected String properties = "./src/main/resources/net/sf/extjwnl/database_properties.xml";

    public void initDictionary() throws IOException, JWNLException {
        dictionary = Dictionary.getInstance(new FileInputStream(properties));
    }
}