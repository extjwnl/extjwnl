package net.didion.jwnl.test.version;

import junit.framework.TestCase;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.test.generic.TestDefaults;

public class Wordnet21SynsetTest extends TestCase {

   public void testGetBySynset() {
        try {
            JWNL.initialize(TestDefaults.getInputStream());
          
            /**
             * 2.1 offset for tank. 
             */
            long offset = 4337089;
            
            Synset syn = Dictionary.getInstance().getSynsetAt(POS.NOUN, offset);
            
            boolean match = false;
            for (int i = 0; i < syn.getWords().length; i++) {
            	Word w = syn.getWords()[i];
                if (w.getLemma().equals("tank")) {
                    match = true;
                    break;
                }
            }
            
            if (!match) {
                fail("Term 'tank' not found in test grab.");
            }
            
        } catch(Exception e) {
            fail("Exception in Synset 2.1 test caught");
            e.printStackTrace();
        }
        
        System.out.println("Synset 2.1 test passed.");
        
    }
}
