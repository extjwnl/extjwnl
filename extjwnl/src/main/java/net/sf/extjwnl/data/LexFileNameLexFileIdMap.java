package net.sf.extjwnl.data;

import java.util.*;

/**
 * Maps the lexicographer files names to identifiers. See LEXNAMES(5WN).
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class LexFileNameLexFileIdMap implements Map<String, Long> {

    /**
     * A mapping of id's to files.
     */
    private static Map<String, Long> lexFileNameLexFileId;

    static {
        lexFileNameLexFileId = new HashMap<String, Long>();
        for (Map.Entry<Long, String> e : LexFileIdFileNameMap.getMap().entrySet()) {
            lexFileNameLexFileId.put(e.getValue(), e.getKey());
        }
        lexFileNameLexFileId = Collections.unmodifiableMap(lexFileNameLexFileId);
    }

    public static Map<String, Long> getMap() {
        return lexFileNameLexFileId;
    }

    public int size() {
        return lexFileNameLexFileId.size();
    }

    public boolean equals(Object o) {
        return lexFileNameLexFileId.equals(o);
    }

    public int hashCode() {
        return lexFileNameLexFileId.hashCode();
    }

    public String toString() {
        return lexFileNameLexFileId.toString();
    }

    public boolean isEmpty() {
        return lexFileNameLexFileId.isEmpty();
    }

    public Long get(Object key) {
        return lexFileNameLexFileId.get(key);
    }

    public boolean containsKey(Object key) {
        return lexFileNameLexFileId.containsKey(key);
    }

    public Long put(String key, Long value) {
        return lexFileNameLexFileId.put(key, value);
    }

    public void putAll(Map<? extends String, ? extends Long> m) {
        lexFileNameLexFileId.putAll(m);
    }

    public Long remove(Object key) {
        return lexFileNameLexFileId.remove(key);
    }

    public void clear() {
        lexFileNameLexFileId.clear();
    }

    public boolean containsValue(Object value) {
        return lexFileNameLexFileId.containsValue(value);
    }

    public Set<String> keySet() {
        return lexFileNameLexFileId.keySet();
    }

    public Collection<Long> values() {
        return lexFileNameLexFileId.values();
    }

    public Set<Map.Entry<String, Long>> entrySet() {
        return lexFileNameLexFileId.entrySet();
    }
}