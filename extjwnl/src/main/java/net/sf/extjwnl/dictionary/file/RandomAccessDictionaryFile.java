package net.sf.extjwnl.dictionary.file;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.util.PointedCharSequence;

/**
 * <code>DictionaryFile</code> that reads lines from a random-access text file.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface RandomAccessDictionaryFile extends DictionaryFile {

    /**
     * Reads starting at <var>offset</var> and till the end of line.
     *
     * @param offset offset
     * @return a line from the file
     * @throws JWNLException JWNLException
     */
    PointedCharSequence readLine(long offset) throws JWNLException;

    /**
     * Reads starting at <var>offset</var> and till space.
     *
     * @param offset offset
     * @return one word from the file (e.g. offset, index word)
     * @throws JWNLException JWNLException
     */
    PointedCharSequence readWord(long offset) throws JWNLException;

    /**
     * Returns offset of the first line.
     *
     * @return offset of the first line
     * @throws JWNLException JWNLException
     */
    long getFirstLineOffset() throws JWNLException;

    /**
     * Returns start of the next line or -1 if no new line.
     *
     * @param offset starting offset
     * @return start of the next line or -1 if no new line.
     * @throws JWNLException JWNLException
     */
    long getNextLineOffset(long offset) throws JWNLException;

    /**
     * Returns the length, in bytes, of the file.
     *
     * @return the length, in bytes, of the file
     * @throws JWNLException JWNLException
     */
    long length() throws JWNLException;

    /**
     * Returns offset length that accommodates largest offset.
     *
     * @return offset length that accommodates largest offset
     * @throws JWNLException JWNLException
     */
    int getOffsetLength() throws JWNLException;

    /**
     * Sets offset length to be used while rendering the file.
     *
     * @param length offset length to be used while rendering the file
     * @throws JWNLException JWNLException
     */
    void setOffsetLength(int length) throws JWNLException;
}