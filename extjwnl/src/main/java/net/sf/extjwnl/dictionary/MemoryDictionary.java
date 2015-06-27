package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import org.w3c.dom.Document;

import java.util.*;

/**
 * A in-memory <code>Dictionary</code> backed by <code>HashMap</code>s.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class MemoryDictionary extends MapDictionary {

    public MemoryDictionary(Document doc) throws JWNLException {
        super(doc);
        for (POS pos : POS.values()) {
            Map<DictionaryFileType, Map<Object, DictionaryElement>> files = tableMap.get(pos);
            for (DictionaryFileType type : DictionaryFileType.getAllDictionaryFileTypes()) {
                Map<Object, DictionaryElement> file = new HashMap<>();
                files.put(type, file);
            }
        }
    }
}