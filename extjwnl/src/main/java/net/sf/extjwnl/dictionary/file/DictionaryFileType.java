package net.sf.extjwnl.dictionary.file;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.DictionaryElementType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumerates different types of dictionary files.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public enum DictionaryFileType {

    INDEX("index", DictionaryElementType.INDEX_WORD),
    DATA("data", DictionaryElementType.SYNSET),
    EXCEPTION("exception", DictionaryElementType.EXCEPTION),
    REVCNTLIST("cntlist.rev", null),
    CNTLIST("cntlist", null);

    private static final List<DictionaryFileType> ALL_TYPES = Collections.unmodifiableList(Arrays.asList(INDEX, DATA, EXCEPTION));

    public static List<DictionaryFileType> getAllDictionaryFileTypes() {
        return ALL_TYPES;
    }

    private final transient String name;
    private final transient DictionaryElementType elementType;

    private DictionaryFileType(String type, DictionaryElementType elementType) {
        name = type;
        this.elementType = elementType;
    }

    public String getName() {
        return name;
    }

    public DictionaryElementType getElementType() {
        return elementType;
    }

    public String toString() {
        return JWNL.resolveMessage("DICTIONARY_TOSTRING_002", getName());
    }
}