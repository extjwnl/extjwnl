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
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface DictionaryFile extends Owned {

    /**
     * Closes the file.
     */
    public void close();

    /**
     * Returns true if the file is open.
     */
    public boolean isOpen();

    /**
     * Returns the POS associated with this file.
     */
    public POS getPOS();

    /**
     * Returns the file.
     *
     * @return the file
     */
    public File getFile();

    /**
     * Returns the file type associated with this file.
     */
    public DictionaryFileType getFileType();

    /**
     * Opens the file.
     */
    public void open() throws IOException;

    /**
     * Deletes the file.
     *
     * @throws IOException IOException
     */
    public boolean delete() throws IOException;

    /**
     * Saves the file.
     *
     * @throws IOException IOException
     */
    public void save() throws IOException, JWNLException;

    /**
     * Reopens file in write mode.
     */
    void edit() throws IOException;
}