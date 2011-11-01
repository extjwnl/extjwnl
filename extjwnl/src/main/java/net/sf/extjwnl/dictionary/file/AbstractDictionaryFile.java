package net.sf.extjwnl.dictionary.file;

import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.factory.Param;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Abstract implementation of <code>DictionaryFile</code>. This class
 * should be implemented for each file naming scheme used. It is assumed that each
 * file will be associated with both a POS and a file type (e.g. in the windows
 * naming scheme, the verb index file is called "verb.idx").
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class AbstractDictionaryFile implements DictionaryFile {

    protected final Dictionary dictionary;
    protected final Map<String, Param> params;
    protected File file;
    protected POS pos;

    /**
     * The type of the file. For example, the default implementation defines the types INDEX, DATA, and EXCEPTION.
     */
    private DictionaryFileType fileType;


    public AbstractDictionaryFile(Dictionary dictionary, Map<String, Param> params) {
        this.dictionary = dictionary;
        this.params = params;
    }

    protected AbstractDictionaryFile(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType, Map<String, Param> params) {
        this(dictionary, params);
        this.pos = pos;
        this.fileType = fileType;
        file = new File(path, getFilename());
    }

    /**
     * Returns a filename from the part-of-speech and the file type.
     *
     * @return a filename from the part-of-speech and the file type
     */
    protected abstract String getFilename();

    /**
     * Opens the <var>file</var>.
     *
     * @throws IOException IOException
     */
    protected abstract void openFile() throws IOException;

    /**
     * The POS associated with this file.
     */
    public POS getPOS() {
        return pos;
    }

    public File getFile() {
        return file;
    }

    /**
     * The file type associated with this file.
     */
    public DictionaryFileType getFileType() {
        return fileType;
    }

    /**
     * Opens the file.
     */
    public void open() throws IOException {
        synchronized (file) {
            if (!isOpen()) {
                openFile();
            }
        }
    }

    public void close() {
    }

    public boolean delete() throws IOException {
        close();
        return file.delete();
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        throw new UnsupportedOperationException();
    }
}