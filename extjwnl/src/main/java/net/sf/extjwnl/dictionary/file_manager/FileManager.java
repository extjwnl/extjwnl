package net.sf.extjwnl.dictionary.file_manager;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.util.factory.Owned;

import java.io.IOException;

/**
 * <code>FileManager</code> defines the interface between the <code>FileBackedDictionary</code> and the file system.
 * Methods in this interface operate on and return offsets, which are indices into a dictionary file.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface FileManager extends Owned {
    /**
     * Search for the line whose first word is <var>index</var> (that is, that begins with
     * <var>index</var> followed by a space or tab).
     *
     * @param pos      part of speech
     * @param fileType file type
     * @param index    word to search for
     * @return The file offset of the start of the matching line, or <code>-1</code> if no such line exists.
     * @throws IOException IOException
     */
    long getIndexedLinePointer(POS pos, DictionaryFileType fileType, String index) throws IOException;

    /**
     * Read the line that begins at file offset <var>offset</var>.
     *
     * @param pos      part of speech
     * @param fileType file type
     * @param offset   file offset
     * @return the line that begins at file offset <var>offset</var>
     * @throws IOException IOException
     */
    String readLineAt(POS pos, DictionaryFileType fileType, long offset) throws IOException;

    /**
     * Search for the line following the line that begins at <var>offset</var>.
     *
     * @param pos      part of speech
     * @param fileType file type
     * @param offset   file offset
     * @return The file offset of the start of the line, or <code>-1</code> if <var>offset</var>
     *         is the last line in the file.
     * @throws IOException IOException
     */
    long getNextLinePointer(POS pos, DictionaryFileType fileType, long offset) throws IOException;

    /**
     * Search for a line whose index word contains <var>substring</var>, starting at <var>offset</var>.
     *
     * @param pos       part of speech
     * @param fileType  file type
     * @param offset    file offset
     * @param substring substring to search for in indexword
     * @return The file offset of the start of the matching line, or <code>-1</code> if
     *         no such line exists.
     * @throws IOException IOException
     */
    long getMatchingLinePointer(POS pos, DictionaryFileType fileType, long offset, String substring) throws IOException;

    /**
     * Return a randomly-chosen line pointer (offset of the beginning of a line).
     *
     * @param pos      part of speech
     * @param fileType file type
     * @return a randomly-chosen line pointer
     * @throws IOException IOException
     */
    long getRandomLinePointer(POS pos, DictionaryFileType fileType) throws IOException;

    /**
     * Return the first valid line pointer in the specified file.
     *
     * @param pos      part of speech
     * @param fileType file type
     * @return the first valid line pointer in the specified file
     * @throws IOException IOException
     */
    long getFirstLinePointer(POS pos, DictionaryFileType fileType) throws IOException;

    /**
     * Returns the number of times the sense marked by <var>senseKey</var> occurs in a semantic concordance.
     *
     * @param senseKey sense key
     * @return the number of times the sense marked by <var>senseKey</var> occurs in a semantic concordance
     * @throws IOException IOException
     */
    int getUseCount(String senseKey) throws IOException;

    /**
     * Shuts down the file manager.
     */
    void close();

    /**
     * Saves the files.
     *
     * @throws IOException   IOException
     * @throws JWNLException JWNLException
     */
    void save() throws IOException, JWNLException;

    /**
     * Deletes the dictionary.
     *
     * @throws IOException IOException
     */
    void delete() throws IOException;

    /**
     * Reopens files for writing.
     *
     * @throws IOException IOException
     */
    void edit() throws IOException;
}