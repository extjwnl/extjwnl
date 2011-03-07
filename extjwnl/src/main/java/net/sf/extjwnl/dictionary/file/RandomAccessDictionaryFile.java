package net.sf.extjwnl.dictionary.file;

import java.io.IOException;

/**
 * <code>DictionaryFile</code> that reads lines from a random-access text file.
 *
 * @author John Didion <jdidion@users.sourceforge.net>
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public interface RandomAccessDictionaryFile extends DictionaryFile {

    /**
     * Reads a byte from the file.
     */
    public int read() throws IOException;

    /**
     * Reads a line from the file.
     */
    public String readLine() throws IOException;

    /**
     * Reads the first word from a file (ie offset, index word).
     *
     * @return the first word from a file (ie offset, index word)
     * @throws IOException IOException
     */
    public String readLineWord() throws IOException;

    /**
     * Goes to position <var>pos</var> in the file.
     */
    public void seek(long pos) throws IOException;

    /**
     * Returns the current position of the file pointer.
     */
    public long getFilePointer() throws IOException;

    /**
     * Returns the length, in bytes, of the file.
     */
    public long length() throws IOException;

    // Offset caching functions

    /**
     * Moves the file pointer so that its next line offset is <var>nextOffset</var>
     */
    public void setNextLineOffset(long previousOffset, long nextOffset);

    /**
     * Returns true if <var>offset</var> is the previous offset.
     */
    public boolean isPreviousLineOffset(long offset);

    /**
     * Returns the byte offset of the next line (after the position of the file pointer)
     */
    public long getNextLineOffset();
}
