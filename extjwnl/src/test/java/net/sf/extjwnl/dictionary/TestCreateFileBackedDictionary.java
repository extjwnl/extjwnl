package net.sf.extjwnl.dictionary;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Tests FileBackedDictionary editing.
 *
 * @author Aliaksandr Autayeu <aliaksandr@autayeu.com>
 */
public class TestCreateFileBackedDictionary extends TestCreateDictionary {

    protected static final String properties = "./src/test/resources/clean_file.xml";

    @Override
    protected InputStream getProperties() throws FileNotFoundException {
        return new FileInputStream(properties);
    }
}
