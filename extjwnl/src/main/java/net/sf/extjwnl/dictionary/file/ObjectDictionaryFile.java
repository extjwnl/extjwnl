package net.sf.extjwnl.dictionary.file;

import java.io.IOException;

/**
 * <code>DictionaryFile</code> that reads and writes serialized objects.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface ObjectDictionaryFile extends DictionaryFile {

    /**
     * Reads and deserializes an object from the file.
     *
     * @return deserialized an object
     * @throws IOException            IOException
     * @throws ClassNotFoundException ClassNotFoundException
     */
    Object readObject() throws IOException, ClassNotFoundException;

    /**
     * Serializes and write an object ot the file.
     *
     * @param obj object to write
     * @throws IOException IOException
     */
    void writeObject(Object obj) throws IOException;
}
