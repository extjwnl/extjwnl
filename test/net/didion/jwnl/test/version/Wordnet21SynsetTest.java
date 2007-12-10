package net.didion.jwnl.test.version;

import java.io.FileInputStream;

import junit.framework.TestCase;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.test.generic.TestDefaults;

import org.junit.Test;

public class Wordnet21SynsetTest extends TestCase {

    @Test
    public void testGetBySynset() {
        try {
            JWNL.initialize(TestDefaults.getInputStream());
            IndexWord word = Dictionary.getInstance().getIndexWord(POS.NOUN, "tank");
           
            /**
             * 2.1 offset for tank. 
             */
            long offset = 4337089;
            
            Synset syn = Dictionary.getInstance().getSynsetAt(POS.NOUN, offset);
            
            boolean match = false;
            for (Word w : syn.getWords()) {
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
