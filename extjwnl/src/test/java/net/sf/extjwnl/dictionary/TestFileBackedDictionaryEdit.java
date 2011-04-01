package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import org.junit.Test;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Tests editing the FileBackedDictionary.
 *
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public class TestFileBackedDictionaryEdit {

    protected String properties = "./src/test/config/clean_file.xml";

    @Test
    public void TestLoadEmptyDictionary() throws IOException, JWNLException, InterruptedException {
        Dictionary dictionary = Dictionary.getInstance(new FileInputStream(properties));
        dictionary.edit();
        dictionary.save();
        dictionary.close();
        dictionary.delete();
    }
}