package net.sf.extjwnl.dictionary;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Tests FileBackedDictionary editing.
 *
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public class TestCreateFileBackedDictionary extends TestCreateDictionary {

    protected static final String properties = "./config/clean_file.xml";

    @Override
    protected InputStream getProperties() throws FileNotFoundException {
        return new FileInputStream(properties);
    }
}
