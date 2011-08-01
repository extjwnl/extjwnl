package net.sf.extjwnl.dictionary.file;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.util.factory.Owned;

import java.io.File;
import java.io.IOException;

/**
 * Represents a single dictionary file. Extensions or implementations of this interface should provide
 * the appropriate methods to read from the file.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface DictionaryFile extends Owned {

    /**
     * Closes the file.
     */
    void close();

    /**
     * Returns true if the file is open.
     *
     * @return true if the file is open
     */
    boolean isOpen();

    /**
     * Returns the POS associated with this file.
     *
     * @return the POS associated with this file
     */
    POS getPOS();

    /**
     * Returns the file.
     *
     * @return the file
     */
    File getFile();

    /**
     * Returns the file type associated with this file.
     *
     * @return the file type associated with this file
     */
    DictionaryFileType getFileType();

    /**
     * Opens the file.
     *
     * @throws IOException IOException
     */
    void open() throws IOException;

    /**
     * Deletes the file.
     *
     * @return true if succeeded
     * @throws IOException IOException
     */
    boolean delete() throws IOException;

    /**
     * Saves the file.
     *
     * @throws IOException   IOException
     * @throws JWNLException JWNLException
     */
    void save() throws IOException, JWNLException;

    /**
     * Reopens file in write mode.
     *
     * @throws IOException IOException
     */
    void edit() throws IOException;
}