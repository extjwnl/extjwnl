package net.sf.extjwnl.princeton.file;

import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.dictionary.file.RandomAccessDictionaryFile;
import net.sf.extjwnl.util.factory.Param;

import java.util.Map;

/**
 * Base class for text files.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class AbstractPrincetonRandomAccessDictionaryFile extends AbstractPrincetonDictionaryFile
        implements RandomAccessDictionaryFile {

    /**
     * Dictionary file encoding. Use Java-compatible encoding names. See {@link java.nio.charset.Charset}.
     */
    public static final String ENCODING_KEY = "encoding";

    protected final String encoding;

    /**
     * Factory constructor.
     *
     * @param dictionary dictionary
     * @param params     params
     */
    protected AbstractPrincetonRandomAccessDictionaryFile(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary, params);
        this.encoding = null;
    }

    /**
     * Instance constructor.
     *
     * @param dictionary dictionary
     * @param path       file path
     * @param pos        part of speech
     * @param fileType   file type
     * @param params     params
     */
    protected AbstractPrincetonRandomAccessDictionaryFile(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType, Map<String, Param> params) {
        super(dictionary, path, pos, fileType, params);
        Param param = params.get(ENCODING_KEY);
        if (null != param) {
            encoding = param.getValue();
        } else {
            encoding = null;
        }
    }
}