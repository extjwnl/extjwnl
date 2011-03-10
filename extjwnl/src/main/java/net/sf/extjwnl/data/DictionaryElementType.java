package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNL;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * WordNet contains different file types, index, synset data, and exception files.
 * This class defines what the current dictionary is.
 *
 * @author Brett Walenz <bwalenz@users.sourceforge.net>
 * @author John Didion <jdidion@didion.net>
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public class DictionaryElementType {

    static {
        JWNL.initialize();
    }

    /**
     * Property to define an index file.
     */
    public static final DictionaryElementType INDEX_WORD = new DictionaryElementType("INDEX_WORD");

    /**
     * Property to define a synset file.
     */
    public static final DictionaryElementType SYNSET = new DictionaryElementType("SYNSET");

    /**
     * Property that defines an exception file.
     */
    public static final DictionaryElementType EXCEPTION = new DictionaryElementType("EXCEPTION");

    /**
     * The name of the dictionary element type.
     */
    private final String name;

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

    public int hashCode() {
        return name.hashCode();
    }
}