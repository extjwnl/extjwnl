package net.didion.jwnl.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Base class for tests.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class BaseDictionaryTest {

    private final String properties = "./config/clean_properties.xml";

    protected InputStream getProperties() throws IOException {
        return new FileInputStream(properties);
    }

}
