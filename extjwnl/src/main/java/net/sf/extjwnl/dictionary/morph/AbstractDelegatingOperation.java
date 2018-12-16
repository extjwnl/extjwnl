package net.sf.extjwnl.dictionary.morph;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.factory.Param;
import net.sf.extjwnl.util.factory.ParamList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for operations.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class AbstractDelegatingOperation extends AbstractOperation {

    private final Map<String, Operation[]> operationSets;
    protected final Map<String, Param> params;

    public AbstractDelegatingOperation(Dictionary dictionary, Map<String, Param> params) throws JWNLException {
        super(dictionary);
        this.params = params;
        operationSets = new HashMap<>();
        String[] keys = getKeys();
        for (String key : keys) {
            ParamList paramList = (ParamList) params.get(key);
            if (paramList != null) {
                @SuppressWarnings("unchecked")
                List<Operation> operations = (List<Operation>) paramList.create();
                Operation[] operationArray = operations.toArray(new Operation[0]);
                this.addDelegate(key, operationArray);
            }
        }
    }

    public void addDelegate(String key, Operation[] operations) {
        operationSets.put(key, operations);
    }

    protected abstract String[] getKeys();

    protected boolean hasDelegate(String key) {
        return operationSets.containsKey(key);
    }

    protected boolean delegate(POS pos, String lemma, BaseFormSet forms, String key) throws JWNLException {
        Operation[] operations = operationSets.get(key);
        boolean result = false;
        for (Operation operation : operations) {
            if (operation.execute(pos, lemma, forms)) {
                result = true;
            }
        }
        return result;
    }
}