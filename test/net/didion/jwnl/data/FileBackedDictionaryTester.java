package net.didion.jwnl.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;

/**
 * Creates a FileBackedDictionary and creates all the test cases.
 * @author bwalenz
 *
 */
public class FileBackedDictionaryTester extends DictionaryTester {
	
	/** Properties location. */
	protected String properties = "C:\\21csi\\jwnl\\jwordnet\\trunk\\jwnl\\config\\file_properties.xml";

    public FileBackedDictionaryTester(String properties) {
        this.properties = properties;
    }

    public FileBackedDictionaryTester() {
        //nop
    }

    /**
	 * {@inheritDoc}
	 */
	public void initDictionary() {
		try { 
			JWNL.initialize(new FileInputStream(properties));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JWNLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a new FileBackedDictionaryTester and runs the tests. 
	 * @param args none
	 */
	public static void main(String[] args) {
        DictionaryTester t;
        if (0 == args.length) {
            t = new FileBackedDictionaryTester();
        } else {
            t = new FileBackedDictionaryTester(args[0]);
        }
		t.initDictionary();
		t.test();
	}
	
}