package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.relationship.Relationship;
import net.sf.extjwnl.data.relationship.RelationshipFinder;
import net.sf.extjwnl.data.relationship.RelationshipList;
import net.sf.extjwnl.dictionary.Dictionary;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests RelationshipList.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestRelationshipList {

    private static Dictionary dic;

    @BeforeClass
    public static void runOnceBeforeAllTests() throws FileNotFoundException, JWNLException {
        dic = Dictionary.getInstance(new FileInputStream("./src/main/resources/net/sf/extjwnl/file_properties.xml"));
    }

    @Test
    public void testGetShallowest() throws JWNLException, CloneNotSupportedException {
        Word sW = dic.getWordBySenseKey("dog%1:05:00::");
        Word tW = dic.getWordBySenseKey("man%1:05:00::");
        assertNotNull("source is not found", sW);
        assertNotNull("target is not found", tW);
        RelationshipList result = RelationshipFinder.findRelationships(sW.getSynset(), tW.getSynset(), PointerType.HYPERNYM);
        assertNotNull("relationships are not found", result);
        assertEquals("getDepth is wrong", 16, result.get(1).getDepth());
        assertEquals("getDeepest is wrong", 16, result.getDeepest().getDepth());
        assertEquals("getDepth is wrong", 7, result.get(0).getDepth());
        assertEquals("getDepth is wrong", 7, result.getShallowest().getDepth());
    }
}
