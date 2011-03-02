package net.didion.jwnl.dictionary.file_manager;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.dictionary.file.DictionaryCatalogSet;
import net.didion.jwnl.dictionary.file.DictionaryFileType;
import net.didion.jwnl.dictionary.file.RandomAccessDictionaryFile;
import net.didion.jwnl.util.factory.Param;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Random;

/**
 * An implementation of <code>FileManager</code> that reads files from the local file system.
 * <code>FileManagerImpl</code> caches the file position before and after <code>readLineAt</code>
 * in order to eliminate the redundant IO activity that a naive implementation of these methods
 * would necessitate.
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class FileManagerImpl implements FileManager {

    private Dictionary dictionary;

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
        } catch (IOException e) {
            throw new JWNLException("DICTIONARY_EXCEPTION_016", e);
        }
    }

    public void close() {
        files.close();
    }

    public void delete() throws IOException {
        files.delete();
    }

    /**
     * Gets the file from a part of speech and file type (ie data.noun).
     *
     * @param pos      - the part of speech (NOUN, ADJ, VERB, ADV)
     * @param fileType - the file type (data, index, exc)
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
        RandomAccessDictionaryFile file = getFile(pos, fileType);
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
     * Get indexed line pointer is typically used to find a word within an index file matching a given part of speech.
     * It first accesses the appropriate file (based on pos and dictionary type), then iterates through the file. Does so
     * by using an offset and string comparison algorithm.
     */
    public long getIndexedLinePointer(POS pos, DictionaryFileType fileType, String target) throws IOException {
        RandomAccessDictionaryFile file = getFile(pos, fileType);
        if (file == null || file.length() == 0) {
            return -1;
        }
        synchronized (file) {
            long start = 0;
            long stop = file.length();
            long offset = start, midpoint; //our current offset within the file
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
    }
}