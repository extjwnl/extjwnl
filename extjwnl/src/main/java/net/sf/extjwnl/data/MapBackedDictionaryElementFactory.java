package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.factory.Param;

import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class MapBackedDictionaryElementFactory extends MapDictionaryElementFactory {

    public MapBackedDictionaryElementFactory(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary, params);
        maxOffset.clear();
    }

    @Override
    public Synset createSynset(POS pos) throws JWNLException {
        if (!maxOffset.containsKey(pos)) {
            // first time - update max offset
            // iteration might take time
            long maxOff = 0L;
            Iterator<Synset> si = dictionary.getSynsetIterator(pos);
            while (si.hasNext()) {
                Synset s = si.next();
                if (maxOff < s.getOffset()) {
                    maxOff = s.getOffset();
                }
            }
            maxOffset.put(pos, maxOff);
        }

        return super.createSynset(pos);
    }
}