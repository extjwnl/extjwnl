package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.relationship.RelationshipFinder;
import net.sf.extjwnl.data.relationship.RelationshipList;
import net.sf.extjwnl.dictionary.Dictionary;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileNotFoundException;

/**
 * Tests RelationshipList.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestRelationshipList {

    private static Dictionary dic;

    @BeforeClass
    public static void runOnceBeforeAllTests() throws FileNotFoundException, JWNLException {
        dic = Dictionary.getInstance(TestRelationshipList.class.getResourceAsStream("/test_file_properties.xml"));
    }

    @Test
    public void testGetShallowest() throws JWNLException, CloneNotSupportedException {
        Word sW = dic.getWordBySenseKey("dog%1:05:00::");
        Word tW = dic.getWordBySenseKey("man%1:05:00::");
        Assert.assertNotNull("source is not found", sW);
        Assert.assertNotNull("target is not found", tW);
        RelationshipList result = RelationshipFinder.findRelationships(sW.getSynset(), tW.getSynset(), PointerType.HYPERNYM);
        Assert.assertNotNull("relationships are not found", result);
        Assert.assertEquals("getDepth is wrong", 16, result.get(1).getDepth());
        Assert.assertEquals("getDeepest is wrong", 16, result.getDeepest().getDepth());
        Assert.assertEquals("getDepth is wrong", 7, result.get(0).getDepth());
        Assert.assertEquals("getDepth is wrong", 7, result.getShallowest().getDepth());
    }
}
