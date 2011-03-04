package net.didion.jwnl.princeton.file;

import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.dictionary.file.DictionaryFileType;
import net.didion.jwnl.dictionary.file.RandomAccessDictionaryFile;
import net.didion.jwnl.util.factory.Param;

import java.util.Map;

/**
 * Base class for random access files.
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public abstract class AbstractPrincetonRandomAccessDictionaryFile extends AbstractPrincetonDictionaryFile
        implements RandomAccessDictionaryFile {

    /**
     * Encoding parameter name.
     */
    private static final String ENCODING = "encoding";

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
}
