package net.didion.jwnl.data;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Tests editing the FileBackedDictionary.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class TestFileBackedDictionaryEdit {

    protected String properties = "./config/clean_file.xml";

    @Test
    public void TestLoadEmptyDictionary() throws IOException, JWNLException {
        Dictionary dictionary = Dictionary.getInstance(new FileInputStream(properties));
        dictionary.edit();
        dictionary.save();
        dictionary.close();
        dictionary.delete();
    }
}
