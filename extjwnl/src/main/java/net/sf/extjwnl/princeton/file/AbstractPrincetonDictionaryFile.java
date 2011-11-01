package net.sf.extjwnl.princeton.file;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.file.AbstractDictionaryFile;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.util.factory.Param;

import java.util.EnumMap;
import java.util.Map;

/**
 * <code>AbstractDictionaryFile</code> that uses file names compatible with Princeton's distribution of WordNet.
 * The filenames associated are:
 * WINDOWS: <noun, verb, adj, adv>.<idx, dat, exc>
 * MAC, UNIX: <index, data>.<noun, verb, adj, adv>, <noun, verb, adj, adv>.exc
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
    private static final Map<DictionaryFileType, FileNames> fileTypeToFileNameMap;

    static {
        posToExtMap = new EnumMap<POS, String>(POS.class);
        posToExtMap.put(POS.NOUN, NOUN_EXT);
        posToExtMap.put(POS.VERB, VERB_EXT);
        posToExtMap.put(POS.ADJECTIVE, ADJECTIVE_EXT);
        posToExtMap.put(POS.ADVERB, ADVERB_EXT);

        fileTypeToFileNameMap = new EnumMap<DictionaryFileType, FileNames>(DictionaryFileType.class);
        fileTypeToFileNameMap.put(DictionaryFileType.INDEX, new FileNames("idx", "index"));
        fileTypeToFileNameMap.put(DictionaryFileType.DATA, new FileNames("dat", "data"));
        fileTypeToFileNameMap.put(DictionaryFileType.EXCEPTION, new FileNames("exc", "exc"));
    }

    protected AbstractPrincetonDictionaryFile(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary, params);
    }

    public AbstractPrincetonDictionaryFile(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType, Map<String, Param> params) {
        super(dictionary, path, pos, fileType, params);
    }

    protected String getFilename() {
        if (null != getPOS()) {
            String posString = getExtension(getPOS());
            if (getFileType() == DictionaryFileType.EXCEPTION || (JWNL.getOS().equals(JWNL.WINDOWS) && getDictionary().getVersion().getNumber() < 2.1)) {
                return makeWindowsFilename(posString, getFileNames(getFileType()).windowsFileTypeName);
            } else {
                return makeNonWindowsFilename(posString, getFileNames(getFileType()).nonWindowsFileTypeName);
            }
        } else {
            if (DictionaryFileType.REVCNTLIST.equals(getFileType())) {
                return "cntlist.rev";
            } else if (DictionaryFileType.CNTLIST.equals(getFileType())) {
                return "cntlist";
            } else if (DictionaryFileType.INDEX.equals(getFileType())) {
                if (JWNL.getOS().equals(JWNL.WINDOWS) && getDictionary().getVersion().getNumber() < 2.1) {
                    return makeWindowsFilename("sense", getFileNames(getFileType()).windowsFileTypeName);
                } else {
                    return makeNonWindowsFilename("sense", getFileNames(getFileType()).nonWindowsFileTypeName);
                }
            } else {
                throw new IllegalArgumentException(JWNL.resolveMessage("DICTIONARY_EXCEPTION_054", new Object[]{getPOS(), getFileType()}));
            }
        }
    }

    /**
     * Makes a windows file type string. Typically of the form "data.noun" or "index.noun".
     *
     * @param posStr      the part of speech
     * @param fileTypeStr the file type, data, index, etc.
     * @return Windows file type string.
     */
    private String makeWindowsFilename(String posStr, String fileTypeStr) {
        return posStr + "." + fileTypeStr;
    }

    private String makeNonWindowsFilename(String posStr, String fileTypeStr) {
        return fileTypeStr + "." + posStr;
    }

    private String getExtension(POS pos) {
        return posToExtMap.get(pos);
    }

    private FileNames getFileNames(DictionaryFileType type) {
        return fileTypeToFileNameMap.get(type);
    }

    private static final class FileNames {
        private final String windowsFileTypeName;
        private final String nonWindowsFileTypeName;

        public FileNames(String windowsFileTypeName, String nonWindowsFileTypeName) {
            this.windowsFileTypeName = windowsFileTypeName;
            this.nonWindowsFileTypeName = nonWindowsFileTypeName;
        }
    }
}