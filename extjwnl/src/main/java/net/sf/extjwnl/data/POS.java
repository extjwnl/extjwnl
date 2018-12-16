package net.sf.extjwnl.data;

import net.sf.extjwnl.util.ResourceBundleSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration of major syntactic categories, or <b>P</b>art's <b>O</b>f <b>S</b>peech.
 * Each <code>POS</code> has a human-readable label that can be used to print it, and a key by which it can be looked up.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public enum POS {

    NOUN(1, "n", "noun"),
    VERB(2, "v", "verb"),
    ADJECTIVE(3, "a", "adjective"),
    ADVERB(4, "r", "adverb");

    public static final String ADJECTIVE_SATELLITE_KEY = "s";
    public static final int ADJECTIVE_SATELLITE_ID = 5;

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
        if (NOUN.getKey().equals(key)) {
            return POS.NOUN;
        }
        if (VERB.getKey().equals(key)) {
            return POS.VERB;
        }
        if (ADJECTIVE.getKey().equals(key)) {
            return POS.ADJECTIVE;
        }
        if (ADVERB.getKey().equals(key)) {
            return POS.ADVERB;
        }
        if (ADJECTIVE_SATELLITE_KEY.equals(key)) {
            return POS.ADJECTIVE;
        }
        return null;
    }

    /**
     * Return the <code>POS</code> whose key matches <var>key</var>,
     * or null if the key does not match any POS.
     *
     * @param key POS key
     * @return POS
     */
    public static POS getPOSForKey(char key) {
        if (key == NOUN.getKey().charAt(0)) {
            return POS.NOUN;
        }
        if (key == VERB.getKey().charAt(0)) {
            return POS.VERB;
        }
        if (key == ADJECTIVE.getKey().charAt(0)) {
            return POS.ADJECTIVE;
        }
        if (key == ADVERB.getKey().charAt(0)) {
            return POS.ADVERB;
        }
        if (key == ADJECTIVE_SATELLITE_KEY.charAt(0)) {
            return POS.ADJECTIVE;
        }
        return null;
    }

    /**
     * Return the <code>POS</code> whose id matches <var>id</var>,
     * or null if the id does not match any POS.
     *
     * @param id id for POS
     * @return POS
     */
    public static POS getPOSForId(int id) {
        if (NOUN.getId() == id) {
            return POS.NOUN;
        }
        if (VERB.getId() == id) {
            return POS.VERB;
        }
        if (ADJECTIVE.getId() == id) {
            return POS.ADJECTIVE;
        }
        if (ADVERB.getId() == id) {
            return POS.ADVERB;
        }
        if (ADJECTIVE_SATELLITE_ID == id) {
            return POS.ADJECTIVE;
        }
        return null;
    }


    private final transient String label;
    private final transient int id;
    private final transient String key;

    POS(int id, String key, String label) {
        this.id = id;
        this.key = key;
        this.label = label;
    }

    public String toString() {
        return ResourceBundleSet.insertParams("[POS: {0}]", new String[] {getLabel()});
    }

    /**
     * Return a label intended for textual presentation.
     *
     * @return a label intended for textual presentation
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the key for this POS.
     *
     * @return key for this POS
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the id for this POS.
     *
     * @return id for this POS
     */
    public int getId() {
        return id;
    }
}