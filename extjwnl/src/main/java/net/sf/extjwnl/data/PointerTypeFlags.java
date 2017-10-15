package net.sf.extjwnl.data;

/**
 * Flags for tagging a pointer type with the POS types it apples to.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface PointerTypeFlags {

    int N = 1;
    int V = 2;
    int ADJ = 4;
    int ADV = 8;
}