package net.sf.extjwnl.utilities;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.DictionaryElement;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.AbstractCachingDictionary;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.file.DictionaryCatalog;
import net.sf.extjwnl.dictionary.file.DictionaryCatalogSet;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.dictionary.file.ObjectDictionaryFile;
import net.sf.extjwnl.princeton.file.PrincetonObjectDictionaryFile;
import net.sf.extjwnl.util.factory.NameValueParam;
import net.sf.extjwnl.util.factory.Param;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DictionaryToMap allows you to populate and create an in-memory map of the WordNet
 * library. The goal of this utility is to provide a performance boost to applications
 * using a high quantity of API calls to the extJWNL library
 * (such as word sense disambiguation algorithms or dictionary services).
 *
 * @author Brett Walenz (bwalenz@users.sourceforge.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class DictionaryToMap {

    private final Dictionary dictionary;
    private final DictionaryCatalogSet<ObjectDictionaryFile> destinationFiles;

    /**
     * Initialize with the given map destination directory, using the properties file(usually file_properties.xml)
     *
     * @param propFile             properties file
     * @param destinationDirectory destination directory for in-memory map files
     * @throws JWNLException JWNLException
     * @throws IOException   IOException
     */
    public DictionaryToMap(String propFile, String destinationDirectory) throws JWNLException, IOException {
        dictionary = Dictionary.getInstance(new FileInputStream(propFile));
        HashMap<String, Param> params = new HashMap<>();
        params.put(DictionaryCatalog.DICTIONARY_PATH_KEY, new NameValueParam(dictionary, DictionaryCatalog.DICTIONARY_PATH_KEY, destinationDirectory));
        params.put(DictionaryCatalog.DICTIONARY_FILE_TYPE_KEY, new NameValueParam(dictionary, DictionaryCatalog.DICTIONARY_FILE_TYPE_KEY, PrincetonObjectDictionaryFile.class.getCanonicalName()));
        destinationFiles = new DictionaryCatalogSet<>(dictionary, params, ObjectDictionaryFile.class);
    }

    /**
     * Converts the current Dictionary to a MapBackedDictionary.
     *
     * @throws JWNLException JWNLException
     * @throws IOException   IOException
     */
    public void convert() throws JWNLException, IOException {
        destinationFiles.open();
        destinationFiles.edit();
        boolean canClearCache = (dictionary instanceof AbstractCachingDictionary) && ((AbstractCachingDictionary) dictionary).isCachingEnabled();
        for (DictionaryFileType fileType : DictionaryFileType.getAllDictionaryFileTypes()) {
            for (POS pos : POS.getAllPOS()) {
                System.out.println("Converting " + pos.getLabel() + " " + fileType.getName() + "...");
                serialize(pos, fileType);
            }

            if (canClearCache) {
                ((AbstractCachingDictionary) dictionary).clearCache(fileType.getElementType());
            }
        }

        destinationFiles.close();
    }

    private Iterator<? extends DictionaryElement> getIterator(POS pos, DictionaryFileType fileType) throws JWNLException {
        if (fileType == DictionaryFileType.DATA) {
            return dictionary.getSynsetIterator(pos);
        }
        if (fileType == DictionaryFileType.INDEX) {
            return dictionary.getIndexWordIterator(pos);
        }
        if (fileType == DictionaryFileType.EXCEPTION) {
            return dictionary.getExceptionIterator(pos);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void serialize(POS pos, DictionaryFileType fileType) throws JWNLException {
        ObjectDictionaryFile file = destinationFiles.getDictionaryFile(pos, fileType);
        int count = 0;
        for (Iterator<? extends DictionaryElement> itr = getIterator(pos, fileType); itr.hasNext(); itr.next()) {
            if (++count % 10000 == 0) {
                System.out.println("Counted and cached element " + count + "...");
            }
        }

        Map<Object, DictionaryElement> map = new ConcurrentHashMap<>();
        Iterator<? extends DictionaryElement> listItr = getIterator(pos, fileType);
        while (listItr.hasNext()) {
            DictionaryElement elt = listItr.next();
            map.put(elt.getKey(), elt);
        }

        file.writeObject(map);
        file.close();
        System.gc();
        Runtime rt = Runtime.getRuntime();
        System.out.println("total mem: " + rt.totalMemory() / 1024L + "K free mem: " + rt.freeMemory() / 1024L + "K");
        System.out.println("Successfully serialized " + count + " elements...");
    }

    public static void main(String[] args) throws IOException, JWNLException {
        if (args.length == 2) {
            new DictionaryToMap(args[0], args[1]).convert();
        } else {
            System.out.println("Usage: DictionaryToMap <properties file> <destination directory>");
        }
    }
}