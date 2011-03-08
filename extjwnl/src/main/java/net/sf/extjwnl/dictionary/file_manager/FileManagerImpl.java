package net.sf.extjwnl.dictionary.file_manager;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.file.*;
import net.sf.extjwnl.util.MessageLog;
import net.sf.extjwnl.util.MessageLogLevel;
import net.sf.extjwnl.util.TokenizerParser;
import net.sf.extjwnl.util.factory.Param;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * An implementation of <code>FileManager</code> that reads files from the local file system.
 * <code>FileManagerImpl</code> caches the file position before and after <code>readLineAt</code>
 * in order to eliminate the redundant IO activity that a naive implementation of these methods
 * would necessitate.
 *
 * @author John Didion <jdidion@users.sourceforge.net>
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public class FileManagerImpl implements FileManager {

    private static final MessageLog log = new MessageLog(DictionaryCatalogSet.class);

    private Dictionary dictionary;

    private RandomAccessDictionaryFile revCntList;
    private RandomAccessDictionaryFile cntList;
    private RandomAccessDictionaryFile senseIndex;

    private static final String CACHE_USE_COUNT_KEY = "cache_use_count";
    private boolean cacheUseCount = true;

    private Map<String, Integer> useCountCache = new HashMap<String, Integer>();
    /**
     * Random number generator used by getRandomLineOffset().
     */
    private static final Random rand = new Random(new Date().getTime());

    /**
     * The catalog set.
     */
    private DictionaryCatalogSet<RandomAccessDictionaryFile> files;

    public FileManagerImpl(Dictionary dictionary, Map<String, Param> params) throws JWNLException {
        try {
            this.dictionary = dictionary;
            files = new DictionaryCatalogSet<RandomAccessDictionaryFile>(dictionary, params, RandomAccessDictionaryFile.class);
            files.open();

            try {
                Class fileClass;
                try {
                    fileClass = Class.forName(params.get(DictionaryCatalog.DICTIONARY_FILE_TYPE_KEY).getValue());
                    if (!RandomAccessDictionaryFile.class.isAssignableFrom(fileClass)) {
                        throw new JWNLRuntimeException("DICTIONARY_EXCEPTION_003", fileClass);
                    }
                } catch (ClassNotFoundException ex) {
                    throw new JWNLRuntimeException("DICTIONARY_EXCEPTION_002", ex);
                }

                if (!params.containsKey(DictionaryCatalog.DICTIONARY_PATH_KEY)) {
                    throw new JWNLException("DICTIONARY_EXCEPTION_052", DictionaryCatalog.DICTIONARY_PATH_KEY);
                }
                String path = params.get(DictionaryCatalog.DICTIONARY_PATH_KEY).getValue();

                @SuppressWarnings("unchecked")
                DictionaryFileFactory<RandomAccessDictionaryFile> factory = (DictionaryFileFactory<RandomAccessDictionaryFile>) params.get(DictionaryCatalog.DICTIONARY_FILE_TYPE_KEY).create();
                revCntList = factory.newInstance(dictionary, path, null, DictionaryFileType.REVCNTLIST);
                cntList = factory.newInstance(dictionary, path, null, DictionaryFileType.CNTLIST);
                senseIndex = factory.newInstance(dictionary, path, null, DictionaryFileType.INDEX);

                revCntList.open();

                if (params.containsKey(CACHE_USE_COUNT_KEY)) {
                    cacheUseCount = Boolean.parseBoolean(params.get(CACHE_USE_COUNT_KEY).getValue());
                }

                if (cacheUseCount) {
                    cacheUseCounts();
                }

                cntList.open();
                senseIndex.open();
            } catch (Exception e) {
                throw new JWNLRuntimeException("DICTIONARY_EXCEPTION_018", DictionaryFileType.REVCNTLIST, e);
            }

        } catch (IOException e) {
            throw new JWNLException("DICTIONARY_EXCEPTION_016", e);
        }
    }

    private void cacheUseCounts() throws IOException {
        log.log(MessageLogLevel.INFO, "PRINCETON_INFO_018");
        String line = revCntList.readLine();
        while (null != line && !"".equals(line)) {
            //sense_key  sense_number  tag_cnt
            TokenizerParser tokenizer = new TokenizerParser(line, " ");
            String senseKey = tokenizer.nextToken();//sense_key
            tokenizer.nextToken();//sense_number
            Integer useCnt = tokenizer.nextInt();//tag_cnt
            useCountCache.put(senseKey, useCnt);
            line = revCntList.readLine();
        }
        log.log(MessageLogLevel.INFO, "PRINCETON_INFO_019");
    }

    public void close() {
        files.close();
        cntList.close();
        revCntList.close();
        senseIndex.close();
    }

    public void delete() throws IOException {
        files.delete();
        cntList.delete();
        revCntList.delete();
        senseIndex.delete();
    }

    public void edit() throws IOException {
        files.edit();
        revCntList.edit();
        cntList.edit();
        senseIndex.edit();
    }

    /**
     * Returns the file from a part of speech and file type (ie data.noun).
     *
     * @param pos      - the part of speech (NOUN, ADJ, VERB, ADV) or null
     * @param fileType - the file type (data, index, exc, cntlist)
     * @return - dictionary file
     */
    public RandomAccessDictionaryFile getFile(POS pos, DictionaryFileType fileType) {
        return files.getDictionaryFile(pos, fileType);
    }


    /**
     * Skips the next line in the file.
     *
     * @param file file
     * @throws IOException IOException
     */
    private void skipLine(RandomAccessDictionaryFile file) throws IOException {
        int c;
        while (((c = file.read()) != -1) && c != '\n' && c != '\r') {
        }
        c = file.read();
        if (c != '\n' && c != '\r') {
            if (-1 < c) {
                file.seek(file.getFilePointer() - 1);
            }
        }
    }

    public String readLineAt(POS pos, DictionaryFileType fileType, long offset) throws IOException {
        return fileReadLineAt(getFile(pos, fileType), offset);
    }

    public String fileReadLineAt(RandomAccessDictionaryFile file, long offset) throws IOException {
        synchronized (file) {
            file.seek(offset);
            String line = file.readLine();
            long nextOffset = file.getFilePointer();
            if (line == null) {
                nextOffset = -1;
            }
            file.setNextLineOffset(offset, nextOffset);
            return line;
        }
    }

    public long getNextLinePointer(POS pos, DictionaryFileType fileType, long offset) throws IOException {
        RandomAccessDictionaryFile file = getFile(pos, fileType);
        synchronized (file) {
            if (file.isPreviousLineOffset(offset) && offset != file.getNextLineOffset()) {
                return file.getNextLineOffset();
            }
            file.seek(offset);
            skipLine(file);
            return file.getFilePointer();
        }
    }

    public long getMatchingLinePointer(POS pos, DictionaryFileType fileType, long offset, String substring)
            throws IOException {

        RandomAccessDictionaryFile file = getFile(pos, fileType);
        if (file == null || file.length() == 0) {
            return -1;
        }

        synchronized (file) {
            file.seek(offset);
            do {
                String line = file.readLineWord();
                long nextOffset = file.getFilePointer();
                if (line == null) {
                    return -1;
                }
                file.setNextLineOffset(offset, nextOffset);
                if (line.indexOf(substring) >= 0) {
                    return offset;
                }
                offset = nextOffset;
            } while (true);
        }
    }

    /**
     * Returns indexed line pointer is typically used to find a word within an index file matching a given part of speech.
     * It first accesses the appropriate file (based on pos and dictionary type), then iterates through the file. Does so
     * by using an offset and string comparison algorithm.
     */
    public long getIndexedLinePointer(POS pos, DictionaryFileType fileType, String target) throws IOException {
        return fileGetIndexedLinePointer(getFile(pos, fileType), target);
    }

    private long fileGetIndexedLinePointer(RandomAccessDictionaryFile file, String target) throws IOException {
        if (file == null || file.length() == 0) {
            return -1;
        }
        synchronized (file) {
            long start = 0;
            long stop = file.length();
            long offset, midpoint; //our current offset within the file
            int compare;
            String word; //current word at a line
            while (true) {
                midpoint = (start + stop) / 2;
                file.seek(midpoint);
                file.readLine();
                offset = file.getFilePointer();
                if (stop == offset) { //we are at eof
                    file.seek(start);
                    offset = file.getFilePointer();
                    while (offset != stop) {
                        word = file.readLineWord();
                        if (word.equals(target)) {
                            return offset;
                        } else {
                            file.readLine();
                            offset = file.getFilePointer();
                        }
                    }
                    return -1;
                }
                word = file.readLineWord();
                compare = word.compareTo(target);
                /**
                 * Determines where to go within the file.
                 */
                if (compare == 0) {
                    return offset;
                } else if (compare > 0) {
                    stop = offset;
                } else {
                    start = offset;
                }
            }
        }
    }

    public long getRandomLinePointer(POS pos, DictionaryFileType fileType) throws IOException {
        long fileLength = getFile(pos, fileType).length();
        long start = getFirstLinePointer(pos, fileType);
        long offset = start + (long) rand.nextInt(((int) fileLength) - (int) start);
        return getNextLinePointer(pos, fileType, offset);
    }

    public long getFirstLinePointer(POS pos, DictionaryFileType fileType) throws IOException {
        long offset = 0;
        long oldOffset = -1;
        RandomAccessDictionaryFile file = getFile(pos, fileType);
        file.seek(offset);
        for (String line = file.readLineWord(); line == null || line.trim().length() == 0; line = file.readLineWord()) {
            offset = getNextLinePointer(pos, fileType, offset);
            if (oldOffset == offset) {
                break;
            }
            oldOffset = offset;
        }
        return offset;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public void save() throws IOException, JWNLException {
        files.delete();
        files.open();
        files.save();

        {
            log.log(MessageLogLevel.INFO, "PRINCETON_INFO_008", revCntList.getFile().getName());
            //cntlist.rev
            Set<String> revCntListContent = new TreeSet<String>();
            for (POS pos : POS.getAllPOS()) {
                Iterator<IndexWord> ii = dictionary.getIndexWordIterator(pos);
                while (ii.hasNext()) {
                    IndexWord iw = ii.next();
                    for (int i = 0; i < iw.getSenses().size(); i++) {
                        for (Word w : iw.getSenses().get(i).getWords()) {
                            if (0 < w.getUseCount()) {
                                revCntListContent.add(w.getSenseKey() + " " + Integer.toString(i) + " " + w.getUseCount());
                            }
                        }
                    }
                }
            }

            revCntList.seek(0);
            revCntList.writeStrings(revCntListContent);
            log.log(MessageLogLevel.INFO, "PRINCETON_INFO_013", revCntList.getFile().getName());
        }

        {
            log.log(MessageLogLevel.INFO, "PRINCETON_INFO_008", cntList.getFile().getName());
            //cntlist.rev
            Map<Integer, String> cntListMap = new HashMap<Integer, String>();
            List<Integer> useCounts = new ArrayList<Integer>();
            for (POS pos : POS.getAllPOS()) {
                Iterator<IndexWord> ii = dictionary.getIndexWordIterator(pos);
                while (ii.hasNext()) {
                    IndexWord iw = ii.next();
                    for (int i = 0; i < iw.getSenses().size(); i++) {
                        for (Word w : iw.getSenses().get(i).getWords()) {
                            String value = w.getSenseKey() + " " + Integer.toString(i);
                            if (0 < w.getUseCount() && !cntListMap.containsValue(value)) {
                                cntListMap.put(w.getUseCount(), value);
                                useCounts.add(w.getUseCount());
                            }
                        }
                    }
                }
            }
            List<String> cntListContent = new ArrayList<String>();
            Collections.sort(useCounts, Collections.<Integer>reverseOrder());
            for (Integer i : useCounts) {
                cntListContent.add(Integer.toString(i) + " " + cntListMap.get(i));
            }
            cntList.seek(0);
            cntList.writeStrings(cntListContent);
            log.log(MessageLogLevel.INFO, "PRINCETON_INFO_013", cntList.getFile().getName());
        }


        {
            //find the largest offset over all POS
            String decimalFormatString = "00000000";
            for (POS pos : POS.getAllPOS()) {
                for (DictionaryFileType dft : DictionaryFileType.getAllDictionaryFileTypes()) {
                    RandomAccessDictionaryFile dictionaryFile = files.getDictionaryFile(pos, dft);
                    if (decimalFormatString.length() < dictionaryFile.getOffsetFormatString().length()) {
                        decimalFormatString = dictionaryFile.getOffsetFormatString();
                    }
                }
            }
            DecimalFormat dfOff = new DecimalFormat(decimalFormatString);
            log.log(MessageLogLevel.INFO, "PRINCETON_INFO_008", senseIndex.getFile().getName());
            Set<String> senseIndexContent = new TreeSet<String>();
            for (POS pos : POS.getAllPOS()) {
                Iterator<IndexWord> ii = dictionary.getIndexWordIterator(pos);
                while (ii.hasNext()) {
                    IndexWord iw = ii.next();
                    for (int i = 0; i < iw.getSenses().size(); i++) {
                        Synset synset = iw.getSenses().get(i);
                        for (Word w : synset.getWords()) {
                            //sense_key  synset_offset  sense_number  tag_cnt
                            senseIndexContent.add(w.getSenseKey() + " " + dfOff.format(synset.getOffset()) + " " + Integer.toString(i) + " " + w.getUseCount());
                        }
                    }
                }
            }

            senseIndex.seek(0);
            senseIndex.writeStrings(senseIndexContent);
            log.log(MessageLogLevel.INFO, "PRINCETON_INFO_013", senseIndex.getFile().getName());
        }
    }

    @Override
    public int getUseCount(String senseKey) throws IOException {
        if (cacheUseCount) {
            Integer result = useCountCache.get(senseKey);
            return null == result ? 0 : result;
        } else {
            long offset = fileGetIndexedLinePointer(revCntList, senseKey);
            if (-1 == offset) {
                return 0;
            } else {
                String line = fileReadLineAt(revCntList, offset);
                //sense_key  sense_number  tag_cnt
                TokenizerParser tokenizer = new TokenizerParser(line, " ");
                tokenizer.nextToken();//sense_key
                tokenizer.nextToken();//sense_number
                return tokenizer.nextInt();//tag_cnt
            }
        }
    }
}