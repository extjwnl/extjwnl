package net.sf.extjwnl.data.list;

import net.sf.extjwnl.data.PointerTarget;
import net.sf.extjwnl.data.Synset;

import java.util.ArrayList;
import java.util.List;

/**
 * A container for the root node of a pointer target tree.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class PointerTargetTree {

    private final PointerTargetTreeNode rootNode;

    public PointerTargetTree(PointerTargetTreeNode rootNode) {
        this.rootNode = rootNode;
    }

    public PointerTargetTree(Synset synset, PointerTargetTreeNodeList list) {
        rootNode = new PointerTargetTreeNode(synset);
        rootNode.setChildTreeList(list);
    }

    public PointerTargetTreeNode getRootNode() {
        return rootNode;
    }

    /**
     * Two PointerTargetTree's are equal if their root nodes are equal.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof PointerTargetTree) && rootNode.equals(((PointerTargetTree) obj).getRootNode());
    }

    @Override
    public int hashCode() {
        return rootNode.hashCode();
    }

    /**
     * Walks the tree and performs the operation <var>opr</var> on
     * each node. Continues until either opr returns a non-null
     * value, or it reaches the last node in the tree.
     *
     * @param opr operation to execute
     * @return operation result
     */
    public PointerTargetTreeNode getFirstMatch(PointerTargetTreeNodeList.Operation opr) {
        PointerTargetTreeNode obj = opr.execute(getRootNode());
        if (obj == null && getRootNode().hasValidChildTreeList()) {
            obj = getRootNode().getChildTreeList().getFirstMatch(opr);
        }
        return obj;
    }

    /**
     * Walks the tree and performs the operation <var>opr</var> on each node.
     * Searches the tree exhaustively and returns a List containing all nodes
     * that are returned by <var>opr</var>.
     *
     * @param opr operation to execute
     * @return list of operation results
     */
    public List<PointerTargetTreeNode> getAllMatches(PointerTargetTreeNodeList.Operation opr) {
        List<PointerTargetTreeNode> list = new ArrayList<>();
        if (opr.execute(getRootNode()) != null) {
            list.add(getRootNode());
        }
        if (getRootNode().hasValidChildTreeList()) {
            getRootNode().getChildTreeList().getAllMatches(opr, list);
        }
        return list;
    }

    /**
     * Finds the first occurrence of <var>node</var> in the tree.
     *
     * @param node node to search for
     * @return the first occurrence of <var>node</var> in the tree
     */
    public PointerTargetTreeNode findFirst(PointerTargetTreeNode node) {
        return getFirstMatch(new PointerTargetTreeNodeList.FindNodeOperation(node));
    }

    /**
     * Finds the first node in the tree whose target is <var>target</var>.
     *
     * @param target target to search for
     * @return the first node in the tree whose target is <var>target</var>
     */
    public PointerTargetTreeNode findFirst(PointerTarget target) {
        return getFirstMatch(new PointerTargetTreeNodeList.FindTargetOperation(target));
    }

    /**
     * Finds all occurrences of <var>node</var> in the tree.
     *
     * @param node node to search for
     * @return all occurrences of <var>node</var> in the tree
     */
    public List<PointerTargetTreeNode> findAll(PointerTargetTreeNode node) {
        return getAllMatches(new PointerTargetTreeNodeList.FindNodeOperation(node));
    }

    /**
     * Finds all nodes in the tree whose target is <var>target</var>.
     *
     * @param target target to search for
     * @return all nodes in the tree whose target is <var>target</var>
     */
    public List<PointerTargetTreeNode> findAll(PointerTarget target) {
        return getAllMatches(new PointerTargetTreeNodeList.FindTargetOperation(target));
    }

    public void print() {
        if (getRootNode() != null) {
            System.out.println(getRootNode());
            getRootNode().getChildTreeList().print();
        }
    }

    //
    // Conversion functions
    //

    /**
     * Reverse this tree. A reversal is done by converting this tree to lists
     * and then reversing each of the lists. The structure of the tree is
     * unaffected by this operation.
     *
     * @return reversed lists
     */
    public List<PointerTargetNodeList> reverse() {
        List<PointerTargetNodeList> list = toList();
        if (list != null) {
            List<PointerTargetNodeList> reversedLists = new ArrayList<>(list.size());
            for (PointerTargetNodeList l : list) {
                reversedLists.add(l.reverse());
            }
            return reversedLists;
        }
        return null;
    }

    /**
     * Convert this tree to a list of PointerTargetNodeLists. This creates one list for each
     * unique path through the tree.
     *
     * @return list of PointerTargetNodeLists
     */
    public List<PointerTargetNodeList> toList() {
        List<PointerTargetNodeList> list = getRootNode().toList(new PointerTargetNodeList());
        // since the tree could have been made up of multiple types, we need to set the type of
        // the root node now that we're breaking the tree down into lists that can only be of
        // one type
        for (PointerTargetNodeList aList : list) {
            if (aList.size() >= 2) {
                PointerTargetNode root = aList.get(0);
                PointerTargetNode node = aList.get(1);
                root.setType(node.getType());
            }
        }
        return list;
    }
}