package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

/**
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestReadFileChannelBackedDictionary extends DictionaryReadTester {

    @BeforeClass
    public static void initDictionary() throws IOException, JWNLException {
        s_dictionary = Dictionary.getInstance(
                TestReadFileChannelBackedDictionary.class.getResourceAsStream("/test_file_channel_properties.xml"));
    }

    @AfterClass
    public static void closeDictionary() throws IOException, JWNLException {
        if (null != s_dictionary) {
            s_dictionary.close();
        }
    }
}