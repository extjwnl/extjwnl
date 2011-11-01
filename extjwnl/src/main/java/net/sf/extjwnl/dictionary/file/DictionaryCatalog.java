package net.sf.extjwnl.dictionary.file;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.factory.Owned;
import net.sf.extjwnl.util.factory.Param;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A container for the files associated with a catalog (the index, data, and exception
 * files associated with a POS).
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class DictionaryCatalog<E extends DictionaryFile> implements Owned {

    /**
     * Dictionary path install parameter. The value should be the absolute path
     * of the directory containing the dictionary files.
     */
    public static final String DICTIONARY_PATH_KEY = "dictionary_path";

    /**
     * File type install parameter. The value should be the
     * name of the subclass of DictionaryFileType compatible with the catalog owner implementation.
     */
    public static final String DICTIONARY_FILE_TYPE_KEY = "file_type";

    private final Map<POS, E> files;
    private final DictionaryFileType fileType;
    private final Dictionary dictionary;

    public DictionaryCatalog(Dictionary dictionary, DictionaryFileType fileType, Class desiredDictionaryFileType, Map<String, Param> params) throws JWNLException {
        this.files = new EnumMap<POS, E>(POS.class);
        this.dictionary = dictionary;
        this.fileType = fileType;

        if (!params.containsKey(DICTIONARY_PATH_KEY)) {
            throw new JWNLException("DICTIONARY_EXCEPTION_052", DICTIONARY_PATH_KEY);
        }
        String path = params.get(DICTIONARY_PATH_KEY).getValue();

        if (!params.containsKey(DICTIONARY_FILE_TYPE_KEY)) {
            throw new JWNLException("DICTIONARY_EXCEPTION_052", DICTIONARY_FILE_TYPE_KEY);
        }

        try {
            Class fileClass;
            try {
                fileClass = Class.forName(params.get(DICTIONARY_FILE_TYPE_KEY).getValue());
                if (!desiredDictionaryFileType.isAssignableFrom(fileClass)) {
                    throw new JWNLRuntimeException("DICTIONARY_EXCEPTION_003", fileClass);
                }
            } catch (ClassNotFoundException ex) {
                throw new JWNLRuntimeException("DICTIONARY_EXCEPTION_002", ex);
            }

            @SuppressWarnings("unchecked")
            DictionaryFileFactory<E> factory = (DictionaryFileFactory<E>) params.get(DICTIONARY_FILE_TYPE_KEY).create();
            for (POS pos : POS.getAllPOS()) {
                E file = factory.newInstance(dictionary, path, pos, fileType);
                files.put(file.getPOS(), file);
            }
        } catch (Exception e) {
            throw new JWNLRuntimeException("DICTIONARY_EXCEPTION_018", fileType, e);
        }
    }

    public DictionaryFileType getKey() {
        return getFileType();
    }

    public void open() throws IOException {
        if (!isOpen()) {
            for (Iterator<E> itr = getFileIterator(); itr.hasNext();) {
                itr.next().open();
            }
        }
    }

    public void delete() throws IOException {
        for (Iterator<E> itr = getFileIterator(); itr.hasNext();) {
            itr.next().delete();
        }
    }

    public boolean isOpen() {
        for (Iterator<E> itr = getFileIterator(); itr.hasNext();) {
            if (!itr.next().isOpen()) {
                return false;
            }
        }
        return true;
    }

    public void close() {
        for (Iterator<E> itr = getFileIterator(); itr.hasNext();) {
            itr.next().close();
        }
    }

    public int size() {
        return files.size();
    }

    public Iterator<E> getFileIterator() {
        return files.values().iterator();
    }

    public E get(POS pos) {
        return files.get(pos);
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

    public void save() throws IOException, JWNLException {
        for (Iterator<E> itr = getFileIterator(); itr.hasNext();) {
            itr.next().save();
        }
    }

    public void edit() throws IOException {
        for (Iterator<E> itr = getFileIterator(); itr.hasNext();) {
            itr.next().edit();
        }
    }
}