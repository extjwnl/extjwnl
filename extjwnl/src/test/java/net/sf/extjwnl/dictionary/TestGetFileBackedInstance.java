package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

/**
 * Creates a FileBackedDictionary via .getFileBackedInstance and tests it.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestGetFileBackedInstance extends DictionaryReadTester {

    /**
     * Data files location.
     */
    private static final String location = System.getProperty("extjwnl.testDataFolder") + "/net/sf/extjwnl/data/wordnet/wn31";

    @BeforeClass
    public static void initDictionary() throws IOException, JWNLException {
        s_dictionary = Dictionary.getFileBackedInstance(location);
    }

    @AfterClass
    public static void closeDictionary() throws IOException, JWNLException {
        if (null != s_dictionary) {
            s_dictionary.close();
        }
    }
}