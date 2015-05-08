package net.sf.extjwnl.data;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestPointerType {

    @Test
    public void testIsSymmetric() {
        Assert.assertTrue(PointerType.ANTONYM.isSymmetric());
        Assert.assertFalse(PointerType.CAUSE.isSymmetric());

        Assert.assertTrue(PointerType.isSymmetric(PointerType.ANTONYM));

        Assert.assertTrue(PointerType.ANTONYM.symmetricTo(PointerType.ANTONYM));
        Assert.assertFalse(PointerType.ANTONYM.symmetricTo(PointerType.CATEGORY));
        Assert.assertTrue(PointerType.HYPERNYM.symmetricTo(PointerType.HYPONYM));
    }

    @Test
    public void testGetPointerTypeForKey() {
        Assert.assertEquals(PointerType.ANTONYM, PointerType.getPointerTypeForKey("!"));
        Assert.assertEquals(PointerType.HYPERNYM, PointerType.getPointerTypeForKey("@"));
        Assert.assertEquals(PointerType.HYPONYM, PointerType.getPointerTypeForKey("~"));
        Assert.assertEquals(PointerType.ENTAILMENT, PointerType.getPointerTypeForKey("*"));
        Assert.assertEquals(PointerType.SIMILAR_TO, PointerType.getPointerTypeForKey("&"));
        Assert.assertEquals(PointerType.MEMBER_HOLONYM, PointerType.getPointerTypeForKey("#m"));
        Assert.assertEquals(PointerType.SUBSTANCE_HOLONYM, PointerType.getPointerTypeForKey("#s"));
        Assert.assertEquals(PointerType.PART_HOLONYM, PointerType.getPointerTypeForKey("#p"));
        Assert.assertEquals(PointerType.MEMBER_MERONYM, PointerType.getPointerTypeForKey("%m"));
        Assert.assertEquals(PointerType.SUBSTANCE_MERONYM, PointerType.getPointerTypeForKey("%s"));
        Assert.assertEquals(PointerType.PART_MERONYM, PointerType.getPointerTypeForKey("%p"));
        Assert.assertEquals(PointerType.CAUSE, PointerType.getPointerTypeForKey(">"));
        Assert.assertEquals(PointerType.PARTICIPLE_OF, PointerType.getPointerTypeForKey("<"));
        Assert.assertEquals(PointerType.SEE_ALSO, PointerType.getPointerTypeForKey("^"));
        Assert.assertEquals(PointerType.PERTAINYM, PointerType.getPointerTypeForKey("\\"));
        Assert.assertEquals(PointerType.ATTRIBUTE, PointerType.getPointerTypeForKey("="));
        Assert.assertEquals(PointerType.VERB_GROUP, PointerType.getPointerTypeForKey("$"));
        Assert.assertEquals(PointerType.DERIVATION, PointerType.getPointerTypeForKey("+"));
        Assert.assertEquals(PointerType.DOMAIN_ALL, PointerType.getPointerTypeForKey(";"));
        Assert.assertEquals(PointerType.MEMBER_ALL, PointerType.getPointerTypeForKey("-"));
        Assert.assertEquals(PointerType.CATEGORY, PointerType.getPointerTypeForKey(";c"));
        Assert.assertEquals(PointerType.USAGE, PointerType.getPointerTypeForKey(";u"));
        Assert.assertEquals(PointerType.REGION, PointerType.getPointerTypeForKey(";r"));
        Assert.assertEquals(PointerType.CATEGORY_MEMBER, PointerType.getPointerTypeForKey("-c"));
        Assert.assertEquals(PointerType.USAGE_MEMBER, PointerType.getPointerTypeForKey("-u"));
        Assert.assertEquals(PointerType.REGION_MEMBER, PointerType.getPointerTypeForKey("-r"));
        Assert.assertEquals(PointerType.INSTANCE_HYPERNYM, PointerType.getPointerTypeForKey("@i"));
        Assert.assertEquals(PointerType.INSTANCES_HYPONYM, PointerType.getPointerTypeForKey("~i"));
        Assert.assertNull(PointerType.getPointerTypeForKey("!!"));
    }

    @Test
    public void testGetAllPointerTypesForPOS() {
        List<PointerType> ptrs = PointerType.getAllPointerTypesForPOS(POS.NOUN);
        Assert.assertTrue(0 < ptrs.size());
        Assert.assertTrue(ptrs.contains(PointerType.ANTONYM));
        Assert.assertFalse(ptrs.contains(PointerType.SIMILAR_TO));
    }

    @Test
    public void testToString() {
        Assert.assertNotNull(PointerType.ANTONYM.toString());
        // on purpose to test cache
        Assert.assertNotNull(PointerType.ANTONYM.toString());

        Assert.assertNotNull(PointerType.PARTICIPLE_OF.toString());
        Assert.assertNotNull(PointerType.PART_MERONYM.toString());
    }

    @Test
    public void testAppliesTo() {
        Assert.assertTrue(PointerType.ANTONYM.appliesTo(POS.NOUN));
        Assert.assertTrue(PointerType.ANTONYM.appliesTo(POS.ADJECTIVE));
        Assert.assertTrue(PointerType.ANTONYM.appliesTo(POS.VERB));
        Assert.assertTrue(PointerType.ANTONYM.appliesTo(POS.ADVERB));
        Assert.assertFalse(PointerType.CAUSE.appliesTo(POS.NOUN));
    }

    @Test
    public void testGetFlags() {
        Assert.assertEquals(PointerTypeFlags.N | PointerTypeFlags.V, PointerType.HYPERNYM.getFlags());
    }

    @Test
    public void testGetAllPointerTypes() {
        Assert.assertNotNull(PointerType.getAllPointerTypes());
        Assert.assertEquals(26, PointerType.getAllPointerTypes().size());
    }
}
