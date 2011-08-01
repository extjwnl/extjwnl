package net.sf.extjwnl.princeton.file;

import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.dictionary.file.RandomAccessDictionaryFile;
import net.sf.extjwnl.util.factory.Param;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Base class for random access files.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class AbstractPrincetonRandomAccessDictionaryFile extends AbstractPrincetonDictionaryFile
        implements RandomAccessDictionaryFile {

    /**
     * Dictionary file encoding. Use Java compatible encoding names. See {@link java.nio.charset.Charset}.
     */
    public static final String ENCODING = "encoding";

    /**
     * Used for caching the previously accessed file offset.
     */
    private long previousOffset;
    /**
     * Used for caching the offset of the line following the line at
     * <code>previousOffset</code>.
     */
    private long nextOffset;

    protected String encoding;

    protected AbstractPrincetonRandomAccessDictionaryFile(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary, params);
    }

    protected AbstractPrincetonRandomAccessDictionaryFile(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType, Map<String, Param> params) {
        super(dictionary, path, pos, fileType, params);
        Param param = params.get(ENCODING);
        if (null != param) {
            encoding = param.getValue();
        }
    }

    public void setNextLineOffset(long previousOffset, long nextOffset) {
        this.previousOffset = previousOffset;
        this.nextOffset = nextOffset;
    }

    public boolean isPreviousLineOffset(long offset) {
        return previousOffset == offset;
    }

    public long getNextLineOffset() {
        return nextOffset;
    }

    @Override
    public void edit() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeStrings(Collection<String> strings) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeLine(String line) throws IOException {
        throw new UnsupportedOperationException();
    }
}