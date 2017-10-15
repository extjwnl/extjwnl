package net.sf.extjwnl.princeton.data;

import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.factory.Param;

import java.util.Map;

/**
 * Element factory for WN1.7.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class PrincetonWN17DatabaseDictionaryElementFactory extends AbstractPrincetonDatabaseDictionaryElementFactory {

    public PrincetonWN17DatabaseDictionaryElementFactory(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary, params);
    }
}
