package net.sf.extjwnl.dictionary.file;

import net.sf.extjwnl.JWNLException;

import java.io.IOException;
import java.util.Collection;

/**
 * <code>DictionaryFile</code> that reads lines from a random-access text file.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface RandomAccessDictionaryFile extends DictionaryFile {

    /**
     * Reads a byte from the file.
     *
     * @return a byte from the file
     * @throws IOException IOException
     */
    int read() throws IOException;

    /**
     * Reads a line from the file.
     *
     * @return a line from the file
     * @throws IOException IOException
     */
    String readLine() throws IOException;

    /**
     * Writes a line to the file.
     *
     * @param line a line to write
     * @throws IOException IOException
     */
    void writeLine(String line) throws IOException;

    /**
     * Reads the first word from a file (ie offset, index word).
     *
     * @return the first word from a file (ie offset, index word)
     * @throws IOException IOException
     */
    String readLineWord() throws IOException;

    /**
     * Goes to position <var>pos</var> in the file.
     *
     * @param pos position <var>pos</var> in the file
     * @throws IOException IOException
     */
    void seek(long pos) throws IOException;

    /**
     * Returns the current position of the file pointer.
     *
     * @return the current position of the file pointer
     * @throws IOException IOException
     */
    long getFilePointer() throws IOException;

    /**
     * Returns the length, in bytes, of the file.
     *
     * @return the length, in bytes, of the file
     * @throws IOException IOException
     */
    long length() throws IOException;

    /**
     * Moves the file pointer so that its next line offset is <var>nextOffset</var>.
     *
     * @param previousOffset previous offset
     * @param nextOffset     next offset
     */
    void setNextLineOffset(long previousOffset, long nextOffset);

    /**
     * Returns true if <var>offset</var> is the previous offset.
     *
     * @param offset previous offset
     * @return true if <var>offset</var> is the previous offset
     */
    boolean isPreviousLineOffset(long offset);

    /**
     * Returns the byte offset of the next line (after the position of the file pointer).
     *
     * @return the byte offset of the next line
     */
    long getNextLineOffset();

    /**
     * Writes strings in file.
     *
     * @param strings strings to write
     * @throws IOException IOException
     */
    void writeStrings(Collection<String> strings) throws IOException;

    /**
     * Returns offset length that accommodates largest offset.
     *
     * @return offset length that accommodates largest offset
     * @throws java.io.IOException          IOException
     * @throws net.sf.extjwnl.JWNLException IOException
     */
    int getOffsetLength() throws JWNLException, IOException;

    /**
     * Sets offset length to be used while rendering the file.
     *
     * @param length offset length to be used while rendering the file
     * @throws java.io.IOException          IOException
     * @throws net.sf.extjwnl.JWNLException IOException
     */
    void setOffsetLength(int length) throws JWNLException, IOException;
}