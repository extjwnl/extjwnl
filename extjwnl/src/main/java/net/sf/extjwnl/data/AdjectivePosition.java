package net.sf.extjwnl.data;

import net.sf.extjwnl.util.ResourceBundleSet;

/**
 * Adjective positions denote a restriction on the on the syntactic position the
 * adjective may have in relation to noun that it modifies. Adjective positions are
 * only used through WordNet version 1.6.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public enum AdjectivePosition {
    NONE("none", "none"),
    PREDICATIVE("p", "predicative"),
    ATTRIBUTIVE("a", "attributive"),
    IMMEDIATE_POSTNOMINAL("ip", "immediate postnominal");

    public static AdjectivePosition getAdjectivePositionForKey(String key) {
        if (NONE.getKey().equals(key)) {
            return NONE;
        }
        if (PREDICATIVE.getKey().equals(key)) {
            return PREDICATIVE;
        }
        if (ATTRIBUTIVE.getKey().equals(key)) {
            return ATTRIBUTIVE;
        }
        if (IMMEDIATE_POSTNOMINAL.getKey().equals(key)) {
            return IMMEDIATE_POSTNOMINAL;
        }
        return null;
    }

    private transient final String key;
    private transient final String label;

    AdjectivePosition(String key, String label) {
        this.key = key;
        this.label = label;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public String toString() {
        return ResourceBundleSet.insertParams("[AdjectivePosition: {0}]", new String[]{label});
    }
}