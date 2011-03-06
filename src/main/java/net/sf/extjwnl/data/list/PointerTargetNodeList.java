package net.sf.extjwnl.data.list;

import net.sf.extjwnl.data.PointerTarget;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.util.TypeCheckingList;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A <code>PointerTargetNodeList</code> holds the results of a relationship method.
 * Each node contains a <code>PointerTarget</code> (a synset or word) and the type of
 * relationship that the node has to the other elements in the list and/or to
 * the source word.
 */
public class PointerTargetNodeList extends TypeCheckingList {

    private static final NodePrinter PRINTER =
            new NodePrinter(System.out, 2) {
                public void print(PrintStream stream, Node node, int indent, int indentIncrement) {
                    PointerTargetNode n = (PointerTargetNode) node;
                    char c[] = new char[indent >= 0 ? indent : 0];
                    Arrays.fill(c, ' ');
                    stream.println(new String(c) + n);
                }
            };

    public PointerTargetNodeList() {
        this(new LinkedList<PointerTarget>());
    }

    public PointerTargetNodeList(LinkedList<PointerTarget> list) {
        this(list, PointerTargetNode.class);
    }

    public PointerTargetNodeList(List<PointerTarget> targets) {
        this();
        for (PointerTarget target : targets) {
            add(target);
        }
    }

    protected PointerTargetNodeList(LinkedList<PointerTarget> list, Class type) {
        super(list, type, PointerTargetNode.class);
    }

    public void add(PointerTarget target) {
        add(new PointerTargetNode(target));
    }

    public void add(PointerTarget target, PointerType type) {
        add(new PointerTargetNode(target, type));
    }

    protected NodePrinter getNodePrinter() {
        return PRINTER;
    }

    public void print() {
        getNodePrinter().print(getTypeCheckingListIterator());
    }

    public void print(int indent) {
        getNodePrinter().print(getTypeCheckingListIterator(), indent);
    }

    public void print(PrintStream stream) {
        getNodePrinter().print(getTypeCheckingListIterator(), stream);
    }

    public void print(PrintStream stream, int indent) {
        getNodePrinter().print(getTypeCheckingListIterator(), stream, indent);
    }

    protected void print(PrintStream stream, int indent, int indentIncrement) {
        getNodePrinter().print(getTypeCheckingListIterator(), stream, indent, indentIncrement);
    }

    /**
     * Convert this list to a PointerTargetTreeNodeList.
     */
    public PointerTargetTreeNodeList toTreeList() {
        TypeCheckingListIterator itr = (TypeCheckingListIterator) listIterator();
        PointerTargetTreeNodeList list = new PointerTargetTreeNodeList();
        while (itr.hasNext()) {
            PointerTargetNode node = (PointerTargetNode) itr.next();
            list.add(new PointerTargetTreeNode(node.getPointerTarget(), node.getType()));
        }
        return list;
    }

    /**
     * Reverse the contents of this list. This function creates a copy of
     * this list and reverses it, so there are no changes made to this list
     * itself.
     */
    public PointerTargetNodeList reverse() {
        try {
            PointerTargetNodeList clone = (PointerTargetNodeList) this.clone();
            Collections.reverse(clone);
            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new UnsupportedOperationException();
        }
    }

    public Object clone() throws CloneNotSupportedException {
        super.clone();
        return new PointerTargetNodeList((LinkedList) copyBackingList());
    }

    public Object deepClone() throws CloneNotSupportedException {
        PointerTargetNodeList list = new PointerTargetNodeList();
        for (Object o : this) {
            list.add(((PointerTargetNode) o).clone());
        }
        return list;
    }
}