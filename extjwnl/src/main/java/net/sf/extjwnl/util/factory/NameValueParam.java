package net.sf.extjwnl.util.factory;

import net.sf.extjwnl.dictionary.Dictionary;

import java.util.List;

/**
 * Param with name and value.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class NameValueParam extends AbstractValueParam {
    private final String name;
    private final String value;

    public NameValueParam(Dictionary dictionary, String name, String value) {
        super(dictionary);
        this.name = name;
        this.value = value;
    }

    public NameValueParam(Dictionary dictionary, String name, String value, List<Param> params) {
        super(dictionary, params);
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}