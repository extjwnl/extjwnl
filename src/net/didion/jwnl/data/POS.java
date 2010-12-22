package net.didion.jwnl.data;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.util.Resolvable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Instances of this class enumerate the possible major syntactic categories, or
 * <b>P</b>art's <b>O</b>f <b>S</b>peech. Each <code>POS</code> has a human-readable
 * label that can be used to print it, and a key by which it can be looked up.
 */
public final class POS implements Serializable {
    static final long serialVersionUID = 4311120391558046419L;

    public static final POS NOUN = new POS("NOUN", "NOUN_KEY");
    public static final POS VERB = new POS("VERB", "VERB_KEY");
    public static final POS ADJECTIVE = new POS("ADJECTIVE", "ADJECTIVE_KEY");
    public static final POS ADVERB = new POS("ADVERB", "ADVERB_KEY");

    private static final List<POS> ALL_POS = Collections.unmodifiableList(Arrays.asList(NOUN, VERB, ADJECTIVE, ADVERB));

    public static List<POS> getAllPOS() {
        return ALL_POS;
    }

    /**
     * Return the <code>POS</code> whose key matches <var>label</var>,
     * or null if the label does not match any POS.
     *
     * @param label POS label
     * @return POS
     */
    public static POS getPOSForLabel(String label) {
        for (POS pos : ALL_POS) {
            if (pos.getLabel().equals(label)) {
                return pos;
            }
        }
        return null;
    }

    /**
     * Return the <code>POS</code> whose key matches <var>key</var>,
     * or null if the key does not match any POS.
     *
     * @param key key for POS
     * @return POS
     */
    public static POS getPOSForKey(String key) {
        for (POS pos : ALL_POS) {
            if (pos.getKey().equals(key)) {
                return pos;
            }
        }
        return null;
    }

    private Resolvable _label;
    private Resolvable _key;

    private POS(String label, String key) {
        _label = new Resolvable(label);
        _key = new Resolvable(key);
    }

    // Object methods

    private transient String _cachedToString = null;

    public String toString() {
        if (_cachedToString == null) {
            _cachedToString = JWNL.resolveMessage("DATA_TOSTRING_010", getLabel());
        }
        return _cachedToString;
    }

    /**
     * Returns the underlying pos key's hash code.
     *
     * @return key hash code
     */
    public int hashCode() {
        return _key.toString().hashCode();
    }

    /**
     * An instance of POS is equal to another iff they're underlying keys are
     * equal.
     *
     * @param obj the comparison object
     * @return true if keys equal
     */
    public boolean equals(Object obj) {
        if (obj instanceof POS) {
            POS pos = (POS) obj;
            return _key.toString().equals(pos.getKey());
        }
        return false;
    }

    /**
     * Return a label intended for textual presentation.
     *
     * @return a label intended for textual presentation
     */
    public String getLabel() {
        return _label.toString();
    }

    /**
     * Gets the key for this POS.
     *
     * @return key for this POS
     */
    public String getKey() {
        return _key.toString();
    }
}