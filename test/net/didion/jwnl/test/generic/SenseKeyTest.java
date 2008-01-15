package net.didion.jwnl.test.generic;

import java.io.FileInputStream;

import junit.framework.TestCase;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;

import org.junit.Test;

/**
 * Tests accessing the various sense keys and usage counts for the lemma "tank". 
 * @author brett
 *
 */
public class SenseKeyTest extends TestCase {

	@Test
	public void testSimpleSenseKey() {
		try {
            JWNL.initialize(TestDefaults.getInputStream());
            IndexWord word = Dictionary.getInstance().getIndexWord(POS.VERB, "get");
			Synset[] syns = word.getSenses();
			for (Synset syn : syns) {
                System.out.println("Synset: " + syn.toString());
				for (Word w: syn.getWords()) {
					if (w.getLemma().equals("get")) {
						 System.out.println("count: " + w.getUsageCount());
		                
					}
                   
				}
			}
			
			
		} catch(Exception e) {
            e.printStackTrace();
            fail("Exception in Sense Key test caught");
			
		}
		
		System.out.println("Sense key test passed.");
		
	}
	
}
