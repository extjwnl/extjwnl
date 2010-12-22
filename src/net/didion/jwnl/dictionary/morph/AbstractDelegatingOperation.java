package net.didion.jwnl.dictionary.morph;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.util.factory.ParamList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractDelegatingOperation implements Operation {
    private Map _operationSets;

    public Object create(Map params) throws JWNLException {
        AbstractDelegatingOperation operation = getInstance(params);
        String[] keys = getKeys();
        for (String key : keys) {
            ParamList paramList = (ParamList) params.get(key);
            if (paramList != null) {
                List operations = (List) paramList.create();
                Operation[] operationArray = (Operation[]) operations.toArray(new Operation[operations.size()]);
                operation.addDelegate(key, operationArray);
            }
        }
        return operation;
    }

    public void addDelegate(String key, Operation[] operations) {
        _operationSets.put(key, operations);
    }

    protected AbstractDelegatingOperation() {
        _operationSets = new HashMap();
    }

    protected abstract String[] getKeys();

    protected abstract AbstractDelegatingOperation getInstance(Map params) throws JWNLException;

    protected boolean hasDelegate(String key) {
        return _operationSets.containsKey(key);
    }

    protected boolean delegate(POS pos, String lemma, BaseFormSet forms, String key) throws JWNLException {
        Operation[] operations = (Operation[]) _operationSets.get(key);
        boolean result = false;
        for (Operation operation : operations) {
            if (operation.execute(pos, lemma, forms)) {
                result = true;
            }
        }
        return result;
    }
}