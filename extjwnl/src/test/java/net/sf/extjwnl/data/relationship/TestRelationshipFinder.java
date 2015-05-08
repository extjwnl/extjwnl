package net.sf.extjwnl.data.relationship;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestRelationshipFinder extends BaseData {

    @Test
    public void testImmediateRelationshipPositive() throws JWNLException {
        dictionary.edit();

        Synset s1 = dictionary.createSynset(POS.NOUN);
        Synset s2 = dictionary.createSynset(POS.NOUN);
        Synset s3 = dictionary.createSynset(POS.NOUN);

        IndexWord i1 = dictionary.createIndexWord(POS.NOUN, "test", s1);
        IndexWord i2 = dictionary.createIndexWord(POS.NOUN, "rest", s2);
        IndexWord i3 = dictionary.createIndexWord(POS.NOUN, "best", s3);

        i1.getSenses().add(s2);

        Assert.assertEquals(2, RelationshipFinder.getImmediateRelationship(i1, i2));
        Assert.assertEquals(-1, RelationshipFinder.getImmediateRelationship(i1, i3));
        Assert.assertEquals(-1, RelationshipFinder.getImmediateRelationship(i2, i3));
    }

    @Test
    public void testFindSymmetricRelationships() throws JWNLException, CloneNotSupportedException {
        dictionary.edit();

        Synset s1 = dictionary.createSynset(POS.NOUN);
        Synset s2 = dictionary.createSynset(POS.NOUN);

        RelationshipList l = RelationshipFinder.findRelationships(s1, s2, PointerType.ANTONYM);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        s1.getPointers().add(new Pointer(PointerType.ANTONYM, s1, s2));
        RelationshipList ll = RelationshipFinder.findRelationships(s1, s2, PointerType.ANTONYM);
        Assert.assertEquals(1, ll.size());
        Relationship r = ll.get(0);
        Assert.assertTrue(r instanceof SymmetricRelationship);
        Assert.assertEquals(1, r.getSize());
        Assert.assertEquals(PointerType.ANTONYM, r.getType());
        Assert.assertEquals(s1, r.getSourceSynset());
        Assert.assertEquals(s2, r.getTargetSynset());
        Assert.assertEquals(s2, r.getNodeList().get(0).getSynset());
    }

    @Test
    public void testFindAsymmetricRelationships() throws JWNLException, CloneNotSupportedException {
        dictionary.edit();

        Synset s1 = dictionary.createSynset(POS.VERB);
        Synset s2 = dictionary.createSynset(POS.VERB);

        RelationshipList l = RelationshipFinder.findRelationships(s1, s2, PointerType.CAUSE);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        s1.getPointers().add(new Pointer(PointerType.CAUSE, s1, s2));
        RelationshipList ll = RelationshipFinder.findRelationships(s1, s2, PointerType.CAUSE);
        Assert.assertEquals(1, ll.size());
        Relationship r = ll.get(0);
        Assert.assertTrue(r instanceof AsymmetricRelationship);
        Assert.assertEquals(3, r.getSize());
        Assert.assertEquals(PointerType.CAUSE, r.getType());
        Assert.assertEquals(s1, r.getSourceSynset());
        Assert.assertEquals(s2, r.getTargetSynset());
        Assert.assertEquals(s1, r.getNodeList().get(0).getSynset());
        Assert.assertEquals(s2, r.getNodeList().get(1).getSynset());
        Assert.assertEquals(s2, r.getNodeList().get(2).getSynset());
    }

    @Test
    public void testFindAsymmetricRelationshipsDepth() throws JWNLException, CloneNotSupportedException {
        dictionary.edit();

        Synset s1 = dictionary.createSynset(POS.VERB);
        Synset s2 = dictionary.createSynset(POS.VERB);
        Synset s3 = dictionary.createSynset(POS.VERB);

        s1.getPointers().add(new Pointer(PointerType.CAUSE, s1, s2));
        s2.getPointers().add(new Pointer(PointerType.CAUSE, s2, s3));

        RelationshipList ll = RelationshipFinder.findRelationships(s1, s3, PointerType.CAUSE, 2);
        Assert.assertEquals(1, ll.size());
        Relationship r = ll.get(0);
        Assert.assertTrue(r instanceof AsymmetricRelationship);
        Assert.assertEquals(4, r.getSize());

        RelationshipList lll = RelationshipFinder.findRelationships(s1, s3, PointerType.CAUSE, 1);
        Assert.assertEquals(0, lll.size());
    }
}
