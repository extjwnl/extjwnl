package net.sf.extjwnl.dictionary.database;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.util.factory.Owned;

public interface DatabaseManager extends Owned {
    Query getIndexWordSynsetsQuery(POS pos, String lemma) throws JWNLException;

    Query getIndexWordLemmasQuery(POS pos) throws JWNLException;

    Query getIndexWordLemmasQuery(POS pos, String substring) throws JWNLException;

    Query getRandomIndexWordQuery(POS pos) throws JWNLException;

    Query getSynsetQuery(POS pos, long offset) throws JWNLException;

    Query getSynsetWordQuery(POS pos, long offset) throws JWNLException;

    Query getPointerQuery(POS pos, long offset) throws JWNLException;

    Query getVerbFrameQuery(POS pos, long offset) throws JWNLException;

    Query getSynsetsQuery(POS pos) throws JWNLException;

    Query getExceptionQuery(POS pos, String derivation) throws JWNLException;

    Query getExceptionsQuery(POS pos) throws JWNLException;
}