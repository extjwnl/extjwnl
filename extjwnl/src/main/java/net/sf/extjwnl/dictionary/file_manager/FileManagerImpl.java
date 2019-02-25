package net.sf.extjwnl.dictionary.file_manager;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.file.*;
import net.sf.extjwnl.util.CharSequenceParser;
import net.sf.extjwnl.util.PointedCharSequence;
import net.sf.extjwnl.util.factory.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * An implementation of <code>FileManager</code> that reads files from the local file system.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class FileManagerImpl implements FileManager {

    private static final Logger log = LoggerFactory.getLogger(FileManagerImpl.class);

    private Dictionary dictionary;

    /**
     * Whether to check dictionary path existence.
     */
    public static final String CHECK_PATH_KEY = "check_path";

	/**
     * The catalog set.
     */
    private final DictionaryCatalogSet<RandomAccessDictionaryFile> files;

    /**
     * Whether to cache use counts, default false. Setting this parameter to <code>true</code> speeds up elements
     * loading from files considerably, at the expense of some amount of memory.
     */
    public static final String CACHE_USE_COUNT_KEY = "cache_use_count";
    private boolean cacheUseCount = false;

    private final Map<String, Integer> useCountCache = new HashMap<>();

    private RandomAccessDictionaryFile revCntList;
    private RandomAccessDictionaryFile cntList;
    private RandomAccessDictionaryFile senseIndex;

    public FileManagerImpl(Dictionary dictionary, Map<String, Param> params) throws JWNLException {
        try {
            if (!params.containsKey(DictionaryCatalog.DICTIONARY_PATH_KEY)) {
                throw new JWNLException(dictionary.getMessages().resolveMessage("JWNL_EXCEPTION_004", DictionaryCatalog.DICTIONARY_PATH_KEY));
            }

            boolean checkPath = true;
            if (params.containsKey(CHECK_PATH_KEY)) {
                checkPath = Boolean.parseBoolean(params.get(CHECK_PATH_KEY).getValue());
            }

            String path = params.get(DictionaryCatalog.DICTIONARY_PATH_KEY).getValue();

            if (checkPath) {
                File dictionaryPath = new File(path);
                if (!dictionaryPath.exists()) {
                    throw new JWNLException(dictionary.getMessages().resolveMessage("JWNL_EXCEPTION_009", path));
                }
            }

            this.dictionary = dictionary;
            files = new DictionaryCatalogSet<>(dictionary, params, RandomAccessDictionaryFile.class);
            files.open();

            try {
                Class fileClass = Class.forName(params.get(DictionaryCatalog.DICTIONARY_FILE_TYPE_KEY).getValue());
                if (!RandomAccessDictionaryFile.class.isAssignableFrom(fileClass)) {
                    throw new JWNLException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_003",
                            new Object[]{fileClass, RandomAccessDictionaryFile.class.getCanonicalName()}));
                }
            } catch (ClassNotFoundException e) {
                throw new JWNLException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_002"), e);
            }

            @SuppressWarnings("unchecked")
            DictionaryFileFactory<RandomAccessDictionaryFile> factory =
                    (DictionaryFileFactory<RandomAccessDictionaryFile>) params.get(DictionaryCatalog.DICTIONARY_FILE_TYPE_KEY).create();

            revCntList = factory.newInstance(dictionary, path, null, DictionaryFileType.REVCNTLIST);
            revCntList.open();

            cntList = factory.newInstance(dictionary, path, null, DictionaryFileType.CNTLIST);
            cntList.open();

            senseIndex = factory.newInstance(dictionary, path, null, DictionaryFileType.SENSEINDEX);
            senseIndex.open();

            if (params.containsKey(CACHE_USE_COUNT_KEY)) {
                cacheUseCount = Boolean.parseBoolean(params.get(CACHE_USE_COUNT_KEY).getValue());
            }

            if (cacheUseCount) {
                cacheUseCounts();
            }
        } catch (JWNLException e) {
            throw new JWNLException(dictionary.getMessages().resolveMessage("DICTIONARY_EXCEPTION_016"), e);
        }
    }

    @Override
    public void close() throws JWNLException {
        files.close();
        cntList.close();
        revCntList.close();
        senseIndex.close();
    }

    @Override
    public boolean delete() throws JWNLException {
        boolean result = files.delete();
        if (cntList instanceof DictionaryDiskFile) {
            result = result && ((DictionaryDiskFile) cntList).delete();
        }
        if (revCntList instanceof DictionaryDiskFile) {
            result = result && ((DictionaryDiskFile) revCntList).delete();
        }
        if (senseIndex instanceof DictionaryDiskFile) {
            result = result && ((DictionaryDiskFile) senseIndex).delete();
        }
        return result;
    }

    @Override
    public void edit() throws JWNLException {
        files.edit();
        revCntList.edit();
        cntList.edit();
        senseIndex.edit();
    }

    @Override
    public PointedCharSequence readLineAt(POS pos, DictionaryFileType fileType, long offset) throws JWNLException {
        RandomAccessDictionaryFile file = getFile(pos, fileType);
        if (null == file || file.length() == 0) {
            return null;
        }
        return file.readLine(offset);
    }

    @Override
    public PointedCharSequence getMatchingLine(POS pos, DictionaryFileType fileType, long offset, String substring)
            throws JWNLException {
        RandomAccessDictionaryFile file = getFile(pos, fileType);
        if (null == file || file.length() == 0) {
            return null;
        }

        PointedCharSequence word = file.readWord(offset);

        while (null != word && -1 == word.indexOf(substring)) {
            offset = file.getNextLineOffset(word.getLastBytePosition() + 1);
            word = file.readWord(offset);
        }

        if (null == word) {
            return null;
        } else {
            return file.readLine(offset);
        }
    }

    @Override
    public PointedCharSequence getIndexedLine(POS pos, DictionaryFileType fileType, String index) throws JWNLException {
        RandomAccessDictionaryFile file = getFile(pos, fileType);
        if (null == file || file.length() == 0) {
            return null;
        }
        PointedCharSequence result = null;
        long start = file.getFirstLineOffset();
        long stop = file.length() - 1;
        long offset; // current offset within the file, midpoint of binary search
        int compare;
        PointedCharSequence word; // current word
        out:
        while (start < stop) {
            offset = start + (stop - start) / 2;
            offset = file.getNextLineOffset(offset);
            if (stop == offset || -1 == offset) {
                // we hit somewhere in the line ending at stop or eol,
                // e.g. can't advance midpoint enough to catch a line
                // search sequentially from start remaining lines
                offset = start;
                while (stop != offset && -1 != offset) {
                    word = file.readWord(offset);
                    if (0 == word.compareTo(index)) {
                        result = file.readLine(offset);
                        break out;
                    } else {
                        offset = file.getNextLineOffset(offset);
                    }
                }
                break;
            }
            word = file.readWord(offset);
            compare = word.compareTo(index);
            if (compare == 0) {
                result = file.readLine(offset);
                break;
            } else if (compare > 0) {
                stop = offset;
            } else {
                start = offset;
            }
        }
        return result;
    }

    public PointedCharSequence getRandomLine(POS pos, DictionaryFileType fileType) throws JWNLException {
        // favors successors of long lines...
        final RandomAccessDictionaryFile file = getFile(pos, fileType);
        long start = file.getFirstLineOffset();
        int range = ((int) file.length()) - (int) start;
        PointedCharSequence result;
        long offset;
        do {
            // casts break long files...
            offset = start + (long) getDictionary().getRandom().nextInt(range);
            // first line is at a disadvantage
            result = file.readLine(file.getNextLineOffset(offset));
        } while (null == result);
        return result;
    }

    @Override
    public long getFirstLineOffset(POS pos, DictionaryFileType fileType) throws JWNLException {
        RandomAccessDictionaryFile file = getFile(pos, fileType);
        return file.getFirstLineOffset();
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public synchronized void save() throws JWNLException {
        files.delete();
        files.open();

        // find max offset length
        int maxOffsetLength = 0;
        {
            Iterator<RandomAccessDictionaryFile> itr = files.get(DictionaryFileType.DATA).getFileIterator();
            while (itr.hasNext()) {
                RandomAccessDictionaryFile radf = itr.next();
                int offsetLength = radf.getOffsetLength();
                if (maxOffsetLength < offsetLength) {
                    maxOffsetLength = offsetLength;
                }
            }
        }

        // set the same max offset length for all files
        for (DictionaryFileType dft : DictionaryFileType.getAllDictionaryFileTypes()) {
            Iterator<RandomAccessDictionaryFile> itr = files.get(dft).getFileIterator();
            while (itr.hasNext()) {
                RandomAccessDictionaryFile radf = itr.next();
                radf.setOffsetLength(maxOffsetLength);
            }
        }
        revCntList.setOffsetLength(maxOffsetLength);
        cntList.setOffsetLength(maxOffsetLength);
        senseIndex.setOffsetLength(maxOffsetLength);

        files.save();

        revCntList.save();
        cntList.save();
        senseIndex.save();
    }

    public int getUseCount(String senseKey) throws JWNLException {
        if (cacheUseCount) {
            Integer result = useCountCache.get(senseKey);
            return null == result ? 0 : result;
        } else {
            int result = 0;
            PointedCharSequence line = getIndexedLine(null, DictionaryFileType.REVCNTLIST, senseKey);
            if (null != line && 0 != line.length()) {
                // sense_key  sense_number  tag_cnt
                CharSequenceParser p = new CharSequenceParser(line);
                p.skipToken(); // sense_key
                p.skipToken(); // sense_number
                result = p.nextInt(); // tag_cnt
            }
            return result;
        }
    }

    /**
     * Returns the file from a part of speech and file type (ie data.noun).
     *
     * @param pos      - the part of speech (NOUN, ADJ, VERB, ADV) or null
     * @param fileType - the file type (data, index, exc, cntlist)
     * @return - dictionary file
     */
    private RandomAccessDictionaryFile getFile(POS pos, DictionaryFileType fileType) {
        RandomAccessDictionaryFile file = files.getDictionaryFile(pos, fileType);
        if (null == file) {
            if (DictionaryFileType.REVCNTLIST == fileType) {
                file = revCntList;
            } else if (DictionaryFileType.CNTLIST == fileType) {
                file = cntList;
            } else if (DictionaryFileType.SENSEINDEX == fileType) {
                file = senseIndex;
            }
        }
        return file;
    }

    private void cacheUseCounts() throws JWNLException {
        if (log.isDebugEnabled()) {
            log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_018"));
        }

        PointedCharSequence line = revCntList.readLine(0);
        while (null != line && 0 != line.length()) {
            //sense_key  sense_number  tag_cnt
            CharSequenceParser p = new CharSequenceParser(line);
            String senseKey = p.nextToken(); // sense_key
            p.skipToken(); // skip sense_number
            Integer useCnt = p.nextInt(); // tag_cnt
            useCountCache.put(senseKey, useCnt);
            line = revCntList.readLine(line.getLastBytePosition() + 1);
        }
        if (log.isDebugEnabled()) {
            log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_019"));
        }
    }
}