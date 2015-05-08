package net.sf.extjwnl.data.relationship;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.BaseData;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.data.list.PointerTargetNodeList;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestAsymmetricRelationship extends BaseData {

    @Test
    public void testConstructor() throws JWNLException {
        dictionary.edit();
        Synset s1 = dictionary.createSynset(POS.NOUN);
        Synset s2 = dictionary.createSynset(POS.NOUN);
        AsymmetricRelationship r =
                new AsymmetricRelationship(PointerType.HYPERNYM, new PointerTargetNodeList(
                        Arrays.asList(new PointerTargetNode(s2))
                ), 0, s1, s2);

        Assert.assertEquals(s1, r.getSourceSynset());
        Assert.assertEquals(s2, r.getTargetSynset());
        Assert.assertNotNull(r.toString());
    }

    @Test
    public void testGetRelativeTargetDepth() throws JWNLException {
        dictionary.edit();
        Synset s1 = dictionary.createSynset(POS.NOUN);
        Synset s2 = dictionary.createSynset(POS.NOUN);
        Synset s3 = dictionary.createSynset(POS.NOUN);
        AsymmetricRelationship r =
                new AsymmetricRelationship(PointerType.HYPERNYM, new PointerTargetNodeList(
                        Arrays.asList(
                                new PointerTargetNode(s1),
                                new PointerTargetNode(s2),
                                new PointerTargetNode(s3)
                        )
                ), 1, s1, s3);

        Assert.assertEquals(0, r.getRelativeTargetDepth());
    }

    @Test
    public void testReverse() throws JWNLException, CloneNotSupportedException {
        dictionary.edit();
        Synset s1 = dictionary.createSynset(POS.NOUN);
        Synset s2 = dictionary.createSynset(POS.NOUN);
        Synset s3 = dictionary.createSynset(POS.NOUN);
        AsymmetricRelationship r =
                new AsymmetricRelationship(PointerType.HYPERNYM,
                        new PointerTargetNodeList(Arrays.asList(
                                new PointerTargetNode(s1, PointerType.HYPERNYM),
                                new PointerTargetNode(s2, PointerType.HYPERNYM),
                                new PointerTargetNode(s3, PointerType.HYPONYM)
                        )),
                        1,
                        s1, s2);
        Relationship rev = r.reverse();

        Assert.assertEquals(3, rev.getSize());
        Assert.assertEquals(3, rev.getNodeList().size());
        Assert.assertEquals(PointerType.HYPONYM, rev.getNodeList().get(0).getType());
        Assert.assertEquals(PointerType.HYPERNYM, rev.getNodeList().get(1).getType());
        Assert.assertEquals(PointerType.HYPONYM, rev.getNodeList().get(2).getType());
    }
}
