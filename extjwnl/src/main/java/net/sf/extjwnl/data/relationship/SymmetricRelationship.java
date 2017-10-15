package net.sf.extjwnl.data.relationship;

import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.data.list.PointerTargetNodeList;

/**
 * A symmetric relationship is one whose type is symmetric (its own inverse). An example of a symmetric
 * relationship is synonymy (since, if a is a synonym of b, then be is a synonym of a). Symmetric relationships
 * differ from asymmetric relationships in that there is no definite divergence point between the ancestry of
 * the source and target synsets. Another way of saying this is that the target synset will always be in
 * the source's ancestry, and vice versa. For this reason, symmetric relationships have no concept of a
 * common parent index.
 *
 * @author John Didion (jdidion@didion.net)
 */
public class SymmetricRelationship extends Relationship {

    public SymmetricRelationship(
            PointerType type, PointerTargetNodeList nodes, Synset sourceSynset, Synset targetSynset) {

        super(type, nodes, sourceSynset, targetSynset);
    }

    public Relationship reverse() throws CloneNotSupportedException {
        PointerTargetNodeList list = getNodeList().deepClone().reverse();
        for (Object aList : list) {
            ((PointerTargetNode) aList).setType(getType().getSymmetricType());
        }
        return new SymmetricRelationship(getType(), list, getSourceSynset(), getTargetSynset());
    }
}