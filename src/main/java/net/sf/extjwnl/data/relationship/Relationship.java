package net.sf.extjwnl.data.relationship;

import net.sf.extjwnl.data.PointerTarget;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.data.list.PointerTargetNodeList;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;

/**
 * A <code>Relationship</code> encapsulates the relationship between two synsets. Basically, it is a list of
 * synsets/words that one must traverse to get from the source synset to the target synset of the
 * relationship, for some relationship type.
 * <p/>
 * There are two types of relationships - {@link net.sf.extjwnl.data.relationship.SymmetricRelationship Symmetric}
 * and {@link net.sf.extjwnl.data.relationship.AsymmetricRelationship Asymmetric}.
 */
public abstract class Relationship {
    /**
     * The nodes that comprise the relationship.
     */
    private PointerTargetNodeList nodes;
    /**
     * The relationship's type
     */
    private PointerType type;

    private Synset sourceSynset;
    private Synset targetSynset;

    protected Relationship(PointerType type, PointerTargetNodeList nodes, Synset sourceSynset, Synset targetSynset) {
        this.type = type;
        this.nodes = nodes;
        this.sourceSynset = sourceSynset;
        this.targetSynset = targetSynset;
    }

    public abstract Relationship reverse() throws CloneNotSupportedException;

    /**
     * Get the list that contains the nodes of this relationship.
     */
    public PointerTargetNodeList getNodeList() {
        return nodes;
    }

    /**
     * Get the pointer target of the source node.
     */
    public PointerTarget getSourcePointerTarget() {
        return ((PointerTargetNode) nodes.get(0)).getPointerTarget();
    }

    /**
     * Get the pointer target of the target node.
     */
    public PointerTarget getTargetPointerTarget() {
        return ((PointerTargetNode) nodes.get(nodes.size() - 1)).getPointerTarget();
    }

    public String toString() {
        StringBufferOutputStream stream = new StringBufferOutputStream();
        nodes.print(new PrintStream(stream));
        return stream.getStringBuffer().toString();
    }

    /**
     * Two relationships are assumed equal if they have the same source synset, target synset, and type
     */
    public boolean equals(Object obj) {
        if (obj instanceof Relationship) {
            Relationship r = (Relationship) obj;
            return r.getType().equals(getType())
                    && r.getSourceSynset().equals(getSourceSynset())
                    && r.getTargetSynset().equals(getTargetSynset());
        }
        return false;
    }

    public PointerType getType() {
        return type;
    }

    /**
     * Get the Synset that is the source of this relationship.
     */
    public Synset getSourceSynset() {
        return sourceSynset;
    }

    /**
     * Get the Synset that is the target of this relationship.
     */
    public Synset getTargetSynset() {
        return targetSynset;
    }

    public int getSize() {
        return getNodeList().size();
    }

    /**
     * Get the depth of this relationship. Depth is a concept that can be defined by each relationship type.
     * The default notion of depth is the number of pointers that need to be traversed to go from the source
     * to target synset. This is basically getSize() - 1.
     */
    public int getDepth() {
        return getSize() - 1;
    }

    private static class StringBufferOutputStream extends OutputStream {
        private StringWriter writer = new StringWriter();

        public void write(int b) throws IOException {
            writer.write(b);
        }

        public StringBuffer getStringBuffer() {
            return writer.getBuffer();
        }
    }
}