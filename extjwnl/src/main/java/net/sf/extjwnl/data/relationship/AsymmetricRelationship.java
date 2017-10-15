package net.sf.extjwnl.data.relationship;

import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.list.PointerTargetNodeList;

/**
 * An asymmetric relationship is one whose source and target synsets have lineages with a definite divergence point.
 * The commonParentIndex is the index of the node in the relationship that represents this divergence point.
 * <p>
 * For example, in finding a hypernym  relationship between dog and cat, the relationship is dog -&gt; canine -&gt;
 * carnivore -&gt; feline -&gt; cat. The ancestry of "dog" and the ancestry of "cat" diverge at "carnivore," so
 * the common parent index is thus 2.
 * </p>
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class AsymmetricRelationship extends Relationship {
    /**
     * The index of the node in the relationship that represents the point
     * at which the source and target nodes' ancestries diverge.
     */
    private final int commonParentIndex;
    private transient int cachedRelativeTargetDepth = -1;

    public AsymmetricRelationship(
            PointerType type, PointerTargetNodeList nodes, int commonParentIndex, Synset sourceSynset, Synset targetSynset) {

        super(type, nodes, sourceSynset, targetSynset);
        // fail fast
        nodes.get(commonParentIndex);
        this.commonParentIndex = commonParentIndex;
    }

    public int getCommonParentIndex() {
        return commonParentIndex;
    }

    /**
     * Returns the depth of the target, from the commonParentIndex, relative to the depth of the source.
     * If both target and source are equidistant from the commonParentIndex, this method returns 0;
     *
     * @return the depth of the target, from the commonParentIndex, relative to the depth of the source
     */
    public int getRelativeTargetDepth() {
        if (cachedRelativeTargetDepth == -1) {
            int distSourceToParent = commonParentIndex;
            int distParentToTarget = (getNodeList().size() - 1) - commonParentIndex;
            cachedRelativeTargetDepth = distParentToTarget - distSourceToParent;
        }
        return cachedRelativeTargetDepth;
    }

    public Relationship reverse() throws CloneNotSupportedException {
        PointerTargetNodeList list = getNodeList().deepClone().reverse();
        int commonParentIndex = (list.size() - 1) - getCommonParentIndex();
        for (int i = 0; i < list.size(); i++) {
            if (i != commonParentIndex) {
                list.get(i).setType(getType().getSymmetricType());
            }
        }
        return new AsymmetricRelationship(getType(), list, commonParentIndex, getSourceSynset(), getTargetSynset());
    }
}