package net.sf.extjwnl.util.factory;

import net.sf.extjwnl.JWNLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * List of parameters.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class ParamList extends ArrayList<Param> implements Param {

    private final String name;

    public ParamList(String name) {
        this.name = name;
    }

    public ParamList(String name, List<Param> params) {
        this.name = name;
        addAll(params);
    }

    public ParamList(String name, Map<String, Param> params) {
        this.name = name;
        addAll(params.values());
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        throw new UnsupportedOperationException();
    }

    public void addParam(Param param) {
        add(param);
    }

    public List<Param> getParams() {
        return this;
    }

    public Object create() throws JWNLException {
        List<Param> params = getParams();
        List<Object> results = new ArrayList<>(params.size());
        for (Param param : params) {
            results.add(param.create());
        }
        return results;
    }
}