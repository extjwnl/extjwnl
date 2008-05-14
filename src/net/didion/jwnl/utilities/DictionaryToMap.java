
package net.didion.jwnl.utilities;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.DictionaryElement;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.AbstractCachingDictionary;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.dictionary.file.DictionaryCatalogSet;
import net.didion.jwnl.dictionary.file.DictionaryFileType;
import net.didion.jwnl.dictionary.file.ObjectDictionaryFile;


/**
 * DictionaryToMap allows you to populate and create an in-memory map of the WordNet 
 * library. The goal of this utility is to provide a performance boost to applications 
 * using a high quantity of API calls to the JWNL library 
 * (such as word sense disambiguation algorithms, or dictionary services). 
 * @author brett
 *
 */
public class DictionaryToMap
{

	/**
	 * Initalize with the given map destination directory, using the properties file(usually file_properties.xml)
	 * @param destDirectory - destination directory for in-memory map files
	 * @param propFile - properties file of file-based WordNet
	 * @throws JWNLException 
	 * @throws IOException
	 */
    public DictionaryToMap(String destDirectory, String propFile)
        throws JWNLException, IOException
    {
        JWNL.initialize(new FileInputStream(propFile));
        _destFiles = new DictionaryCatalogSet(destDirectory, net.didion.jwnl.princeton.file.PrincetonObjectDictionaryFile.class);
    }

    /**
     * Converts the current Dictionary to a MapBackedDictionary.
     * @throws JWNLException
     * @throws IOException
     */
    public void convert()
        throws JWNLException, IOException
    {
        _destFiles.open();
        boolean canClearCache = (Dictionary.getInstance() instanceof AbstractCachingDictionary) && ((AbstractCachingDictionary)Dictionary.getInstance()).isCachingEnabled();
        for(Iterator typeItr = DictionaryFileType.getAllDictionaryFileTypes().iterator(); typeItr.hasNext(); System.gc())
        {
            DictionaryFileType fileType = (DictionaryFileType)typeItr.next();
            POS pos;
            for(Iterator posItr = POS.getAllPOS().iterator(); posItr.hasNext(); serialize(pos, fileType))
            {
                pos = (POS)posItr.next();
                System.out.println("Converting " + pos + " " + fileType + " file...");
            }

            if(canClearCache)
                ((AbstractCachingDictionary)Dictionary.getInstance()).clearCache(fileType.getElementType());
        }

        _destFiles.close();
    }

    private Iterator getIterator(POS pos, DictionaryFileType fileType)
        throws JWNLException
    {
        if(fileType == DictionaryFileType.DATA)
            return Dictionary.getInstance().getSynsetIterator(pos);
        if(fileType == DictionaryFileType.INDEX)
            return Dictionary.getInstance().getIndexWordIterator(pos);
        if(fileType == DictionaryFileType.EXCEPTION)
            return Dictionary.getInstance().getExceptionIterator(pos);
        else
            throw new IllegalArgumentException();
    }

    private void serialize(POS pos, DictionaryFileType fileType)
        throws JWNLException, IOException
    {
        ObjectDictionaryFile file = (ObjectDictionaryFile)_destFiles.getDictionaryFile(pos, fileType);
        int count = 0;
        for(Iterator itr = getIterator(pos, fileType); itr.hasNext(); itr.next())
            if(++count % 10000 == 0)
                System.out.println("Counted and cached word " + count + "...");

        Map map = new HashMap((int)Math.ceil((float)count / 0.9F) + 1, 0.9F);
        DictionaryElement elt;
        for(Iterator listItr = getIterator(pos, fileType); listItr.hasNext(); map.put(elt.getKey(), elt))
            elt = (DictionaryElement)listItr.next();

        file.writeObject(map);
        file.close();
        map = null;
        file = null;
        System.gc();
        Runtime rt = Runtime.getRuntime();
        System.out.println("total mem: " + rt.totalMemory() / 1024L + "K free mem: " + rt.freeMemory() / 1024L + "K");
        System.out.println("Successfully serialized...");
    }

    public static void main(String args[])
    {
        String destinationDirectory = null;
        String propertyFile = null;
        if(args.length == 2)
        {
            destinationDirectory = args[0];
            propertyFile = args[1];
        } else
        {
            System.out.println("java DictionaryToMap <destination directory> <properties file>");
            System.exit(-1);
        }
        try
        {
            (new DictionaryToMap(destinationDirectory, propertyFile)).convert();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private DictionaryCatalogSet _destFiles;
  }
