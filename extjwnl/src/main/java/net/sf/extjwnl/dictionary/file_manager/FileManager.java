package net.sf.extjwnl.dictionary.file_manager;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.util.PointedCharSequence;
import net.sf.extjwnl.util.factory.Owned;

/**
 * <code>FileManager</code> defines the interface between the <code>FileBackedDictionary</code> and the file system.
 * Methods in this interface operate on and return offsets, which are indices into a dictionary file.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface FileManager extends Owned {

    /**
     * Returns the line whose first word is <var>index</var> or null if not found.
     *
     * @param pos      part of speech
     * @param fileType file type
     * @param index    word to search for
     * @return the matching line, or null if no such line exists.
     * @throws JWNLException JWNLException
     */
    PointedCharSequence getIndexedLine(POS pos, DictionaryFileType fileType, String index) throws JWNLException;

    /**
     * Returns the line that begins at file offset <var>offset</var>.
     *
     * @param pos      part of speech
     * @param fileType file type
     * @param offset   file offset
     * @return the line that begins at file offset <var>offset</var>
     * @throws JWNLException JWNLException
     */
    PointedCharSequence readLineAt(POS pos, DictionaryFileType fileType, long offset) throws JWNLException;

    /**
     * Returns the line whose index word contains <var>substring</var>,
     * starting at <var>offset</var> or null if not found.
     *
     * @param pos       part of speech
     * @param fileType  file type
     * @param offset    file offset
     * @param substring substring to search for in indexword
     * @return the matching line, or null if not found.
     * @throws JWNLException JWNLException
     */
    PointedCharSequence getMatchingLine(POS pos, DictionaryFileType fileType, long offset, String substring) throws JWNLException;

    /**
     * Returns a randomly-chosen line.
     *
     * @param pos      part of speech
     * @param fileType file type
     * @return a randomly-chosen line
     * @throws JWNLException JWNLException
     */
    PointedCharSequence getRandomLine(POS pos, DictionaryFileType fileType) throws JWNLException;

    /**
     * Returns the offset of the first valid line in the file (i.e. skips header if present).
     *
     * @param pos      part of speech
     * @param fileType file type
     * @return the offset of the first valid line in the file
     * @throws JWNLException JWNLException
     */
    long getFirstLineOffset(POS pos, DictionaryFileType fileType) throws JWNLException;

    /**
     * Returns the number of times the sense marked by <var>senseKey</var> occurs in a semantic concordance.
     *
     * @param senseKey sense key
     * @return the number of times the sense marked by <var>senseKey</var> occurs in a semantic concordance
     * @throws JWNLException JWNLException
     */
    int getUseCount(String senseKey) throws JWNLException;

    /**
     * Shuts down the file manager.
     *
     * @throws JWNLException JWNLException
     */
    void close() throws JWNLException;

    /**
     * Saves the files.
     *
     * @throws JWNLException JWNLException
     */
    void save() throws JWNLException;

    /**
     * Deletes the dictionary.
     *
     * @return true if successfully deleted
     * @throws JWNLException JWNLException
     */
    boolean delete() throws JWNLException;

    /**
     * Reopens files for writing.
     *
     * @throws JWNLException JWNLException
     */
    void edit() throws JWNLException;
}