package net.didion.jwnl.test.version;

import junit.framework.TestCase;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.test.generic.TestDefaults;

public class Wordnet30SynsetTest extends TestCase {

    public void testGetBySynset() {
        try {
            JWNL.initialize(TestDefaults.getInputStream());
              /**
             * 3.0 offset for tank. 
             */
            long offset = 4389033;
            
            Synset syn = Dictionary.getInstance().getSynsetAt(POS.NOUN, offset);
            System.out.println("Synset: " + syn.toString());
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
            fail("Exception in Synset 3.0 test caught");
            e.printStackTrace();
        }
        
       
    }
    
    
    /**
     * Pulls a noun "tank" from the dictionary and checks to see if it has 5 senses.
     *
     */
    public void testGetWordSenses() {
        try {
            JWNL.initialize(TestDefaults.getInputStream());
            IndexWord word = Dictionary.getInstance().getIndexWord(POS.NOUN, "tank");
      
            assertTrue(word.getSenseCount() == 5); 
            
            word = Dictionary.getInstance().getIndexWord(POS.VERB, "eat");
            assertTrue(word.getSenseCount() == 6); 
            
            word = Dictionary.getInstance().getIndexWord(POS.ADJECTIVE, "quick");
            assertTrue(word.getSenseCount() == 6); 
            
            word = Dictionary.getInstance().getIndexWord(POS.ADJECTIVE, "big");
            assertTrue(word.getSenseCount() == 13); 
            
        } catch(JWNLException e) {
            fail("Exception in testGetSenses caught");
            e.printStackTrace();
        } 
    }
    
    
    
}
