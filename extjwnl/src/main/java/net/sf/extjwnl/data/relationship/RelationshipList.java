package net.sf.extjwnl.data.relationship;

import java.util.ArrayList;

/**
 * A list of <code>Relationship</code>s.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class RelationshipList extends ArrayList<Relationship> {

    /**
     * The index of the shallowest relationship.
     */
    private int shallowestIndex = -1;
    /**
     * The index of the deepest relationship.
     */
    private int deepestIndex = -1;

    public RelationshipList() {
        super();
    }

    @Override
    public boolean add(Relationship relationship) {
        int curSize = size();
        boolean success = super.add(relationship);
        if (1 == size()) {
            shallowestIndex = 0;
            deepestIndex = 0;
        } else {
            if (success) {
                Relationship shallowest = get(shallowestIndex);
                Relationship deepest = get(deepestIndex);
                if (relationship.getDepth() < shallowest.getDepth()) {
                    shallowestIndex = curSize;
                }
                if (relationship.getDepth() > deepest.getDepth()) {
                    deepestIndex = curSize;
                }
            }
        }
        return success;
    }

    /**
     * Returns the shallowest relationship in the list. N.B. There can be more than one shallowest.
     * The shallowest index is not updated on element removal.
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
     * Returns the deepest Relationship in the list. N.B. There can be more than one deepest.
     * The shallowest index is not updated on element removal.
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