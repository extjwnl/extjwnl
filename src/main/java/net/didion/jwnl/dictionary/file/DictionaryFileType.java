package net.didion.jwnl.dictionary.file;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.DictionaryElementType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Instances of this class specify the different types of dictionary files (the different classes of dictionary files.
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class DictionaryFileType {

    private static final String INDEX_KEY = "index";
    private static final String DATA_KEY = "data";
    private static final String EXCEPTION_KEY = "exception";

    // File type constants
    public static final DictionaryFileType INDEX = new DictionaryFileType(INDEX_KEY, DictionaryElementType.INDEX_WORD);
    public static final DictionaryFileType DATA = new DictionaryFileType(DATA_KEY, DictionaryElementType.SYNSET);
    public static final DictionaryFileType EXCEPTION = new DictionaryFileType(EXCEPTION_KEY, DictionaryElementType.EXCEPTION);

    private static final List<DictionaryFileType> ALL_TYPES = Collections.unmodifiableList(Arrays.asList(INDEX, DATA, EXCEPTION));

    public static List<DictionaryFileType> getAllDictionaryFileTypes() {
        return ALL_TYPES;
    }

    private String name;
    private DictionaryElementType elementType;

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

    public int hashCode() {
        return getName().hashCode();
    }
}