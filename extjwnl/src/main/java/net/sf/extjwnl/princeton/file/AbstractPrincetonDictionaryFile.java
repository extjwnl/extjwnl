package net.sf.extjwnl.princeton.file;

import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.file.AbstractDictionaryFile;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.util.factory.Param;

import java.util.EnumMap;
import java.util.Map;

/**
 * <code>AbstractDictionaryFile</code> that uses file names compatible with Princeton's distribution of WordNet.
 * The filenames associated are: <index, data>.<noun, verb, adj, adv>, <noun, verb, adj, adv>.exc
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class AbstractPrincetonDictionaryFile extends AbstractDictionaryFile {
    private static final String NOUN_EXT = "noun";
    private static final String VERB_EXT = "verb";
    private static final String ADJECTIVE_EXT = "adj";
    private static final String ADVERB_EXT = "adv";

    private static final Map<POS, String> posToExtMap;
    private static final Map<DictionaryFileType, String> fileTypeToFileNameMap;

    static {
        posToExtMap = new EnumMap<POS, String>(POS.class);
        posToExtMap.put(POS.NOUN, NOUN_EXT);
        posToExtMap.put(POS.VERB, VERB_EXT);
        posToExtMap.put(POS.ADJECTIVE, ADJECTIVE_EXT);
        posToExtMap.put(POS.ADVERB, ADVERB_EXT);

        fileTypeToFileNameMap = new EnumMap<DictionaryFileType, String>(DictionaryFileType.class);
        fileTypeToFileNameMap.put(DictionaryFileType.INDEX, "index");
        fileTypeToFileNameMap.put(DictionaryFileType.DATA, "data");
        fileTypeToFileNameMap.put(DictionaryFileType.EXCEPTION, "exc");
    }

    protected AbstractPrincetonDictionaryFile(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary, params);
    }

    public AbstractPrincetonDictionaryFile(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType, Map<String, Param> params) {
        super(dictionary, path, pos, fileType, params);
    }

    protected String getFilename() {
        if (null != getPOS()) {
            String posString = posToExtMap.get(getPOS());
            if (getFileType() == DictionaryFileType.EXCEPTION) {
                return posString + "." + fileTypeToFileNameMap.get(getFileType());
            } else {
                return fileTypeToFileNameMap.get(getFileType()) + "." + posString;
            }
        } else {
            if (DictionaryFileType.REVCNTLIST.equals(getFileType())) {
                return "cntlist.rev";
            } else if (DictionaryFileType.CNTLIST.equals(getFileType())) {
                return "cntlist";
            } else if (DictionaryFileType.INDEX.equals(getFileType())) {
                return fileTypeToFileNameMap.get(getFileType()) + ".sense";
            } else {
                throw new IllegalArgumentException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_054", new Object[]{getPOS(), getFileType()}));
            }
        }
    }
}