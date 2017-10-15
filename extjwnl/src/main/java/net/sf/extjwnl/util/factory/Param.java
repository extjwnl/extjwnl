package net.sf.extjwnl.util.factory;

import net.sf.extjwnl.JWNLException;

/**
 * Represents a parameter in a properties file. Parameters can be nested.
 *
 * @author John Didion (jdidion@didion.net)
 */
public interface Param {

    String getName();

    String getValue();

    void addParam(Param param);

    Object create() throws JWNLException;
}