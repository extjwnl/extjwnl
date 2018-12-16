package net.sf.extjwnl.data.list;

import net.sf.extjwnl.data.PointerTarget;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.util.DeepCloneable;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A list of <code>PointerTargetTreeNode</code>s.
 *
 * @author John Didion (jdidion@didion.net)
 */
public class PointerTargetTreeNodeList extends LinkedList<PointerTargetTreeNode> implements DeepCloneable {

    private static final NodePrinter<PointerTargetTreeNode> PRINTER =
            new NodePrinter<PointerTargetTreeNode>(2) {
                public void print(PrintStream stream, PointerTargetTreeNode node, int indent, int indentIncrement) {
                    PointerTargetNodeList.printIndented(stream, node, indent);
                    if (node.hasValidChildTreeList()) {
                        node.getChildTreeList().print(stream, indent + indentIncrement, indentIncrement);
                    }
                }
            };

    public PointerTargetTreeNodeList() {
        super();
    }

    public PointerTargetTreeNodeList(LinkedList<PointerTargetTreeNode> list) {
        super(list);
    }

    public void add(PointerTarget target) {
        add(new PointerTargetTreeNode(target));
    }

    public void add(PointerTarget target, PointerType type) {
        add(new PointerTargetTreeNode(target, type));
    }

    public void add(PointerTarget target, PointerType type, PointerTargetTreeNode parent) {
        add(new PointerTargetTreeNode(target, type, parent));
    }

    public void add(PointerTarget target, PointerTargetTreeNodeList childTreeList, PointerType type) {
        add(new PointerTargetTreeNode(target, childTreeList, type));
    }

    public void add(PointerTarget target, PointerTargetTreeNodeList childTreeList,
                    PointerType type, PointerTargetTreeNode parent) {
        add(new PointerTargetTreeNode(target, childTreeList, type, parent));
    }

    public void add(PointerTarget target, PointerTargetTreeNodeList childTreeList,
                    PointerTargetTreeNodeList pointerTreeList, PointerType type) {
        add(new PointerTargetTreeNode(target, childTreeList, pointerTreeList, type));
    }

    public void add(PointerTarget target, PointerTargetTreeNodeList childTreeList,
                    PointerTargetTreeNodeList pointerTreeList, PointerType type, PointerTargetTreeNode parent) {
        add(new PointerTargetTreeNode(target, childTreeList, pointerTreeList, type, parent));
    }

    protected NodePrinter<PointerTargetTreeNode> getNodePrinter() {
        return PRINTER;
    }

    /**
     * Walks the list and all the children of each node in the list and
     * performs the operation <code>opr</code> on each node. Continues until
     * either opr returns a non-null value, or it reaches the last node in the list.
     *
     * @param opr operation to execute
     * @return operation result
     */
    public PointerTargetTreeNode getFirstMatch(Operation opr) {
        for (final PointerTargetTreeNode node : this) {
            final PointerTargetTreeNode obj = opr.execute(node);
            if (obj != null) {
                return obj;
            } else if (node.hasValidChildTreeList()) {
                return node.getChildTreeList().getFirstMatch(opr);
            }
        }
        return null;
    }

    /**
     * Walks the list and performs the operation <code>opr</code> on each node.
     * Searches the list exhaustively and return a List containing all nodes
     * that are returned by <code>opr</code>.
     *
     * @param opr operation
     * @return list of operation results
     */
    public List<PointerTargetTreeNode> getAllMatches(Operation opr) {
        List<PointerTargetTreeNode> list = new ArrayList<>();
        getAllMatches(opr, list);
        return list;
    }

    /**
     * Returns all matches and adds them to <var>matches</var>
     *
     * @param opr     operation
     * @param matches list of matches
     */
    public void getAllMatches(Operation opr, List<PointerTargetTreeNode> matches) {
        for (final PointerTargetTreeNode node : this) {
            final PointerTargetTreeNode obj = opr.execute(node);
            if (obj != null) {
                matches.add(obj);
            }
            if (node.hasValidChildTreeList()) {
                node.getChildTreeList().getAllMatches(opr, matches);
            }
        }
    }

    /**
     * Finds the first node in the list that is equal to <code>node</code>.
     * <code>node</code> is considered to match a node in the list
     * if they contain equal pointer targets and are of the same type.
     *
     * @param node node to search for
     * @return the first node in the list that is equal to <code>node</code>
     */
    public PointerTargetTreeNode findFirst(PointerTargetTreeNode node) {
        PointerTargetTreeNode obj = getFirstMatch(new FindNodeOperation(node));
        return obj;
    }

    /**
     * Finds all occurrences of <code>node</code> within the list.
     *
     * @param node node to search for
     * @return all occurrences of <code>node</code> within the list
     */
    public List<PointerTargetTreeNode> findAll(PointerTargetTreeNode node) {
        List<PointerTargetTreeNode> v = getAllMatches(new FindNodeOperation(node));
        if (v == null) {
            return null;
        } else {
            return v;
        }
    }

    public PointerTargetTreeNodeList clone() {
        return (PointerTargetTreeNodeList) super.clone();
    }

    public PointerTargetTreeNodeList deepClone() {
        PointerTargetTreeNodeList list = new PointerTargetTreeNodeList();
        for (PointerTargetTreeNode o : this) {
            list.add(o.deepClone());
        }
        return list;
    }

    /**
     * Operation that is performed on the nodes of a tree or list.
     */
    public interface Operation {

        /**
         * Executes the operation on the given node.
         *
         * @param node operation target
         * @return operation result
         */
        PointerTargetTreeNode execute(PointerTargetTreeNode node);
    }

    /**
     * Operation that is used for finding the specified node in a tree.
     */
    public static class FindNodeOperation implements Operation {
        private final PointerTargetTreeNode node;

        public FindNodeOperation(PointerTargetTreeNode node) {
            this.node = node;
        }

        public PointerTargetTreeNode execute(PointerTargetTreeNode testNode) {
            if (node.equals(testNode)) {
                return testNode;
            }
            return null;
        }
    }

    /**
     * Operation that is used for finding the node(s) in a tree that have the specified <code>PointerTarget</code>.
     */
    public static class FindTargetOperation implements Operation {
        private final PointerTarget target;

        public FindTargetOperation(PointerTarget target) {
            this.target = target;
        }

        public PointerTargetTreeNode execute(PointerTargetTreeNode node) {
            if (node.getPointerTarget().equals(target)) {
                return node;
            }
            return null;
        }
    }

    protected void print() {
        getNodePrinter().print(listIterator());
    }

    protected void print(PrintStream stream, int indent, int indentIncrement) {
        getNodePrinter().print(listIterator(), stream, indent, indentIncrement);
    }
}