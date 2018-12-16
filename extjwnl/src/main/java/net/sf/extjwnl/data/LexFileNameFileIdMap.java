package net.sf.extjwnl.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps the lexicographer files names to identifiers. See LEXNAMES(5WN).
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class LexFileNameFileIdMap {

    /**
     * A mapping of id's to files.
     */
    private static Map<String, Long> lexFileNameLexFileId;

    static {
        lexFileNameLexFileId = new HashMap<>();
        for (Map.Entry<Long, String> e : LexFileIdFileNameMap.getMap().entrySet()) {
            lexFileNameLexFileId.put(e.getValue(), e.getKey());
        }
        lexFileNameLexFileId = Collections.unmodifiableMap(lexFileNameLexFileId);
    }

    public static Map<String, Long> getMap() {
        return lexFileNameLexFileId;
    }
}