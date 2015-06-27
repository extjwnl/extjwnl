package net.sf.extjwnl.dictionary.file;

import net.sf.extjwnl.JWNLException;

import java.io.File;

/**
 * Represents a single dictionary file on disk.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface DictionaryDiskFile extends DictionaryFile {

    /**
     * Returns the file.
     *
     * @return the file
     */
    File getFile();

    /**
     * Deletes the file.
     *
     * @return true if succeeded
     * @throws JWNLException JWNLException
     */
    boolean delete() throws JWNLException;
}