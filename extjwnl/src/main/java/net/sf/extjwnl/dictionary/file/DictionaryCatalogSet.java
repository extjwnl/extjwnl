package net.sf.extjwnl.dictionary.file;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.factory.Owned;
import net.sf.extjwnl.util.factory.Param;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Simple container for <code>DictionaryCatalog</code>s that allows
 * a <code>DictionaryFile</code> to be retrieved by its <code>POS</code>
 * and <code>DictionaryFileType</code>.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class DictionaryCatalogSet<E extends DictionaryFile> implements Owned {

    private final Map<DictionaryFileType, DictionaryCatalog<E>> catalogs;
    private final Dictionary dictionary;

    /**
     * Creates a catalog set of the specified type of file using files in the specified dictionary directory.
     *
     * @param dictionary                dictionary
     * @param params                    parameters
     * @param desiredDictionaryFileType desiredDictionaryFileType
     * @throws JWNLException JWNLException
     */
    public DictionaryCatalogSet(Dictionary dictionary, Map<String, Param> params, Class desiredDictionaryFileType) throws JWNLException {
        this.dictionary = dictionary;
        this.catalogs = new EnumMap<>(DictionaryFileType.class);
        for (DictionaryFileType d : DictionaryFileType.getAllDictionaryFileTypes()) {
            DictionaryCatalog<E> cat = new DictionaryCatalog<>(dictionary, d, desiredDictionaryFileType, params);
            catalogs.put(cat.getKey(), cat);
        }
    }

    public void open() throws JWNLException {
        if (!isOpen()) {
            for (Iterator<DictionaryCatalog<E>> itr = getCatalogIterator(); itr.hasNext(); ) {
                itr.next().open();
            }
        }
    }

    public boolean delete() throws JWNLException {
        boolean result = true;
        for (Iterator<DictionaryCatalog<E>> itr = getCatalogIterator(); itr.hasNext(); ) {
            final DictionaryCatalog<E> catalog = itr.next();
            result = result && catalog.delete();
        }
        return result;
    }

    public boolean isOpen() {
        for (Iterator<DictionaryCatalog<E>> itr = getCatalogIterator(); itr.hasNext(); ) {
            if (!itr.next().isOpen()) {
                return false;
            }
        }
        return true;
    }

    public void close() throws JWNLException {
        for (Iterator<DictionaryCatalog<E>> itr = getCatalogIterator(); itr.hasNext(); ) {
            itr.next().close();
        }
    }

    public DictionaryCatalog<E> get(DictionaryFileType fileType) {
        return catalogs.get(fileType);
    }

    public int size() {
        return catalogs.size();
    }

    public Iterator<DictionaryCatalog<E>> getCatalogIterator() {
        return catalogs.values().iterator();
    }

    public E getDictionaryFile(POS pos, DictionaryFileType fileType) {
        final DictionaryCatalog<E> catalog = get(fileType);
        if (null != catalog) {
            return catalog.get(pos);
        } else {
            return null;
        }
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        throw new UnsupportedOperationException();
    }

    public void save() throws JWNLException {
        for (Iterator<DictionaryCatalog<E>> itr = getCatalogIterator(); itr.hasNext(); ) {
            itr.next().save();
        }
    }

    public void edit() throws JWNLException {
        for (Iterator<DictionaryCatalog<E>> itr = getCatalogIterator(); itr.hasNext(); ) {
            itr.next().edit();
        }
    }
}