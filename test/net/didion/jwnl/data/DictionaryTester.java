package net.didion.jwnl.data;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DictionaryTester is a test suite for dictionary methods
 * but requires an implementation of a specific dictionary to
 * function. 
 * @author bwalenz
 *
 */
public abstract class DictionaryTester {
	
	/** The number of failures. */ 
	protected int failures = 0;
	
	/** The offset for wn30. */ 
	protected long wn30TankOffset = 4389033;
	
	/** The offset for wn2.1. */ 
	protected long wn21TankOffset = 4337089;
	
	/** The offset for wn 2.0. */
	protected long wn20TankOffset = 4219085;
	
	/** The number of pointers the target synset has. */ 
	protected long pointers = 7;
	
	/** Our logger. */ 
	Log log = LogFactory.getLog("jwnl.tests");
	
	/** 
	 * Inits the dictionary. 
	 */
	public abstract void initDictionary();
	
	/** 
	 * Tests IndexWord creation and Synset functionality. 
	 */
	public void test() {
		try {
			log.info("Beginning DictionaryTester...");
			IndexWord w = Dictionary.getInstance().getIndexWord(POS.NOUN, "tank");
			testIndexWord(w);		
			
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		if (failures == 0) {
			log.info("Testing succeeded with no failures.");
		}
	}

	/**
	 * Tests whether or not the index word has the offsets defined. 
	 * @param iw index word
	 * @throws JWNLException exception on loading
	 */
	public void testIndexWord(IndexWord iw) throws JWNLException {
		boolean containsSynset = false;
		Synset armoredTankSynset = null;
		for (int i = 0; i < iw.getSenses().length; i++) {
			Synset s = iw.getSenses()[i];
			boolean found = testSynset(s);
			if (found) {
				containsSynset = true;
				armoredTankSynset = s;
			}
		}
		if (!containsSynset) {
			fail("IndexWord loading and Synset testing... failed.");
			return;
		}
		
		log.info("IndexWord loading and Synset testing... passed.");
		
		testPointers(armoredTankSynset);
		//TODO test synset gloss
		//TODO test specific pointers
		//TODO test Word objects in Synset
		
	}
	
	/**
	 * Tests if the synset contains the offset. 
	 * @param synset synset 
	 * @return true if synset has defined offset
	 * @throws JWNLException exception in loading
	 */
	public boolean testSynset(Synset synset) throws JWNLException {
		boolean found = false;
		if (JWNL.getVersion().getNumber() == 2.0) {
			if (synset.getOffset() == wn20TankOffset) {
				found = true;
			}
		} else if (JWNL.getVersion().getNumber() == 2.1) {
			if (synset.getOffset() == wn21TankOffset) {
				found = true;
			}
		} else if (JWNL.getVersion().getNumber() == 3.0) {
			if (synset.getOffset() == wn30TankOffset) {
				found = true;
			}
		}
		return found;
	}
	
	/**
	 * Tests if the synset has the desired number of pointers. 
	 * @param synset synset 
	 */
	public void testPointers(Synset synset) {
		if (!(synset.getPointers().length == pointers)) {
			fail("Pointer testing... failed. Info: " + synset.getPointers().length + " should be: " + pointers);
		} else {
			log.info("Pointer testing... passed.");
		}
	}
	
	/**
	 * Utility function. 
	 * @param testName
	 */
	public void fail(String testName) {
		failures++;
		log.error(testName);
	}
	
}
