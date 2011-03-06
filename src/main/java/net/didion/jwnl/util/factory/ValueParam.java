package net.didion.jwnl.util.factory;

import net.didion.jwnl.dictionary.Dictionary;

import java.util.List;

/**
 * Param with only value.
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class ValueParam extends AbstractValueParam {
    private String value;

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