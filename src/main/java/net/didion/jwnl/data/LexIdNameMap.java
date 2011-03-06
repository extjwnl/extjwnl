package net.didion.jwnl.data;

import java.io.Serializable;
import java.util.*;

/**
 * Maps the lexicographer files identifiers to names. See LEXNAMES(5WN).
 *
 * @author brett
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class LexIdNameMap implements Map<Long, String>, Serializable {

    /**
     * A mapping of id's to files.
     */
    private static Map<Long, String> lexIdName;

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

        lexIdName = new HashMap<Long, String>();
        for (int i = 0; i < names.size(); i++) {
            lexIdName.put((long) i, names.get(i));
        }
        lexIdName = Collections.unmodifiableMap(lexIdName);
    }

    public static Map<Long, String> getMap() {
        return lexIdName;
    }

    public int size() {
        return lexIdName.size();
    }

    public boolean equals(Object o) {
        return lexIdName.equals(o);
    }

    public int hashCode() {
        return lexIdName.hashCode();
    }

    public String toString() {
        return lexIdName.toString();
    }

    public boolean isEmpty() {
        return lexIdName.isEmpty();
    }

    public String get(Object key) {
        return lexIdName.get(key);
    }

    public boolean containsKey(Object key) {
        return lexIdName.containsKey(key);
    }

    public String put(Long key, String value) {
        return lexIdName.put(key, value);
    }

    public void putAll(Map<? extends Long, ? extends String> m) {
        lexIdName.putAll(m);
    }

    public String remove(Object key) {
        return lexIdName.remove(key);
    }

    public void clear() {
        lexIdName.clear();
    }

    public boolean containsValue(Object value) {
        return lexIdName.containsValue(value);
    }

    public Set<Long> keySet() {
        return lexIdName.keySet();
    }

    public Collection<String> values() {
        return lexIdName.values();
    }

    public Set<Map.Entry<Long, String>> entrySet() {
        return lexIdName.entrySet();
    }
}