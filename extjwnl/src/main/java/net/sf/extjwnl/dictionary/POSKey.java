package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.POS;

import java.io.IOException;

/**
 * A cache key consists of a <code>POS</code> and an object.
 *
 * @author John Didion <jdidion@didion.net>
 */
public class POSKey {
    private POS pos;
    private Object key;

    private POSKey(POS pos, Object key) {
        if (pos == null || key == null) {
            throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_001"));
        }
        this.pos = pos;
        this.key = key;
    }

    public POSKey(POS pos, String lemma) {
        this(pos, (Object) lemma);
    }

    public POSKey(POS pos, long offset) {
        this(pos, new Long(offset));
    }

    public boolean equals(Object object) {
        return object instanceof POSKey
                && ((POSKey) object).pos.equals(pos)
                && ((POSKey) object).key.equals(key);
    }

    public POS getPOS() {
        return pos;
    }

    public Object getKey() {
        return key;
    }

    public boolean isLemmaKey() {
        return key instanceof String;
    }

    public boolean isOffsetKey() {
        return key instanceof Long;
    }

    public int hashCode() {
        return pos.hashCode() ^ key.hashCode();
    }

    public String toString() {
        return JWNL.resolveMessage("DICTIONARY_TOSTRING_001", new Object[]{pos, key});
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        pos = POS.getPOSForKey(pos.getKey());
    }
}