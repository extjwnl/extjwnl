package net.didion.jwnl.data;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;

import java.io.IOException;
import java.io.Serializable;

/**
 * A <code>Pointer</code> encodes a lexical or semantic relationship between WordNet entities.  A lexical
 * relationship holds between Words; a semantic relationship holds between Synsets.  Relationships
 * are <it>directional</it>:  the two roles of a relationship are the <it>source</it> and <it>target</it>.
 * Relationships are <it>typed</it>: the type of a relationship is a {@link PointerType}, and can
 * be retrieved via {@link Pointer#getType getType}.
 */
public class Pointer implements Serializable {
    static final long serialVersionUID = -1275571290466732179L;

    private PointerType _pointerType;

    private TargetIndex _targetIndex;


    /**
     * The source of this poiner. If the pointer applies to all words in the
     * parent synset, then <code>source</code> and <code>synset</code> are the same,
     * otherwise <code>source</code> is the specific <code>Word</code> object that
     * this pointer applies to.
     */
    private PointerTarget _source = null;

    /**
     * Cache for the target after it has been resolved.
     */
    private transient PointerTarget _target = null;

    public Pointer(PointerTarget source, PointerType pointerType,
                   POS targetPOS, long targetOffset, int targetIndex) {
        _source = source;
        _pointerType = pointerType;
        _targetIndex = new TargetIndex(targetPOS, targetOffset, targetIndex);
    }

    public Pointer(PointerType pointerType, PointerTarget source, PointerTarget target) {
        _pointerType = pointerType;
        _source = source;
        _target = target;
    }

    public String toString() {
        String targetMsg = (_target == null) ? _targetIndex.toString() : _target.toString();
        return JWNL.resolveMessage("DATA_TOSTRING_012", new Object[]{getSourceIndex(), getSource(), targetMsg});
    }

    public int getSourceIndex() {
        return _source.getIndex();
    }

    public PointerType getType() {
        return _pointerType;
    }

    /**
     * True if this pointer's source is a Word
     */
    public boolean isLexical() {
        return getSource() instanceof Word;
    }

    /**
     * Get the source of this pointer.
     * @return source of this pointer
     */
    public PointerTarget getSource() {
        return _source;
    }

    /**
     * Get the actual target of this pointer.
     * @return actual target of this pointer
     * @throws JWNLException JWNLException
     */
    public PointerTarget getTarget() throws JWNLException {
        if (null == _target) {
            Dictionary dic = Dictionary.getInstance();
            Synset syn = dic.getSynsetAt(_targetIndex._pos, _targetIndex._offset);
            _target = (_targetIndex._index == 0) ? syn : syn.getWord(_targetIndex._index - 1);
        }
        return _target;
    }

    public void setTarget(PointerTarget target) {
        _target = target;
        _targetIndex = null;
    }

    /**
     * Get the synset that is a) the target of this pointer, or b) the synset that contains the target of this pointer.
     * @return the synset that is a) the target of this pointer, or b) the synset that contains the target of this pointer.
     * @throws JWNLException JWNLException
     */
    public Synset getTargetSynset() throws JWNLException {
        return getTarget().getSynset();
    }

    /**
     * Get the offset of the target synset.
     * @return offset of the target synset
     */
    public long getTargetOffset() {
        if (null == _target) {
            return _targetIndex._offset;
        } else {
            return _target.getSynset().getOffset();
        }
    }

    public int getTargetIndex() {
        if (null == _target) {
            return _targetIndex._index;
        } else {
            return _target.getIndex();
        }
    }

    public POS getTargetPOS() {
        if (null == _target) {
            return _targetIndex._pos;
        } else {
            return _target.getSynset().getPOS();
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // set pointer type to reference the static instance defined in the current runtime environment
        _pointerType = PointerType.getPointerTypeForKey(_pointerType.getKey());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pointer)) return false;

        Pointer pointer = (Pointer) o;

        if (_pointerType != null ? !_pointerType.equals(pointer._pointerType) : pointer._pointerType != null)
            return false;
        if (_source != null ? !_source.equals(pointer._source) : pointer._source != null) return false;
        if (null == _target) {
            if (_targetIndex != null ? !_targetIndex.equals(pointer._targetIndex) : pointer._targetIndex != null)
                return false;
        } else {
            if (!_target.getPOS().equals(pointer.getTargetPOS())
                    || _target.getIndex() != pointer.getTargetIndex()
                    || _target.getSynset().getOffset() != pointer.getTargetOffset())
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = _pointerType != null ? _pointerType.hashCode() : 0;
        result = 31 * result + (_targetIndex != null ? _targetIndex.hashCode() : 0);
        result = 31 * result + (_source != null ? _source.hashCode() : 0);
        return result;
    }

    /**
     * This class is used to avoid paging in the target before it is required, and to prevent
     * keeping a large portion of the database resident once the target has been queried.
     */
    private static class TargetIndex implements Serializable {
        POS _pos;
        long _offset;
        int _index;

        TargetIndex(POS pos, long offset, int index) {
            _pos = pos;
            _offset = offset;
            _index = index;
        }

        public String toString() {
            return JWNL.resolveMessage("DATA_TOSTRING_013", new Object[]{_pos, _offset, _index});
        }

        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            // set POS to reference the static instance defined in the current runtime environment
            _pos = POS.getPOSForKey(_pos.getKey());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TargetIndex)) return false;

            TargetIndex that = (TargetIndex) o;

            if (_index != that._index) return false;
            if (_offset != that._offset) return false;
            if (_pos != null ? !_pos.equals(that._pos) : that._pos != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = _pos != null ? _pos.hashCode() : 0;
            result = 31 * result + (int) (_offset ^ (_offset >>> 32));
            result = 31 * result + _index;
            return result;
        }
    }
}