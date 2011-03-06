package net.didion.jwnl.princeton.data;

import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.util.factory.Param;

import java.util.Map;

/**
 * Element factory for WN1.7.
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class PrincetonWN17DatabaseDictionaryElementFactory extends AbstractPrincetonDatabaseDictionaryElementFactory {

    public PrincetonWN17DatabaseDictionaryElementFactory(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary);
    }
}
