package net.sf.extjwnl.data.relationship;

import java.util.ArrayList;

/**
 * A list of <code>Relationship</code>s.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class RelationshipList extends ArrayList<Relationship> {

    /**
     * The index of the shallowest relationship.
     */
    private int shallowestIndex = Integer.MAX_VALUE;
    /**
     * The index of the deepest relationship.
     */
    private int deepestIndex = -1;

    public RelationshipList() {
        super();
    }

    public boolean add(Relationship relationship) {
        int curSize = size();
        boolean success = super.add(relationship);
        if (success) {
            if (relationship.getDepth() < shallowestIndex) {
                shallowestIndex = curSize;
            }
            if (relationship.getDepth() > deepestIndex) {
                deepestIndex = curSize;
            }
        }
        return success;
    }

    /**
     * Returns the shallowest relationship in the list.
     *
     * @return the shallowest Relationship in the list
     */
    public Relationship getShallowest() {
        if (shallowestIndex >= 0) {
            return get(shallowestIndex);
        }
        return null;
    }

    /**
     * Returns the deepest Relationship in the list.
     *
     * @return the deepest Relationship in the list
     */
    public Relationship getDeepest() {
        if (deepestIndex >= 0) {
            return get(deepestIndex);
        }
        return null;
    }
}