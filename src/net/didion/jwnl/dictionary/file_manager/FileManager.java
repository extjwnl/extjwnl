package net.didion.jwnl.dictionary.file_manager;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.file.DictionaryFileType;
import net.didion.jwnl.util.factory.Owned;

import java.io.IOException;

/**
 * <code>FileManager</code> defines the interface between the <code>FileBackedDictionary</code> and the file system.
 * Methods in this interface operate on and return offsets, which are indices into a dictionary file.
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface FileManager extends Owned {
    /**
     * Search for the line whose first word is <var>index</var> (that is, that begins with
     * <var>index</var> followed by a space or tab).
     *
     * @return The file offset of the start of the matching line, or <code>-1</code> if no such line exists.
     */
    long getIndexedLinePointer(POS pos, DictionaryFileType fileType, String index) throws IOException;

    /**
     * Read the line that begins at file offset <var>offset</var>.
     */
    String readLineAt(POS pos, DictionaryFileType fileType, long offset) throws IOException;

    /**
     * Search for the line following the line that begins at <var>offset</var>.
     *
     * @return The file offset of the start of the line, or <code>-1</code> if <var>offset</var>
     *         is the last line in the file.
     */
    long getNextLinePointer(POS pos, DictionaryFileType fileType, long offset) throws IOException;

    /**
     * Search for a line whose index word contains <var>substring</var>, starting at <var>offset</var>.
     *
     * @return The file offset of the start of the matching line, or <code>-1</code> if
     *         no such line exists.
     */
    long getMatchingLinePointer(POS pos, DictionaryFileType fileType, long offset, String substring) throws IOException;

    /**
     * Return a randomly-chosen line pointer (offset of the beginning of a line).
     *
     * @param pos
     * @param index
     * @return
     */
    long getRandomLinePointer(POS pos, DictionaryFileType index) throws IOException;

    /**
     * Return the first valid line pointer in the specified file.
     *
     * @param pos
     * @param fileType
     * @return
     * @throws IOException
     */
    long getFirstLinePointer(POS pos, DictionaryFileType fileType) throws IOException;


    /**
     * Shuts down the file manager.
     */
    void close();

    /**
     * Saves the files.
     */
    void save() throws IOException, JWNLException;

    /**
     * Deletes the dictionary.
     */
    void delete() throws IOException;

    /**
     * Reopens files for writing.
     */
    void edit() throws IOException;
}