package net.sf.extjwnl.util.factory;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.morph.Util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for configuration parameters.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class AbstractValueParam implements Param {

    protected final Dictionary dictionary;
    private final Map<String, Param> paramMap = new HashMap<>();

    protected AbstractValueParam(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    protected AbstractValueParam(Dictionary dictionary, List<Param> params) {
        this(dictionary);
        for (Param param : params) {
            addParam(param);
        }
    }

    public void addParam(Param param) {
        paramMap.put(param.getName(), param);
    }

    /**
     * If the value of this parameter is a class name, and that class is creatable, this method will create
     * an instance of it using this Param parameters.
     */
    @SuppressWarnings("unchecked")
    public Object create() throws JWNLException {
        try {
            Class clazz = Class.forName(getValue());
            Constructor c = clazz.getConstructor(Dictionary.class, Map.class);
            return c.newInstance(dictionary, paramMap);
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            throw new JWNLException(dictionary.getMessages().resolveMessage("DICTIONARY_UNABLE_TO_CREATE_INSTANCE", new Object[]{getValue(), Util.getRootCause(e)}), e);
        }
    }
}
