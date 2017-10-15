package net.sf.extjwnl.data.list;

import net.sf.extjwnl.data.PointerTarget;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.util.ResourceBundleSet;

/**
 * A node in a <code>PointerTargetNodeList</code>.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class PointerTargetNode implements Node {
    /**
     * The PointerTarget
     */
    private final PointerTarget target;
    /**
     * The relation type that produced this node. e.g. if you make a call to
     * getDirectHypernyms(), each node in the resultant list will have a
     * type of PointerType.HYPERNYM.
     */
    private PointerType type;

    public PointerTargetNode(PointerTarget target) {
        this(target, null);
    }

    public PointerTargetNode(PointerTarget target, PointerType type) {
        this.target = target;
        this.type = type;
    }

    public void setType(PointerType type) {
        this.type = type;
    }

    public PointerType getType() {
        return type;
    }

    public PointerTarget getPointerTarget() {
        return target;
    }

    /**
     * Returns true if the target is a Word, else false.
     *
     * @return true if the target is a Word, else false
     */
    public boolean isLexical() {
        return target instanceof Word;
    }

    /**
     * If the target is a synset, return it, otherwise it's a word
     * so return the word's parent synset.
     *
     * @return If the target is a synset, return it, otherwise it's a word so return the word's parent synset
     */
    public Synset getSynset() {
        return target.getSynset();
    }

    /**
     * If the target is a word, return it, otherwise return null.
     *
     * @return if the target is a word, return it, otherwise return null
     */
    public Word getWord() {
        if (isLexical()) {
            return (Word) target;
        } else {
            return null;
        }
    }

    /**
     * Two PointerTargetNodes are equal if they have the same type and PointerTarget
     */
    public boolean equals(Object object) {
        if (object instanceof PointerTargetNode) {
            PointerTargetNode node = (PointerTargetNode) object;
            return getPointerTarget().equals(node.getPointerTarget()) && getType() == node.getType();
        }
        return false;
    }

    public String toString() {
        return ResourceBundleSet.insertParams("[PointerTargetNode: {0} {1}]", new Object[]{getPointerTarget(), getType()});
    }

    public int hashCode() {
        return getPointerTarget().hashCode() ^ getType().hashCode();
    }

    public PointerTargetNode clone() throws CloneNotSupportedException {
        return (PointerTargetNode) super.clone();
    }

    public PointerTargetNode deepClone() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}