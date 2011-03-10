package net.sf.extjwnl.data;

import java.util.*;

/**
 * Maps the lexicographer files identifiers to names. See LEXNAMES(5WN).
 *
 * @author Brett Walenz <bwalenz@users.sourceforge.net>
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public class LexFileIdFileNameMap implements Map<Long, String> {

    /**
     * A mapping of id's to files.
     */
    private static Map<Long, String> lexFileIdLexFileName;

    static {
        List<String> names = new ArrayList<String>();
        names.add("adj.all");
        names.add("adj.pert");
        names.add("adv.all");
        names.add("noun.Tops");
        names.add("noun.act");
        names.add("noun.animal");
        names.add("noun.artifact");
        names.add("noun.attribute");
        names.add("noun.body");
        names.add("noun.cognition");
        names.add("noun.communication");
        names.add("noun.event");
        names.add("noun.feeling");
        names.add("noun.food");
        names.add("noun.group");
        names.add("noun.location");
        names.add("noun.motive");
        names.add("noun.object");
        names.add("noun.person");
        names.add("noun.phenomenon");
        names.add("noun.plant");
        names.add("noun.possession");
        names.add("noun.process");
        names.add("noun.quantity");
        names.add("noun.relation");
        names.add("noun.shape");
        names.add("noun.state");
        names.add("noun.substance");
        names.add("noun.time");
        names.add("verb.body");
        names.add("verb.change");
        names.add("verb.cognition");
        names.add("verb.communication");
        names.add("verb.competition");
        names.add("verb.consumption");
        names.add("verb.contact");
        names.add("verb.creation");
        names.add("verb.emotion");
        names.add("verb.motion");
        names.add("verb.perception");
        names.add("verb.possession");
        names.add("verb.social");
        names.add("verb.stative");
        names.add("verb.weather");
        names.add("adj.ppl");

        lexFileIdLexFileName = new HashMap<Long, String>();
        for (int i = 0; i < names.size(); i++) {
            lexFileIdLexFileName.put((long) i, names.get(i));
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