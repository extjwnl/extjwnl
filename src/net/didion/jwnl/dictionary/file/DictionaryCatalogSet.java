/**
 * Java WordNet Library (JWNL)
 * See the documentation for copyright information.
 */
package net.didion.jwnl.dictionary.file;

import net.didion.jwnl.data.POS;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Simple container for <code>DictionaryCatalog</code>s that allows
 * a <code>DictionaryFile</code> to be retrieved by its <code>POS</code>
 * and <code>DictionaryFileType</code>.
 */
public class DictionaryCatalogSet {
	private Map _catalogs = new HashMap();

	/** Creates a catalog set of the specified type of file using files in the specified dictionary directory. */
	public DictionaryCatalogSet(String path, Class dictionaryFileType) {
		path = path.trim();
		for (Iterator itr = DictionaryFileType.getAllDictionaryFileTypes().iterator(); itr.hasNext();) {
			DictionaryCatalog cat = new DictionaryCatalog(path, (DictionaryFileType)itr.next(), dictionaryFileType);
			_catalogs.put(cat.getKey(), cat);
		}
	}

	public void open() throws IOException {
		if (!isOpen()) {
			for (Iterator itr = getCatalogIterator(); itr.hasNext();)
				((DictionaryCatalog)itr.next()).open();
		}
	}

	public boolean isOpen() {
		for (Iterator itr = getCatalogIterator(); itr.hasNext();)
			if (!((DictionaryCatalog)itr.next()).isOpen())
				return false;
		return true;
	}

	public void close() {
		for (Iterator itr = getCatalogIterator(); itr.hasNext();)
			((DictionaryCatalog)itr.next()).close();
	}

	public DictionaryCatalog get(DictionaryFileType fileType) {
		return (DictionaryCatalog)_catalogs.get(fileType);
	}

	public int size() {
		return _catalogs.size();
	}

	public Iterator getCatalogIterator() {
		return _catalogs.values().iterator();
	}

	public DictionaryFile getDictionaryFile(POS pos, DictionaryFileType fileType) {
		return get(fileType).get(pos);
	}
}