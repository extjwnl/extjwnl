package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.dictionary.database.DatabaseManager;
import net.sf.extjwnl.dictionary.database.Query;
import net.sf.extjwnl.princeton.data.AbstractDictionaryElementFactory;
import net.sf.extjwnl.util.factory.Param;
import org.w3c.dom.Document;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Database-backed dictionary.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class DatabaseBackedDictionary extends AbstractCachingDictionary {

    /**
     * Database manager install parameter. The value should be the class of DatabaseManager to use.
     */
    public static final String DATABASE_MANAGER = "database_manager";

    private DatabaseDictionaryElementFactory factory;
    private DatabaseManager dbManager;

    public DatabaseBackedDictionary(Document doc) throws JWNLException {
        super(doc);

        this.factory = (DatabaseDictionaryElementFactory) elementFactory;

        if (!params.containsKey(DATABASE_MANAGER)) {
            throw new JWNLException(getMessages().resolveMessage("DICTIONARY_EXCEPTION_001", DATABASE_MANAGER));
        }
        Param param = params.get(DATABASE_MANAGER);
        this.dbManager = (param == null) ? null : (DatabaseManager) param.create();
    }

    public IndexWord getIndexWord(POS pos, String lemma) throws JWNLException {
        lemma = prepareQueryString(lemma);
        IndexWord word = null;
        if (lemma.length() > 0) {
            if (isCachingEnabled()) {
                word = getCachedIndexWord(pos, lemma);
            }
            if (word == null) {
                Query query = null;
                try {
                    query = dbManager.getIndexWordSynsetsQuery(pos, lemma);
                    word = factory.createIndexWord(pos, lemma, query.execute());
                    if (word != null && isCachingEnabled()) {
                        cacheIndexWord(word);
                    }
                } catch (SQLException e) {
                    throw new JWNLException(getMessages().resolveMessage("DICTIONARY_EXCEPTION_004", new Object[]{pos.getLabel(), lemma}), e);
                } finally {
                    if (query != null) {
                        query.close();
                    }
                }
            }
        }
        return word;
    }

    public Iterator<IndexWord> getIndexWordIterator(POS pos) throws JWNLException {
        Query query;
        try {
            query = dbManager.getIndexWordLemmasQuery(pos);
        } catch (SQLException e) {
            throw new JWNLException(getMessages().resolveMessage("DICTIONARY_EXCEPTION_064", pos.getLabel()), e);
        }
        return new IndexWordIterator(pos, query);
    }

    public Iterator<IndexWord> getIndexWordIterator(POS pos, String substring) throws JWNLException {
        Query query;
        try {
            query = dbManager.getIndexWordLemmasQuery(pos, substring);
        } catch (SQLException e) {
            throw new JWNLException(getMessages().resolveMessage("DICTIONARY_EXCEPTION_065", new Object[]{pos.getLabel(), substring}), e);
        }
        return new IndexWordIterator(pos, query);
    }

    public IndexWord getRandomIndexWord(POS pos) throws JWNLException {
        Query query = null;
        String lemma;

        try {
            query = dbManager.getRandomIndexWordQuery(pos);
            query.execute();
            query.getResults().next();
            lemma = query.getResults().getString(1);
        } catch (SQLException e) {
            throw new JWNLException(getMessages().resolveMessage("DICTIONARY_EXCEPTION_004", new Object[]{pos.getLabel(), "random"}), e);
        } finally {
            if (null != query) {
                query.close();
            }
        }

        return getIndexWord(pos, lemma);
    }

    public Synset getSynsetAt(POS pos, long offset) throws JWNLException {
        Synset synset = null;
        if (isCachingEnabled()) {
            synset = getCachedSynset(pos, offset);
        }
        if (synset == null) {
            Query query = null;
            Query wordQuery = null;
            Query pointerQuery = null;
            Query verbFrameQuery = null;
            try {
                query = dbManager.getSynsetQuery(pos, offset);
                wordQuery = dbManager.getSynsetWordQuery(pos, offset);
                pointerQuery = dbManager.getPointerQuery(pos, offset);
                verbFrameQuery = dbManager.getVerbFrameQuery(pos, offset);
                synset = factory.createSynset(pos, offset, query.execute(), wordQuery.execute(),
                        pointerQuery.execute(), POS.VERB == pos ? verbFrameQuery.execute() : null);
                if (synset != null && isCachingEnabled()) {
                    cacheSynset(synset);
                }
            } catch (SQLException e) {
                throw new JWNLException(getMessages().resolveMessage("DICTIONARY_EXCEPTION_005", new Object[]{pos.getLabel(), offset}), e);
            } finally {
                if (query != null) {
                    query.close();
                }
                if (wordQuery != null) {
                    wordQuery.close();
                }
                if (pointerQuery != null) {
                    pointerQuery.close();
                }
                if (verbFrameQuery != null) {
                    verbFrameQuery.close();
                }
            }
        }
        return synset;
    }

    public Iterator<Synset> getSynsetIterator(POS pos) throws JWNLException {
        Query query;
        try {
            query = dbManager.getSynsetsQuery(pos);
        } catch (SQLException e) {
            throw new JWNLException(getMessages().resolveMessage("DICTIONARY_EXCEPTION_066", pos.getLabel()), e);
        }
        return new SynsetIterator(pos, query);
    }

    public Exc getException(POS pos, String derivation) throws JWNLException {
        derivation = prepareQueryString(derivation);
        Exc exc = null;
        if (isCachingEnabled()) {
            exc = getCachedException(pos, derivation);
        }
        if (exc == null) {
            Query query = null;
            try {
                query = dbManager.getExceptionQuery(pos, derivation);
                exc = factory.createExc(pos, derivation, query.execute());
                if (exc != null && isCachingEnabled()) {
                    cacheException(exc);
                }
            } catch (SQLException e) {
                throw new JWNLException(getMessages().resolveMessage("DICTIONARY_EXCEPTION_006", new Object[]{pos.getLabel(), derivation}), e);
            } finally {
                if (query != null) {
                    query.close();
                }
            }
        }
        return exc;
    }

    public Iterator<Exc> getExceptionIterator(POS pos) throws JWNLException {
        Query query;
        try {
            query = dbManager.getExceptionsQuery(pos);
        } catch (SQLException e) {
            throw new JWNLException(getMessages().resolveMessage("DICTIONARY_EXCEPTION_067", pos.getLabel()), e);
        }
        return new ExceptionIterator(pos, query);
    }

    public synchronized void close() {
        dbManager.close();
    }

    @Override
    public synchronized void cacheAll() throws JWNLException {
        if (factory instanceof AbstractDictionaryElementFactory) {
            ((AbstractDictionaryElementFactory) factory).startCaching();
        }
        super.cacheAll();
        if (factory instanceof AbstractDictionaryElementFactory) {
            ((AbstractDictionaryElementFactory) factory).stopCaching();
        }
    }

    @Override
    public synchronized void edit() throws JWNLException {
        throw new UnsupportedOperationException();
    }

    private abstract class DatabaseElementIterator<E extends DictionaryElement> implements Iterator<E> {
        private final POS pos;
        private final Query lemmas;
        private boolean advanced = false;
        private boolean hasNext = false;

        protected DatabaseElementIterator(POS pos, Query query) {
            this.pos = pos;
            lemmas = query;
        }

        public boolean hasNext() {
            if (!advanced) {
                advanced = true;
                try {
                    hasNext = getResults().next();
                } catch (SQLException e) {
                    throw new JWNLRuntimeException(e);
                }
            }
            if (!hasNext) {
                lemmas.close();
            }
            return hasNext;
        }

        public E next() {
            if (hasNext()) {
                advanced = false;
                try {
                    return createElement();
                } catch (JWNLException | SQLException e) {
                    throw new JWNLRuntimeException(e);
                }
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        protected abstract E createElement() throws JWNLException, SQLException;

        protected POS getPOS() {
            return pos;
        }

        protected ResultSet getResults() throws SQLException {
            if (!lemmas.isExecuted()) {
                lemmas.execute();
            }
            return lemmas.getResults();
        }

        protected void finalize() throws Throwable {
            super.finalize();
            lemmas.close();
        }
    }

    private class IndexWordIterator extends DatabaseElementIterator<IndexWord> {
        public IndexWordIterator(POS pos, Query query) {
            super(pos, query);
        }

        protected IndexWord createElement() throws JWNLException, SQLException {
            String lemma = getResults().getString(1);
            return getIndexWord(getPOS(), lemma);
        }
    }

    private class SynsetIterator extends DatabaseElementIterator<Synset> {
        public SynsetIterator(POS pos, Query query) {
            super(pos, query);
        }

        protected Synset createElement() throws JWNLException, SQLException {
            long offset = getResults().getLong(1);
            return getSynsetAt(getPOS(), offset);
        }
    }

    private class ExceptionIterator extends DatabaseElementIterator<Exc> {
        public ExceptionIterator(POS pos, Query query) {
            super(pos, query);
        }

        protected Exc createElement() throws JWNLException, SQLException {
            String derivation = getResults().getString(1);
            return getException(getPOS(), derivation);
        }
    }
}