package net.sf.extjwnl.data.list;

import net.sf.extjwnl.data.PointerTarget;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.util.DeepCloneable;

import java.io.PrintStream;
import java.util.*;

/**
 * A <code>PointerTargetNodeList</code> holds the results of a relationship method.
 * Each node contains a <code>PointerTarget</code> (a synset or word) and the type of
 * relationship that the node has to the other elements in the list and/or to
 * the source word.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class PointerTargetNodeList extends LinkedList<PointerTargetNode> implements DeepCloneable {

    private static final NodePrinter<PointerTargetNode> PRINTER =
            new NodePrinter<PointerTargetNode>(System.out, 2) {
                public void print(PrintStream stream, PointerTargetNode node, int indent, int indentIncrement) {
                    printIndented(stream, node, indent);
                }
            };

    public PointerTargetNodeList() {
        super();
    }

    public PointerTargetNodeList(Collection<? extends PointerTargetNode> c) {
        super(c);
    }

    public PointerTargetNodeList(List<PointerTarget> targets, PointerType pointerType) {
        this();
        for (PointerTarget target : targets) {
            add(target, pointerType);
        }
    }

    public void add(PointerTarget target, PointerType pointerType) {
        add(new PointerTargetNode(target, pointerType));
    }

    protected static void printIndented(PrintStream stream, PointerTargetNode node, int indent) {
        char[] c = new char[indent >= 0 ? indent : 0];
        Arrays.fill(c, ' ');
        stream.println(new String(c) + node);
    }

    protected NodePrinter<PointerTargetNode> getNodePrinter() {
        return PRINTER;
    }

    public void print() {
        getNodePrinter().print(listIterator());
    }

    public void print(int indent) {
        getNodePrinter().print(listIterator(), indent);
    }

    public void print(PrintStream stream) {
        getNodePrinter().print(listIterator(), stream);
    }

    public void print(PrintStream stream, int indent) {
        getNodePrinter().print(listIterator(), stream, indent);
    }

    protected void print(PrintStream stream, int indent, int indentIncrement) {
        getNodePrinter().print(listIterator(), stream, indent, indentIncrement);
    }

    /**
     * Reverses the contents of this list. This function creates a copy of
     * this list and reverses it, so there are no changes made to this list
     * itself.
     *
     * @return reversed list
     */
    public PointerTargetNodeList reverse() {
        PointerTargetNodeList clone = this.clone();
        Collections.reverse(clone);
        return clone;
    }

    @Override
    public PointerTargetNodeList clone() {
        return (PointerTargetNodeList) super.clone();
    }

    public PointerTargetNodeList deepClone() throws CloneNotSupportedException {
        PointerTargetNodeList list = new PointerTargetNodeList();
        for (PointerTargetNode o : this) {
            list.add(o.clone());
        }
        return list;
    }
}