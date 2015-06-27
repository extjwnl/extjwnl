package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

/**
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestReadResourceDictionary extends DictionaryReadTester {

    @BeforeClass
    public static void initDictionary() throws IOException, JWNLException {
        s_dictionary = Dictionary.getDefaultResourceInstance();
    }

    @AfterClass
    public static void closeDictionary() throws IOException, JWNLException {
        if (null != s_dictionary) {
            s_dictionary.close();
        }
    }
}