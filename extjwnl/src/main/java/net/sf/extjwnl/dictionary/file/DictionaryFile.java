package net.sf.extjwnl.dictionary.file;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.util.factory.Owned;

/**
 * Represents a single dictionary file.
 * The "file" can represent something not necessary on the disk: stream, resource.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface DictionaryFile extends Owned {

    /**
     * Returns the POS associated with this file.
     *
     * @return the POS associated with this file
     */
    POS getPOS();

    /**
     * Returns a filename from the part-of-speech and the file type.
     *
     * @return a filename from the part-of-speech and the file type
     */
    String getFilename();

    /**
     * Returns the file type associated with this file.
     *
     * @return the file type associated with this file
     */
    DictionaryFileType getFileType();

    /**
     * Opens the file.
     *
     * @throws JWNLException JWNLException
     */
    void open() throws JWNLException;

    /**
     * Closes the file.
     *
     * @throws JWNLException JWNLException
     */
    void close() throws JWNLException;

    /**
     * Returns true if the file is open.
     *
     * @return true if the file is open
     */
    boolean isOpen();

    /**
     * Saves the file.
     *
     * @throws JWNLException JWNLException
     */
    void save() throws JWNLException;

    /**
     * Reopens file in write mode.
     *
     * @throws JWNLException JWNLException
     */
    void edit() throws JWNLException;
}