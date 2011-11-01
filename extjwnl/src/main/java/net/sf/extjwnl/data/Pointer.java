package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.Serializable;

/**
 * A <code>Pointer</code> encodes a lexical or semantic relationship between WordNet entities.  A lexical
 * relationship holds between Words; a semantic relationship holds between Synsets.  Relationships
 * are <it>directional</it>:  the two roles of a relationship are the <it>source</it> and <it>target</it>.
 * Relationships are <it>typed</it>: the type of a relationship is a {@link PointerType}, and can
 * be retrieved via {@link Pointer#getType getType}.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class Pointer implements Serializable {

    private static final long serialVersionUID = 4L;

    private static final Log log = LogFactory.getLog(Pointer.class);

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

    public Pointer(PointerTarget source, PointerType pointerType,
                   POS targetPOS, long targetOffset, int targetIndex) {
        this.source = source;
        this.pointerType = pointerType;
        this.targetIndex = new TargetIndex(targetPOS, targetOffset, targetIndex);
        this.target = null;
    }

    public Pointer(PointerType pointerType, PointerTarget source, PointerTarget target) {
        this.pointerType = pointerType;
        this.source = source;
        this.target = target;
    }

    public String toString() {
        String targetMsg = (target == null) ? targetIndex.toString() : target.toString();
        return JWNL.resolveMessage("DATA_TOSTRING_012", new Object[]{getSourceIndex(), getSource(), targetMsg});
    }

    public int getSourceIndex() {
        return source.getIndex();
    }

    public PointerType getType() {
        return pointerType;
    }

    /**
     * Returns whether this pointer's source is a Word.
     *
     * @return true if this pointer's source is a Word
     */
    public boolean isLexical() {
        return getSource() instanceof Word;
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
     */
    public PointerTarget getTarget() {
        try {
            if (null == target && null != source.getDictionary()) {
                Synset syn = source.getDictionary().getSynsetAt(targetIndex.pos, targetIndex.offset);
                target = (targetIndex.index == 0) ? syn : (null == syn ? null : syn.getWords().get(targetIndex.index - 1));
                if (null != target && source.getDictionary().isEditable()) {
                    targetIndex = null;
                }
            }
        } catch (JWNLException e) {
            if (log.isErrorEnabled()) {
                log.error(JWNL.resolveMessage("EXCEPTION_001", e.getMessage()), e);
            }
        } catch (IndexOutOfBoundsException e) {
            if (log.isErrorEnabled()) {
                log.error(JWNL.resolveMessage("EXCEPTION_001", e.getMessage()), e);
                log.error(JWNL.resolveMessage("EXCEPTION_002", new Object[]{source.getSynset().getOffset(), targetIndex.offset, targetIndex.index}));
            }
            throw e;
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
     */
    public Synset getTargetSynset() {
        if (null == getTarget()) {
            return null;
        }
        return getTarget().getSynset();
    }

    /**
     * Returns the offset of the target synset.
     *
     * @return offset of the target synset
     */
    public long getTargetOffset() {
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

    public int getTargetIndex() {
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

    public POS getTargetPOS() {
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

        if (pointerType != null ? !pointerType.equals(pointer.pointerType) : pointer.pointerType != null) {
            return false;
        }
        if (source != null ? !source.equals(pointer.source) : pointer.source != null) {
            return false;
        }
        if (null == target) {
            if (targetIndex != null ? !targetIndex.equals(pointer.targetIndex) : pointer.targetIndex != null) {
                return false;
            }
        } else {
            if (!target.getPOS().equals(pointer.getTargetPOS())
                    || target.getIndex() != pointer.getTargetIndex()
                    || target.getSynset().getOffset() != pointer.getTargetOffset()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = pointerType != null ? pointerType.hashCode() : 0;
        result = 31 * result + (targetIndex != null ? targetIndex.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
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

        public String toString() {
            return JWNL.resolveMessage("DATA_TOSTRING_013", new Object[]{pos, offset, index});
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
            if (pos != null ? !pos.equals(that.pos) : that.pos != null) {
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
        this.targetIndex = new TargetIndex(getTargetPOS(), getTargetOffset(), getTargetIndex());
        oos.defaultWriteObject();
        if (wasNull) {
            this.targetIndex = null;
        }
    }
}