package net.didion.jwnl.dictionary.file;

import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;

/**
 * A dictionary file factory.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public interface DictionaryFileFactory<E> {

    /**
     * Creates a new instance of the dictionary file.
     */
    public E newInstance(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType);
}
