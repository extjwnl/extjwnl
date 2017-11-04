package net.sf.extjwnl.dictionary.file;

import net.sf.extjwnl.JWNLException;

/**
 * <code>DictionaryFile</code> that reads and writes serialized objects.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface ObjectDictionaryFile extends DictionaryFile {

    /**
     * Reads and deserializes an object from the file.
     *
     * @return deserialized an object
     * @throws JWNLException JWNLException
     */
    Object readObject() throws JWNLException;

    /**
     * Serializes and write an object ot the file.
     *
     * @param obj object to write
     * @throws JWNLException JWNLException
     */
    void writeObject(Object obj) throws JWNLException;
}
