package net.sf.extjwnl.dictionary.file;

import java.io.IOException;

/**
 * <code>DictionaryFile</code> that reads and writes serialized objects.
 *
 * @author John Didion <jdidion@users.sourceforge.net>
 */
public interface ObjectDictionaryFile extends DictionaryFile {
    /**
     * Read and deserialize an object from the file
     */
    public Object readObject() throws IOException, ClassNotFoundException;

    /**
     * Serialize and write an object ot the file.
     */
    public void writeObject(Object obj) throws IOException;
}
