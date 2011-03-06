package net.didion.jwnl.princeton.data;

import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.util.factory.Param;

import java.util.Map;

/**
 * <code>FileDictionaryElementFactory</code> that produces elements for the Princeton release of WordNet v 1.7
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class PrincetonWN17FileDictionaryElementFactory extends AbstractPrincetonFileDictionaryElementFactory {

    public PrincetonWN17FileDictionaryElementFactory(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary);
    }
}
