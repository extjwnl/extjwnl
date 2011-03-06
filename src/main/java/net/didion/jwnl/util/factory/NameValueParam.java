package net.didion.jwnl.util.factory;

import net.didion.jwnl.dictionary.Dictionary;

import java.util.List;

/**
 * Param with name and value.
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class NameValueParam extends AbstractValueParam {
    private String name;
    private String value;

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