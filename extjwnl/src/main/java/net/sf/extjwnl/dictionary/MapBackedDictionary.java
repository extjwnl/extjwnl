package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.DictionaryElement;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.file.DictionaryCatalog;
import net.sf.extjwnl.dictionary.file.DictionaryCatalogSet;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.dictionary.file.ObjectDictionaryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.util.Map;

/**
 * A <code>Dictionary</code> backed by <code>Map</code>s. Warning: this has huge memory requirements.
 * Make sure to start JVM with a large enough free memory pool to accommodate this.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class MapBackedDictionary extends MapDictionary {

    private static final Logger log = LoggerFactory.getLogger(MapBackedDictionary.class);

    private final DictionaryCatalogSet<ObjectDictionaryFile> files;

    public MapBackedDictionary(Document doc) throws JWNLException {
        super(doc);
        files = new DictionaryCatalogSet<>(this, params, ObjectDictionaryFile.class);
        this.load();
    }

    @Override
    public synchronized boolean delete() throws JWNLException {
        return files.delete();
    }

    @Override
    public synchronized void close() throws JWNLException {
        files.close();
        super.close();
    }

    @Override
    public synchronized void edit() throws JWNLException {
        if (!isEditable()) {
            super.edit();
            files.edit();
        }
    }

    @Override
    public synchronized void save() throws JWNLException {
        super.save();
        files.save();
    }

    private void load() throws JWNLException {
        // because restore variable is static
        synchronized (Dictionary.class) {
            Dictionary.setRestoreDictionary(this);
            try {
                if (!files.isOpen()) {
                    files.open();
                }
                // load all the hash tables into memory
                if (log.isDebugEnabled()) {
                    log.debug(getMessages().resolveMessage("DICTIONARY_INFO_009"));
                }
                if (log.isTraceEnabled()) {
                    log.trace(getMessages().resolveMessage("DICTIONARY_INFO_010", Runtime.getRuntime().freeMemory()));
                }

                for (DictionaryFileType fileType : DictionaryFileType.getAllDictionaryFileTypes()) {
                    DictionaryCatalog<ObjectDictionaryFile> catalog = files.get(fileType);
                    for (POS pos : POS.getAllPOS()) {
                        if (log.isDebugEnabled()) {
                            log.debug(getMessages().resolveMessage("DICTIONARY_INFO_011", new Object[]{pos.getLabel(), fileType.getName()}));
                        }
                        putTable(pos, fileType, loadDictFile(catalog.get(pos)));
                        if (log.isTraceEnabled()) {
                            log.trace(getMessages().resolveMessage("DICTIONARY_INFO_012", Runtime.getRuntime().freeMemory()));
                        }
                    }
                }
                files.close();
            } finally {
                Dictionary.setRestoreDictionary(null);
            }
        }
    }

    private Map<Object, DictionaryElement> loadDictFile(ObjectDictionaryFile file) throws JWNLException {
        try {
            @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
            Map<Object, DictionaryElement> result = (Map<Object, DictionaryElement>) file.readObject();
            return result;
        } catch (Exception e) {
            throw new JWNLException(getMessages().resolveMessage("DICTIONARY_EXCEPTION_020", file.getFilename()), e);
        }
    }

    /**
     * Use <var>table</var> for lookups to the file represented by <var>pos</var> and
     * <var>fileType</var>.
     *
     * @param pos      POS
     * @param fileType element type
     * @param table    hashmap with elements
     */
    private void putTable(POS pos, DictionaryFileType fileType, Map<Object, DictionaryElement> table) {
        tableMap.get(pos).put(fileType, table);
    }
}