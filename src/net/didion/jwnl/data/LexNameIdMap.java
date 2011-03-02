package net.didion.jwnl.data;

import java.io.Serializable;
import java.util.*;

/**
 * Maps the lexicographer files names to identifiers. See LEXNAMES(5WN).
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class LexNameIdMap implements Map<String, Long>, Serializable {

    /**
     * A mapping of id's to files.
     */
    private static Map<String, Long> lexNameId;

    static {
        lexNameId = new HashMap<String, Long>();
        for (Map.Entry<Long, String> e : LexIdNameMap.getMap().entrySet()) {
            lexNameId.put(e.getValue(), e.getKey());
        }
        lexNameId = Collections.unmodifiableMap(lexNameId);
    }

    public static Map<String, Long> getMap() {
        return lexNameId;
    }

    public int size() {
        return lexNameId.size();
    }

    public boolean equals(Object o) {
        return lexNameId.equals(o);
    }

    public int hashCode() {
        return lexNameId.hashCode();
    }

    public String toString() {
        return lexNameId.toString();
    }

    public boolean isEmpty() {
        return lexNameId.isEmpty();
    }

    public Long get(Object key) {
        return lexNameId.get(key);
    }

    public boolean containsKey(Object key) {
        return lexNameId.containsKey(key);
    }

    public Long put(String key, Long value) {
        return lexNameId.put(key, value);
    }

    public void putAll(Map<? extends String, ? extends Long> m) {
        lexNameId.putAll(m);
    }

    public Long remove(Object key) {
        return lexNameId.remove(key);
    }

    public void clear() {
        lexNameId.clear();
    }

    public boolean containsValue(Object value) {
        return lexNameId.containsValue(value);
    }

    public Set<String> keySet() {
        return lexNameId.keySet();
    }

    public Collection<Long> values() {
        return lexNameId.values();
    }

    public Set<Map.Entry<String, Long>> entrySet() {
        return lexNameId.entrySet();
    }
}