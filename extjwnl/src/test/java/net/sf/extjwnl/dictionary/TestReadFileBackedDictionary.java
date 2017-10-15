package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

/**
 * Creates a FileBackedDictionary and creates all the test cases.
 *
 * @author Brett Walenz (bwalenz@users.sourceforge.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestReadFileBackedDictionary extends DictionaryReadTester {

    @BeforeClass
    public static void initDictionary() throws IOException, JWNLException {
        s_dictionary = Dictionary.getInstance(
                TestReadFileBackedDictionary.class.getResourceAsStream("/test_file_properties.xml"));
    }

    @AfterClass
    public static void closeDictionary() throws IOException, JWNLException {
        if (null != s_dictionary) {
            s_dictionary.close();
        }
    }
}