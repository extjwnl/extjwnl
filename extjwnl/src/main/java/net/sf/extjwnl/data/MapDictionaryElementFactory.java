package net.sf.extjwnl.data;

import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.princeton.data.AbstractDictionaryElementFactory;
import net.sf.extjwnl.util.factory.Param;

import java.util.Map;

/**
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class MapDictionaryElementFactory extends AbstractDictionaryElementFactory {

    public MapDictionaryElementFactory(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary, params);
    }
}