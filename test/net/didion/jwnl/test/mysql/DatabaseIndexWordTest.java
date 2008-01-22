package net.didion.jwnl.test.mysql;

import junit.framework.TestCase;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.test.generic.TestDefaults;

public class DatabaseIndexWordTest extends TestCase {
    
    public void testMySQLAccess() {
    try {
        
    JWNL.initialize(TestDefaults.getInputStream());
         
    IndexWord iw = Dictionary.getInstance().getIndexWord(POS.NOUN, "tank");
    
    Synset[] senses = iw.getSenses();
    assertTrue(senses.length > 0);
   
} catch(Exception e) {
    fail("Exception in Database test caught.");
    e.printStackTrace();
}
    }

}
