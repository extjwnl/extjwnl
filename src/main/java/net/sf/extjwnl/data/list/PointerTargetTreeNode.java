package net.sf.extjwnl.data.list;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.PointerTarget;
import net.sf.extjwnl.data.PointerType;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * A node in a <code>PointerTargetTreeNodeList</code>. Each node can have a childTreeList, which is a list of nodes that
 * are children of this node, and a pointerTreeList, which is a tree of pointers related to this node's
 * target. Basically, this allows for a single tree supporting multiple relationships. For example, you
 * may have a ancestry tree, and each node, besides having links to its children, has links to the
 * synonyms of its target.
 *
 * @author John Didion <jdidion@users.sourceforge.net>
 */
public class PointerTargetTreeNode extends PointerTargetNode {
    /**
     * The list of all this node's children
     */
    private PointerTargetTreeNodeList childTreeList;
    /**
     * This list of pointers associated with this node
     */
    private PointerTargetTreeNodeList pointerTreeList;
    /**
     * This node's parent
     */
    private PointerTargetTreeNode parent;

    public PointerTargetTreeNode(PointerTarget target, PointerTargetTreeNodeList childTreeList,
                                 PointerTargetTreeNodeList pointerTreeList, PointerType type,
                                 PointerTargetTreeNode parent) {
        super(target, type);
        this.parent = parent;
        this.childTreeList = childTreeList;
        this.pointerTreeList = pointerTreeList;
    }

    public PointerTargetTreeNode(PointerTarget target) {
        this(target, null, null, null, null);
    }

    public PointerTargetTreeNode(PointerTarget target, PointerType type) {
        this(target, null, null, type, null);
    }

    public PointerTargetTreeNode(PointerTarget target, PointerTargetTreeNodeList childTreeList, PointerType type) {
        this(target, childTreeList, null, type, null);
    }

    public PointerTargetTreeNode(PointerTarget target, PointerType type, PointerTargetTreeNode parent) {
        this(target, null, null, type, parent);
    }

    public PointerTargetTreeNode(PointerTarget target, PointerTargetTreeNodeList childTreeList,
                                 PointerType type, PointerTargetTreeNode parent) {
        this(target, childTreeList, null, type, parent);
    }

    public PointerTargetTreeNode(PointerTarget target, PointerTargetTreeNodeList childTreeList,
                                 PointerTargetTreeNodeList pointerTreeList, PointerType type) {
        this(target, childTreeList, pointerTreeList, type, null);
    }

    public boolean equals(Object obj) {
        return (obj instanceof PointerTargetTreeNode) && super.equals(obj);
    }

    public String toString() {
        return JWNL.resolveMessage("DATA_TOSTRING_015",
                new Object[]{
                        getPointerTarget(),
                        getType(),
                        !hasParent(),
                        hasValidChildTreeList(),
                        hasValidPointerTreeList()
                });
    }


    public void setChildTreeList(PointerTargetTreeNodeList list) {
        childTreeList = list;
    }

    public void setPointerTreeList(PointerTargetTreeNodeList list) {
        pointerTreeList = list;
    }

    public void setParent(PointerTargetTreeNode parent) {
        this.parent = parent;
    }

    public PointerTargetTreeNode getParent() {
        return parent;
    }

    public PointerTargetTreeNodeList getChildTreeList() {
        return childTreeList;
    }

    public PointerTargetTreeNodeList getPointerTreeList() {
        return pointerTreeList;
    }

    public boolean hasChildTreeList() {
        return (getChildTreeList() != null);
    }

    /**
     * A valid childTreeList is one that is not null and not empty.
     */
    public boolean hasValidChildTreeList() {
        return hasChildTreeList() && !getChildTreeList().isEmpty();
    }

    public boolean hasPointerTreeList() {
        return (getPointerTreeList() != null);
    }

    /**
     * A valid pointerTreeList is one that is not null and not empty.
     */
    public boolean hasValidPointerTreeList() {
        return hasPointerTreeList() && !getPointerTreeList().isEmpty();
    }

    public boolean hasParent() {
        return (getParent() != null);
    }

    /**
     * Convert this node into a list of PointerTargetList's, each representing a unique brance through the tree
     */
    public List toList(PointerTargetNodeList list) {
        try {
            list.add(getPointerTarget(), getType());
            List l = new ArrayList();
            if (hasValidChildTreeList()) {
                PointerTargetTreeNodeList childTreeList = getChildTreeList();
                ListIterator itr = childTreeList.listIterator();
                while (itr.hasNext()) {
                    l.addAll(((PointerTargetTreeNode) itr.next()).toList((PointerTargetNodeList) list.clone()));
                }
            } else {
                l.add(list);
            }
            return l;
        } catch (CloneNotSupportedException ex) {
            throw new UnsupportedOperationException();
        }
    }

    public Object clone() throws CloneNotSupportedException {
        super.clone();
        return new PointerTargetTreeNode(getPointerTarget(),
                getChildTreeList(),
                getPointerTreeList(),
                getType(),
                getParent());
    }

    public Object deepClone() throws UnsupportedOperationException {
        return new PointerTargetTreeNode(getPointerTarget(),
                (PointerTargetTreeNodeList) getChildTreeList().deepClone(),
                (PointerTargetTreeNodeList) getPointerTreeList().deepClone(),
                getType(), (PointerTargetTreeNode) getParent().deepClone());

    }
}