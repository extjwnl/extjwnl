package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;
import org.junit.After;
import org.junit.Before;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Base class for tests.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class BaseData {

    private final String properties = "./src/test/resources/clean_file.xml";
    private final String mapProperties = "./src/test/resources/clean_map.xml";

    protected Dictionary dictionary;
    protected Dictionary mapDictionary;

    protected InputStream getProperties() throws IOException {
        return new FileInputStream(properties);
    }

    protected InputStream getMapProperties() throws IOException {
        return new FileInputStream(mapProperties);
    }

    @Before
    public void setUp() throws JWNLException, IOException {
        dictionary = Dictionary.getInstance(getProperties());
        mapDictionary = Dictionary.getInstance(getMapProperties());

        dictionary.close();
        dictionary.delete();

        mapDictionary.close();
        mapDictionary.delete();

        dictionary = Dictionary.getInstance(getProperties());
        mapDictionary = Dictionary.getInstance(getMapProperties());
    }

    @After
    public void tearDown() throws JWNLException {
        dictionary.close();
        dictionary.delete();

        mapDictionary.close();
        mapDictionary.delete();
    }
}