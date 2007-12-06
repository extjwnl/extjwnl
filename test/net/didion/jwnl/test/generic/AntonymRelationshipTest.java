package net.didion.jwnl.test.generic;

import java.io.FileInputStream;
import java.util.Iterator;

import junit.framework.TestCase;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.relationship.RelationshipFinder;
import net.didion.jwnl.data.relationship.RelationshipList;
import net.didion.jwnl.data.relationship.SymmetricRelationship;
import net.didion.jwnl.dictionary.Dictionary;

import org.junit.Test;

public class AntonymRelationshipTest extends TestCase {
    
    @Test
    public void testSimpleSenseKey() {
        try {
            JWNL.initialize(new FileInputStream("C:\\21csi\\workspaces\\hicin-data-translator\\com.p21csi.lib.net.didion.jwnl\\config\\file_properties.xml"));
            IndexWord beautiful = Dictionary.getInstance().getIndexWord(POS.ADJECTIVE, "beautiful");
            IndexWord ugly = Dictionary.getInstance().getIndexWord(POS.ADJECTIVE, "ugly");
            
            System.out.println("beautiful count: " + beautiful.getSenseCount());
            Synset[] bea = beautiful.getSenses();
            Synset beaSynset = bea[0];
            
            Synset[] ug = ugly.getSenses();
            Synset uglySynset = ug[0];
            
            if (beaSynset != null && uglySynset != null) {
                System.out.println(beaSynset.toString());
                System.out.println(uglySynset.toString());
                RelationshipList list = RelationshipFinder.getInstance().findRelationships(beaSynset, uglySynset, PointerType.ANTONYM);
                Iterator i = list.iterator();
                while (i.hasNext()) {
                    
                    //PointerTargetNode ptr = (PointerTargetNode)i.next();
                    SymmetricRelationship s = (SymmetricRelationship)i.next();
                    /**
                     * There should be only ONE relationship found at this point, and it should match the ugly synset.
                     */
                    if (!s.getTargetSynset().equals(uglySynset)) {
                        fail("Unmatched Synsets in relationship finder test");
                    } 
                }
            }
            
            
        } catch(Exception e) {
           
            e.printStackTrace();
            fail("Exception in Relationship Finder test caught");
        }
        
        System.out.println("RelationshipFinder test passed.");
        
    }
}
