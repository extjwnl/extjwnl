/**
 * Java WordNet Library (JWNL)
 * See the documentation for copyright information.
 */
package net.didion.jwnl.util.factory;

import net.didion.jwnl.JWNLException;

import java.util.Map;

/**
 * A <code>Createable</code> is an object that can create an instance of itself given
 * parameters from a properties file (<code>Param</code>s). A class that
 * implements this interface must also define a no-arg constructor.
 */
public interface Createable {
	public Object create(Map params) throws JWNLException;
}
