package net.sf.extjwnl.data.list;

import java.io.PrintStream;
import java.util.ListIterator;

/**
 * A printer for displaying the contents of a node list.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class NodePrinter<E extends Node> {

    private PrintStream defaultStream = System.out;
    private int defaultIndent = 0;

    public NodePrinter() {
    }

    public NodePrinter(int defaultIndent) {
        this.defaultIndent = defaultIndent;
    }

    public NodePrinter(PrintStream defaultStream) {
        this.defaultStream = defaultStream;
    }

    public NodePrinter(PrintStream defaultStream, int defaultIndent) {
        this.defaultStream = defaultStream;
        this.defaultIndent = defaultIndent;
    }

    /**
     * Print the contents of the given node, indenting it <var>indent</var> spaces.
     * In each recursive call to print, <var>indent</var> should be incremented by
     * <var>indentIncrement</var>.
     *
     * @param stream          stream to print to
     * @param node            node to print
     * @param indent          indent
     * @param indentIncrement indent increment
     */
    protected abstract void print(PrintStream stream, E node, int indent, int indentIncrement);

    /**
     * Print the contents of <var>itr</var> using the default indent
     *
     * @param itr iterator with nodes to print
     */
    public void print(ListIterator<E> itr) {
        print(itr, defaultStream);
    }

    /**
     * Print the contents of <var>itr</var> to the given stream
     *
     * @param stream stream to print to
     * @param itr    iterator with nodes to print
     */
    public void print(ListIterator<E> itr, PrintStream stream) {
        print(itr, stream, defaultIndent);
    }

    /**
     * Print the contents of <var>itr</var> to the given stream indenting each line <var>indent</var> spaces.
     *
     * @param stream stream to print to
     * @param indent indent
     * @param itr    iterator with nodes to print
     */
    public void print(ListIterator<E> itr, PrintStream stream, int indent) {
        print(itr, stream, indent, indent);
    }

    /**
     * Print the contents of <var>itr</var> indenting each line <var>indent</var> spaces.
     *
     * @param indent indent
     * @param itr    iterator with nodes to print
     */
    public void print(ListIterator<E> itr, int indent) {
        print(itr, indent, indent);
    }

    /**
     * Print the contents of <var>itr</var> to the default stream. Indent the first line <var>indent</var>
     * spaces. Each level of nesting will be printed intended <var>indentIncrement</var> spaces more than
     * the previous level of nesting.
     *
     * @param indent          indent
     * @param indentIncrement indent increment
     * @param itr             iterator with nodes to print
     */
    public void print(ListIterator<E> itr, int indent, int indentIncrement) {
        print(itr, defaultStream, indent, indentIncrement);
    }

    /**
     * Print the contents of <var>itr</var> to the given stream. Indent the first line <var>indent</var>
     * spaces. Each level of nesting will be printed intended <var>indentIncrement</var> spaces more than
     * the previous level of nesting.
     *
     * @param stream          stream to print to
     * @param indent          indent
     * @param indentIncrement indent increment
     * @param itr             iterator with nodes to print
     */
    public void print(ListIterator<E> itr, PrintStream stream, int indent, int indentIncrement) {
        NodeListIteratorWrapper<E> pItr = new NodeListIteratorWrapper<>(itr);
        // Find out where we currently are in the iterator
        int curNode = pItr.currentIndex();
        // Move to the first node in the iterator
        pItr.moveToBeginning();
        // print all the nodes
        while (itr.hasNext()) {
            print(stream, pItr.nextNode(), indent, indentIncrement);
        }
        // go back to our original position
        pItr.moveTo(curNode);
    }

    /**
     * Wrapper for a NodeListIterator that allows the next pointer to be moved to any index.
     */
    private static final class NodeListIteratorWrapper<E extends Node> {
        private final ListIterator<E> itr;

        public NodeListIteratorWrapper(ListIterator<E> itr) {
            this.itr = itr;
        }

        public E nextNode() {
            return itr.next();
        }

        public E previousNode() {
            return itr.previous();
        }

        public int currentIndex() {
            return itr.nextIndex() - 1;
        }

        /**
         * Moves the iterator to a point in the iterator where the next index is <var>index</var>.
         *
         * @param index the next index
         * @return current index
         */
        public int moveTo(int index) {
            if (currentIndex() < index) {
                while (currentIndex() < index && itr.hasNext()) {
                    itr.next();
                }
            } else if (currentIndex() > index) {
                while (currentIndex() > index && itr.hasPrevious()) {
                    itr.previous();
                }
            }
            return currentIndex();
        }

        /**
         * Move to the initial position in the list (where nextNode() returns the first node in the list
         */
        public void moveToBeginning() {
            moveTo(-1);
        }

        /**
         * Move to the first position in the iterator
         */
        public void moveToFirst() {
            moveTo(0);
        }

        /**
         * Move to the last position in the iterator
         */
        public void moveToLast() {
            // just give it a really big number since it will stop
            // when it gets to the end of the list
            moveTo(Integer.MAX_VALUE);
        }
    }
}

