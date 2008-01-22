package net.didion.jwnl.test.generic;

import junit.framework.TestCase;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;

/**
 * Tests accessing the various sense keys and usage counts for the lemma "tank". 
 * @author brett
 *
 */
public class SenseKeyTest extends TestCase {

	public void testSimpleSenseKey() {
		try {
            JWNL.initialize(TestDefaults.getInputStream());
            IndexWord word = Dictionary.getInstance().getIndexWord(POS.VERB, "get");
			Synset[] syns = word.getSenses();
			for (int i = 0; i < syns.length; i++) {
				Synset syn = syns[i];
                System.out.println("Synset: " + syn.toString());
				for (int x = 0; x < syn.getWords().length; x++) {
					Word w = syn.getWords()[x];
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
