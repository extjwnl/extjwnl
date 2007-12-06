package net.didion.jwnl.princeton.data;

import net.didion.jwnl.JWNLException;

import java.util.Map;

public class PrincetonWN17DatabaseDictionaryElementFactory extends AbstractPrincetonDatabaseDictionaryElementFactory {
	public Object create(Map params) throws JWNLException {
		return new PrincetonWN17DatabaseDictionaryElementFactory();
	}
}
