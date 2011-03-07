package net.sf.extjwnl.dictionary.file;

import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;

/**
 * A dictionary file factory.
 *
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public interface DictionaryFileFactory<E> {

    /**
     * Creates a new instance of the dictionary file.
     */
    public E newInstance(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType);
}
