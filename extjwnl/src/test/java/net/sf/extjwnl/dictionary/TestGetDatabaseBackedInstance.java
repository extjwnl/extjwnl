package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;

import java.io.IOException;

/**
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestGetDatabaseBackedInstance extends DictionaryReadTester {

    /**
     * Data files location.
     */
    protected String location = "jdbc:mysql://localhost/jwnl?user=root";

    public void initDictionary() throws IOException, JWNLException {
        dictionary = Dictionary.getDatabaseBackedInstance(location);
    }
}