package net.didion.jwnl.util.factory;

import net.didion.jwnl.JWNLException;

import java.util.ArrayList;
import java.util.List;

public class ParamList implements Param {
    private String _name;
    private List<Param> _params = new ArrayList<Param>();

    public ParamList(String name) {
        _name = name;
    }

    public ParamList(String name, Param[] params) {
        _name = name;
        for (Param param : params) {
            addParam(param);
        }
    }

    public String getName() {
        return _name;
    }

    public String getValue() {
        throw new UnsupportedOperationException();
    }

    public void addParam(Param param) {
        _params.add(param);
    }

    public List<Param> getParams() {
        return _params;
    }

    public Object create() throws JWNLException {
        List<Param> params = getParams();
        List results = new ArrayList(params.size());
        for (Param param : params) {
            results.add(param.create());
        }
        return results;
    }
}