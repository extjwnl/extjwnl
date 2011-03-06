package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.util.Resolvable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Instances of this class enumerate the possible major syntactic categories, or
 * <b>P</b>art's <b>O</b>f <b>S</b>peech. Each <code>POS</code> has a human-readable
 * label that can be used to print it, and a key by which it can be looked up.
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class POS implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String NOUN_KEY = "n";
    private static final String VERB_KEY = "v";
    private static final String ADJECTIVE_KEY = "a";
    private static final String ADVERB_KEY = "r";
    private static final String ADJECTIVE_SATELLITE_KEY = "s";

    public static final POS NOUN = new POS("NOUN", NOUN_KEY);
    public static final POS VERB = new POS("VERB", VERB_KEY);
    public static final POS ADJECTIVE = new POS("ADJECTIVE", ADJECTIVE_KEY);
    public static final POS ADVERB = new POS("ADVERB", ADVERB_KEY);

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
        if (NOUN_KEY.equals(key)) {
            return POS.NOUN;
        }
        if (VERB_KEY.equals(key)) {
            return POS.VERB;
        }
        if (ADJECTIVE_KEY.equals(key)) {
            return POS.ADJECTIVE;
        }
        if (ADVERB_KEY.equals(key)) {
            return POS.ADVERB;
        }
        if (ADJECTIVE_SATELLITE_KEY.equals(key)) {
            return POS.ADJECTIVE;
        }
        return null;
    }

    private Resolvable label;
    private String key;

    private POS(String label, String key) {
        this.label = new Resolvable(label);
        this.key = key;
    }

    // Object methods

    public String toString() {
        return JWNL.resolveMessage("DATA_TOSTRING_010", getLabel());
    }

    /**
     * Returns the underlying pos key's hash code.
     *
     * @return key hash code
     */
    public int hashCode() {
        return key.hashCode();
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
            return key.equals(pos.getKey());
        }
        return false;
    }

    /**
     * Return a label intended for textual presentation.
     *
     * @return a label intended for textual presentation
     */
    public String getLabel() {
        return label.toString();
    }

    /**
     * Gets the key for this POS.
     *
     * @return key for this POS
     */
    public String getKey() {
        return key;
    }
}