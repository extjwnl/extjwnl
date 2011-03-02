package net.didion.jwnl.dictionary.file;

import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.util.factory.Param;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Abstract implementation of <code>DictionaryFile</code>. This class
 * should be implemented for each file naming scheme used. It is assumed that each
 * file will be associated with both a POS and a file type (e.g. in the windows
 * naming scheme, the verb index file is called "verb.idx").
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public abstract class AbstractDictionaryFile implements DictionaryFile {

    protected Dictionary dictionary;
    protected Map<String, Param> params;
    private POS pos;

    /**
     * The type of the file. For example, the default implementation defines the types INDEX, DATA, and EXCEPTION.
     */
    private DictionaryFileType fileType;
    private File file;

    public AbstractDictionaryFile() {
    }

    public AbstractDictionaryFile(Dictionary dictionary, Map<String, Param> params) {
        this.dictionary = dictionary;
        this.params = params;
    }

    protected AbstractDictionaryFile(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType, Map<String, Param> params) {
        this(dictionary, params);
        this.pos = pos;
        this.fileType = fileType;
        file = new File(path, makeFilename());
    }

    /**
     * Returns a filename from the part-of-speech and the file type.
     *
     * @return a filename from the part-of-speech and the file type
     */
    protected abstract String makeFilename();

    /**
     * Opens the <var>file</var>.
     *
     * @param file the file to open
     * @throws IOException IOException
     */
    protected abstract void openFile(File file) throws IOException;

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
        if (!isOpen()) {
            openFile(file);
        }
    }

    public boolean delete() throws IOException {
        close();
        return file.delete();
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }
}