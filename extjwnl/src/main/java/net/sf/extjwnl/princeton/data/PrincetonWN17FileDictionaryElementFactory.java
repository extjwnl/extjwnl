package net.sf.extjwnl.princeton.data;

import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.factory.Param;

import java.util.Map;

/**
 * <code>FileDictionaryElementFactory</code> that produces elements for the Princeton release of WordNet v 1.7
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class PrincetonWN17FileDictionaryElementFactory extends AbstractPrincetonFileDictionaryElementFactory {

    public PrincetonWN17FileDictionaryElementFactory(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary, params);
    }
}
