package net.sf.extjwnl.util.factory;

import net.sf.extjwnl.dictionary.Dictionary;

import java.util.List;

/**
 * Param with only value.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class ValueParam extends AbstractValueParam {
    private final String value;

    public ValueParam(Dictionary dictionary, String value) {
        super(dictionary);
        this.value = value;
    }

    public ValueParam(Dictionary dictionary, String value, List<Param> params) {
        super(dictionary, params);
        this.value = value;
    }

    public String getName() {
        throw new UnsupportedOperationException();
    }

    public String getValue() {
        return value;
    }
}