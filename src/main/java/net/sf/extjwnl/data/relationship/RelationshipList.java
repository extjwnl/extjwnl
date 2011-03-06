package net.sf.extjwnl.data.relationship;

import net.sf.extjwnl.util.TypeCheckingList;

import java.util.ArrayList;

/**
 * A list of <code>Relationship</code>s.
 */
public class RelationshipList extends TypeCheckingList {
    /**
     * The index of the shallowest relationship.
     */
    private int shallowestIndex = Integer.MAX_VALUE;
    /**
     * The index of the deepest relationship.
     */
    private int deepestIndex = -1;

    public RelationshipList() {
        super(new ArrayList(), Relationship.class);
    }

    public synchronized boolean add(Object o) {
        int curSize = size();
        boolean success = super.add(o);
        if (success) {
            Relationship r = (Relationship) o;
            if (r.getDepth() < shallowestIndex) {
                shallowestIndex = curSize;
            }
            if (r.getDepth() > deepestIndex) {
                deepestIndex = curSize;
            }
        }
        return success;
    }

    /**
     * Return the shallowest Relationship in the list.
     */
    public synchronized Relationship getShallowest() {
        if (shallowestIndex >= 0) {
            return (Relationship) get(shallowestIndex);
        }
        return null;
    }

    /**
     * Return the deepest Relationship in the list.
     */
    public synchronized Relationship getDeepest() {
        if (deepestIndex >= 0) {
            return (Relationship) get(deepestIndex);
        }
        return null;
    }
}