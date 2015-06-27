package net.sf.extjwnl.dictionary.file;

import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.factory.Param;

import java.util.Map;

/**
 * Abstract implementation of <code>DictionaryFile</code>. This class
 * should be implemented for each file naming scheme used.
 * It is assumed that each file will be associated with both a POS and a file type.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class AbstractDictionaryFile implements DictionaryFile {

    protected final Dictionary dictionary;
    protected final Map<String, Param> params;
    protected final String path;
    protected final POS pos;

    /**
     * The type of the file: INDEX, DATA, and EXCEPTION.
     */
    protected final DictionaryFileType fileType;

    /**
     * Factory constructor.
     *
     * @param dictionary dictionary
     * @param params     params
     */
    public AbstractDictionaryFile(Dictionary dictionary, Map<String, Param> params) {
        this.dictionary = dictionary;
        this.params = params;
        this.path = null;
        this.pos = null;
        this.fileType = null;
    }

    /**
     * Instance constructor.
     *
     * @param dictionary dictionary
     * @param path       path
     * @param pos        part of speech
     * @param fileType   file type
     * @param params     parameters
     */
    protected AbstractDictionaryFile(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType, Map<String, Param> params) {
        this.dictionary = dictionary;
        this.params = params;
        this.pos = pos;
        this.fileType = fileType;
        this.path = path;
    }

    public POS getPOS() {
        return pos;
    }

    public DictionaryFileType getFileType() {
        return fileType;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        throw new UnsupportedOperationException();
    }
}