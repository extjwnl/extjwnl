package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNL;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumerates different dictionary file types present in WordNet: index, synset data, and exception files.
 *
 * @author Brett Walenz <bwalenz@users.sourceforge.net>
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public enum DictionaryElementType {

    INDEX_WORD("INDEX_WORD"),
    SYNSET("SYNSET"),
    EXCEPTION("EXCEPTION");

    /**
     * The name of the dictionary element type.
     */
    private final transient String name;

    /**
     * A list of the different dictionary types.
     */
    private static final List<DictionaryElementType> ALL_TYPES = Collections.unmodifiableList(
            Arrays.asList(INDEX_WORD, SYNSET, EXCEPTION));


    /**
     * Returns all the dictionary types.
     *
     * @return all the dictionary types
     */
    public static List<DictionaryElementType> getAllDictionaryElementTypes() {
        return ALL_TYPES;
    }


    /**
     * Creates a new DictionaryElementType.
     *
     * @param name name
     */
    private DictionaryElementType(String name) {
        JWNL.initialize();
        this.name = JWNL.resolveMessage(name);
    }

    public String toString() {
        return JWNL.resolveMessage("DATA_TOSTRING_016", getName());
    }

    /**
     * Returns the name of this DictionaryElementType.
     *
     * @return the name of this DictionaryElementType
     */
    public String getName() {
        return name;
    }
}