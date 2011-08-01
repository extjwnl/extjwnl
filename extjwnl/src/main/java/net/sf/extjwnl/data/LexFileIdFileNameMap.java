package net.sf.extjwnl.data;

import java.util.*;

/**
 * Maps the lexicographer files identifiers to names. See LEXNAMES(5WN).
 *
 * @author Brett Walenz <bwalenz@users.sourceforge.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class LexFileIdFileNameMap implements Map<Long, String> {

    /**
     * A mapping of id's to files.
     */
    private static Map<Long, String> lexFileIdLexFileName;

    static {
        String[] names = {
                "adj.all",
                "adj.pert",
                "adv.all",
                "noun.Tops",
                "noun.act",
                "noun.animal",
                "noun.artifact",
                "noun.attribute",
                "noun.body",
                "noun.cognition",
                "noun.communication",
                "noun.event",
                "noun.feeling",
                "noun.food",
                "noun.group",
                "noun.location",
                "noun.motive",
                "noun.object",
                "noun.person",
                "noun.phenomenon",
                "noun.plant",
                "noun.possession",
                "noun.process",
                "noun.quantity",
                "noun.relation",
                "noun.shape",
                "noun.state",
                "noun.substance",
                "noun.time",
                "verb.body",
                "verb.change",
                "verb.cognition",
                "verb.communication",
                "verb.competition",
                "verb.consumption",
                "verb.contact",
                "verb.creation",
                "verb.emotion",
                "verb.motion",
                "verb.perception",
                "verb.possession",
                "verb.social",
                "verb.stative",
                "verb.weather",
                "adj.ppl"
        };

        lexFileIdLexFileName = new HashMap<Long, String>();
        for (int i = 0; i < names.length; i++) {
            lexFileIdLexFileName.put((long) i, names[i]);
        }
        lexFileIdLexFileName = Collections.unmodifiableMap(lexFileIdLexFileName);
    }

    public static Map<Long, String> getMap() {
        return lexFileIdLexFileName;
    }

    public int size() {
        return lexFileIdLexFileName.size();
    }

    public boolean equals(Object o) {
        return lexFileIdLexFileName.equals(o);
    }

    public int hashCode() {
        return lexFileIdLexFileName.hashCode();
    }

    public String toString() {
        return lexFileIdLexFileName.toString();
    }

    public boolean isEmpty() {
        return lexFileIdLexFileName.isEmpty();
    }

    public String get(Object key) {
        return lexFileIdLexFileName.get(key);
    }

    public boolean containsKey(Object key) {
        return lexFileIdLexFileName.containsKey(key);
    }

    public String put(Long key, String value) {
        return lexFileIdLexFileName.put(key, value);
    }

    public void putAll(Map<? extends Long, ? extends String> m) {
        lexFileIdLexFileName.putAll(m);
    }

    public String remove(Object key) {
        return lexFileIdLexFileName.remove(key);
    }

    public void clear() {
        lexFileIdLexFileName.clear();
    }

    public boolean containsValue(Object value) {
        return lexFileIdLexFileName.containsValue(value);
    }

    public Set<Long> keySet() {
        return lexFileIdLexFileName.keySet();
    }

    public Collection<String> values() {
        return lexFileIdLexFileName.values();
    }

    public Set<Map.Entry<Long, String>> entrySet() {
        return lexFileIdLexFileName.entrySet();
    }
}