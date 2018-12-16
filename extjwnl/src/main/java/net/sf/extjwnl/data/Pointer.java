package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.util.ResourceBundleSet;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

/**
 * A <code>Pointer</code> encodes a lexical or semantic relationship between WordNet entities.  A lexical
 * relationship holds between Words; a semantic relationship holds between Synsets.  Relationships
 * are <i>directional</i>:  the two roles of a relationship are the <i>source</i> and <i>target</i>.
 * Relationships are <i>typed</i>: the type of a relationship is a {@link PointerType}, and can
 * be retrieved via {@link Pointer#getType getType}.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class Pointer implements Serializable {

    private static final long serialVersionUID = 5L;

    private final PointerType pointerType;

    private TargetIndex targetIndex;

    /**
     * The source of this pointer. If the pointer applies to all words in the
     * parent synset, then <code>source</code> and <code>synset</code> are the same,
     * otherwise <code>source</code> is the specific <code>Word</code> object that
     * this pointer applies to.
     */
    private final PointerTarget source;

    /**
     * Cache for the target after it has been resolved.
     */
    private transient PointerTarget target;

    public Pointer(PointerTarget source, PointerType pointerType, POS targetPOS, long targetOffset, int targetIndex) {
        if (null == source) {
            throw new IllegalArgumentException("Source must be not null");
        }
        this.source = source;
        if (null == pointerType) {
            throw new IllegalArgumentException("Pointer type must be not null");
        }
        this.pointerType = pointerType;
        if (null == targetPOS) {
            throw new IllegalArgumentException("Target POS must be not null");
        }
        this.targetIndex = new TargetIndex(targetPOS, targetOffset, targetIndex);
        this.target = null;
    }

    public Pointer(PointerType pointerType, PointerTarget source, PointerTarget target) {
        if (null == pointerType) {
            throw new IllegalArgumentException("Pointer type must be not null");
        }
        this.pointerType = pointerType;
        if (null == source) {
            throw new IllegalArgumentException("Source must be not null");
        }
        this.source = source;
        if (null == target) {
            throw new IllegalArgumentException("Target must be not null");
        }
        this.target = target;
        if (source.getDictionary() != target.getDictionary()) {
            if (null != source.getDictionary()) {
                throw new IllegalArgumentException(source.getDictionary().getMessages().resolveMessage("DICTIONARY_EXCEPTION_063"));
            } else {
                if (null != target.getDictionary()) {
                    throw new IllegalArgumentException(target.getDictionary().getMessages().resolveMessage("DICTIONARY_EXCEPTION_063"));
                }
                //else {
                    // should never get there because of null == null above
                    //throw new IllegalArgumentException("Source and target must belong to the same dictionary");
                //}
            }
        }
    }

    public String toString() {
        String targetMsg = (target == null) ? targetIndex.toString() : target.toString();
        return ResourceBundleSet.insertParams("[PointerTarget: [Source Index: {0}] Source: {1} Target: {2}]",
                new Object[]{getSourceIndex(), getSource(), targetMsg});
    }

    public int getSourceIndex() {
        return source.getIndex();
    }

    public PointerType getType() {
        return pointerType;
    }

    /**
     * Returns whether this pointer is between two words.
     *
     * @return true if this pointer is between two words.
	 * @throws JWNLException JWNLException
     */
    public boolean isLexical() throws JWNLException {
        return (getSource() instanceof Word) && (getTarget() instanceof Word);
    }

    /**
     * Returns whether this pointer is between two synsets.
     *
     * @return true if this pointer is between two synsets.
	 * @throws JWNLException JWNLException
     */
    public boolean isSemantic() throws JWNLException {
        return (getSource() instanceof Synset) && (getTarget() instanceof Synset);
    }

    /**
     * Returns whether <var>that</var> is symmetric to this pointer.
     * @param that pointer
     * @return true, if <var>that</var> is symmetric to this pointer.
	 * @throws JWNLException JWNLException
     */
    public boolean isSymmetricTo(Pointer that) throws JWNLException {
        return
                null != pointerType.getSymmetricType()
                && that.getType().equals(pointerType.getSymmetricType())
                && that.getTarget().equals(getSource())
                && that.getSource().equals(getTarget());
    }

    /**
     * Returns the source of this pointer.
     *
     * @return source of this pointer
     */
    public PointerTarget getSource() {
        return source;
    }

    /**
     * Returns the actual target of this pointer.
     *
     * @return actual target of this pointer
	 * @throws JWNLException JWNLException
     */
    public PointerTarget getTarget() throws JWNLException {
        if (null == target && null != source.getDictionary()) {
            Synset syn = source.getDictionary().getSynsetAt(targetIndex.pos, targetIndex.offset);
            target = (targetIndex.index == 0) ? syn : (null == syn ? null : syn.getWords().get(targetIndex.index - 1));
            if (null != target && source.getDictionary().isEditable()) {
                targetIndex = null;
            }
        }
        return target;
    }

    /**
     * Sets the actual target of this pointer.
     *
     * @param target actual target of this pointer
     */
    public void setTarget(PointerTarget target) {
        this.target = target;
        targetIndex = null;
    }

    /**
     * Returns the synset that is a) the target of this pointer, or b) the synset that contains the target of this pointer.
     *
     * @return the synset that is a) the target of this pointer, or b) the synset that contains the target of this pointer.
	 * @throws JWNLException JWNLException
     */
    public Synset getTargetSynset() throws JWNLException {
        if (null == getTarget()) {
            return null;
        }
        return getTarget().getSynset();
    }

    /**
     * Returns the offset of the target synset.
     *
     * @return offset of the target synset
	 * @throws JWNLException JWNLException
     */
    public long getTargetOffset() throws JWNLException {
        if (null == target) {
            if (null != source.getDictionary() && source.getDictionary().isEditable()) {
                return getTarget().getSynset().getOffset();
            } else {
                return targetIndex.offset;
            }
        } else {
            return target.getSynset().getOffset();
        }
    }

    public int getTargetIndex() throws JWNLException {
        if (null == target) {
            if (null != source.getDictionary() && source.getDictionary().isEditable()) {
                return getTarget().getIndex();
            } else {
                return targetIndex.index;
            }
        } else {
            return target.getIndex();
        }
    }

    public POS getTargetPOS() throws JWNLException {
        if (null == target) {
            if (null != source.getDictionary() && source.getDictionary().isEditable()) {
                return getTarget().getSynset().getPOS();
            } else {
                return targetIndex.pos;
            }
        } else {
            return target.getSynset().getPOS();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pointer)) {
            return false;
        }

        Pointer pointer = (Pointer) o;

        if (!pointerType.equals(pointer.pointerType)) {
            return false;
        }
        if (!source.equals(pointer.source)) {
            return false;
        }
        if (null == target) {
            if (!Objects.equals(targetIndex, pointer.targetIndex)) {
                return false;
            }
        } else {
            try {
                if (!target.getPOS().equals(pointer.getTargetPOS())
                        || target.getIndex() != pointer.getTargetIndex()
                        || target.getSynset().getOffset() != pointer.getTargetOffset()) {
                    return false;
                }
            } catch (JWNLException e) {
                throw new JWNLRuntimeException(e);
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = pointerType.hashCode();
        result = 31 * result + (targetIndex != null ? targetIndex.hashCode() : 0);
        result = 31 * result + source.hashCode();
        return result;
    }

    /**
     * This class is used to avoid paging in the target before it is required, and to prevent
     * keeping a large portion of the database resident once the target has been queried.
     */
    private static class TargetIndex implements Serializable {
        private final POS pos;
        private final long offset;
        private final int index;

        TargetIndex(POS pos, long offset, int index) {
            this.pos = pos;
            this.offset = offset;
            this.index = index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TargetIndex)) {
                return false;
            }

            TargetIndex that = (TargetIndex) o;

            if (index != that.index) {
                return false;
            }
            if (offset != that.offset) {
                return false;
            }
            //noinspection RedundantIfStatement
            if (!Objects.equals(pos, that.pos)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = pos != null ? pos.hashCode() : 0;
            result = 31 * result + (int) (offset ^ (offset >>> 32));
            result = 31 * result + index;
            return result;
        }
    }

    private void writeObject(java.io.ObjectOutputStream oos) throws IOException {
        boolean wasNull = null == targetIndex;
        try {
            this.targetIndex = new TargetIndex(getTargetPOS(), getTargetOffset(), getTargetIndex());
        } catch (JWNLException e) {
            throw new JWNLRuntimeException(e);
        }
        oos.defaultWriteObject();
        if (wasNull) {
            this.targetIndex = null;
        }
    }
}