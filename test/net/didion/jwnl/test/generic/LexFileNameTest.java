package net.didion.jwnl.test.generic;

import junit.framework.TestCase;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;

public class LexFileNameTest extends TestCase {

	public void testLexFileName() {
		try {  
			JWNL.initialize(TestDefaults.getInputStream());
			IndexWord iw = Dictionary.getInstance().lookupIndexWord(POS.NOUN, "arm");
          
			Synset[] s = iw.getSenses();
			for (int i = 0; i < s.length; i++) {
				Synset syn = s[i];
				String lexFileName = syn.getLexFileName();
				System.out.println(syn.toString());
				System.out.println(lexFileName);
			}
			
			iw = Dictionary.getInstance().lookupIndexWord(POS.VERB, "run");
	          
			s = iw.getSenses();
			for (int i = 0; i < s.length; i++) {
				Synset syn = s[i];
				String lexFileName = syn.getLexFileName();
				System.out.println(syn.toString());
				System.out.println(lexFileName);
			}
			
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		
		
	}
	
}
