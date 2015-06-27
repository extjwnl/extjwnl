package net.sf.extjwnl.dictionary.file;

import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;

/**
 * A dictionary file factory.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public interface DictionaryFileFactory<E> {

    /**
     * Creates a new instance of dictionary file.
     *
     * @param dictionary owner dictionary
     * @param path       path to dictionary files
     * @param pos        part of speech
     * @param fileType   file type
     * @return a new instance of the dictionary file
     */
    E newInstance(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType);
}