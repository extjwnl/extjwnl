package net.didion.jwnl.dictionary;

import net.didion.jwnl.JWNLException;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Creates a FileBackedDictionary and creates all the test cases.
 *
 * @author bwalenz
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class TestFileBackedDictionary extends DictionaryTester {

    /**
     * Properties location.
     */
    protected String properties = "./config/file_properties.xml";

    public void initDictionary() throws IOException, JWNLException {
        dictionary = Dictionary.getInstance(new FileInputStream(properties));
    }

}