package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNL;

/**
 * Adjective positions denote a restriction on the on the syntactic position the
 * adjective may have in relation to noun that it modifies. Adjective positions are
 * only used through WordNet version 1.6.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public enum AdjectivePosition {
    NONE("none", "NONE"),
    PREDICATIVE("p", "AP_PREDICATIVE"),
    ATTRIBUTIVE("a", "AP_ATTRIBUTIVE"),
    IMMEDIATE_POSTNOMINAL("ip", "AP_IMMEDIATE_POSTNOMINAL");

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

    private AdjectivePosition(String key, String label) {
        JWNL.initialize();
        this.key = key;
        this.label = JWNL.resolveMessage(label);
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public String toString() {
        return JWNL.resolveMessage("DATA_TOSTRING_006", label);
    }
}