/**
 * Java WordNet Library (JWNL)
 * See the documentation for copyright information.
 */
package net.didion.jwnl.data.relationship;

import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;

/**
 * A symmetric relationship is one whose type is symmetric (its own inverse). An example of a symmetric
 * relationship is synonomy (since, if a is a synonym of b, then be is a synonym of a). Symmetric relationsips
 * differ from asymmetric relationships in that there is no definite divergence point between the ancestry of
 * the source and target synsets. Another way of saying this is that the target synset will always been in
 * the source's ancestry, and vice versa. For this reason, symmetric relationships have no concept of a
 * common parent index.
 */
public class SymmetricRelationship extends Relationship {
	public SymmetricRelationship(
	    PointerType type, PointerTargetNodeList nodes, Synset sourceSynset, Synset targetSynset) {

		super(type, nodes, sourceSynset, targetSynset);
	}

	public Relationship reverse() {
		PointerTargetNodeList list = ((PointerTargetNodeList)getNodeList().deepClone()).reverse();
		for (int i = 0; i < list.size(); i++) {
			((PointerTargetNode)list.get(i)).setType(getType().getSymmetricType());
		}
		return new SymmetricRelationship(getType(), list, getSourceSynset(), getTargetSynset());
	}
}