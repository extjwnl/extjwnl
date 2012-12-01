package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;

import java.io.IOException;

/**
 * Creates a FileBackedDictionary via .getFileBackedInstance and tests it.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestGetFileBackedInstance extends DictionaryReadTester {

    /**
     * Data files location.
     */
    protected String location = "./data/wn30";

    public void initDictionary() throws IOException, JWNLException {
        dictionary = Dictionary.getFileBackedInstance(location);
    }
}