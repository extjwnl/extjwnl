/**
 * Java WordNet Library (JWNL)
 * See the documentation for copyright information.
 */
package net.didion.jwnl.dictionary.file_manager;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.JWNLRuntimeException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.file.DictionaryCatalogSet;
import net.didion.jwnl.dictionary.file.DictionaryFile;
import net.didion.jwnl.dictionary.file.DictionaryFileType;
import net.didion.jwnl.dictionary.file.RandomAccessDictionaryFile;
import net.didion.jwnl.util.Grep;
import net.didion.jwnl.util.factory.Param;

/**
 * An implementation of <code>FileManager</code> that reads files from the local file system.
 * <code>FileManagerImpl</code> caches the file position before and after <code>readLineAt</code>
 * in order to eliminate the redundant IO activity that a naive implementation of these methods
 * would necessitate.
 */
public class FileManagerImpl implements FileManager {
    

	/**
     * File type install parameter. The value should be the
     * name of the appropriate subclass of DictionaryFileType.
     */
	public static final String FILE_TYPE = "file_type";
	/**
	 * Dictionary path install parameter. The value should be the absolute path
	 * of the directory containing the dictionary files.
	 */
	public static final String PATH = "dictionary_path";
    /**
     * Random number generator used by getRandomLineOffset().
     */
    private static final Random _rand = new Random(new Date().getTime());

    /**
     * The catalog set. 
     */
	private DictionaryCatalogSet _files;
	
    /**
     * The sense key file. 
     */
	private File senseFile;
	
    /**
     * Uninitialized FileManagerImpl. 
     *
     */
	public FileManagerImpl() {}

	/**
	 * Construct a file manager backed by a set of files contained
	 * in the default WN search directory.
	 */
	public FileManagerImpl(String searchDir, Class dictionaryFileType) throws IOException {
		checkFileType(dictionaryFileType);
		_files = new DictionaryCatalogSet(searchDir, dictionaryFileType);
		_files.open();
        String sense = System.getProperty("file.separator") + "index.sense";
        if (JWNL.getVersion().getNumber() < 2.1) {
            sense = System.getProperty("file.separator") + "sense.idx";
		}
        senseFile = new File(searchDir + sense);
        
		Grep.setFile(senseFile);
	}

    /**
     * {@inheritDoc}
     */
	public Object create(Map params) throws JWNLException {
		Class fileClass = null;
		try {
			fileClass = Class.forName(((Param)params.get(FILE_TYPE)).getValue());
		} catch (ClassNotFoundException ex) {
			throw new JWNLRuntimeException("DICTIONARY_EXCEPTION_002", ex);
		}
		checkFileType(fileClass);

		String path = ((Param)params.get(PATH)).getValue();

		try {
			return new FileManagerImpl(path, fileClass);
		} catch (IOException ex) {
			throw new JWNLException("DICTIONARY_EXCEPTION_016", fileClass, ex);
		}
	}

    /**
     * Checks the type to ensure it's valid. 
     * @param c
     */
	private void checkFileType(Class c) {
		if (!DictionaryFile.class.isAssignableFrom(c)) {
            throw new JWNLRuntimeException("DICTIONARY_EXCEPTION_003", c);
        }
	}

    /**
     * {@inheritDoc}
     */
	public void close() {
		_files.close();
	}

    /**
     * Gets the file from a part of speech and file type (ie data.noun).
     * @param pos - the part of speech (NOUN, ADJ, VERB, ADV)
     * @param fileType - the file type (data, index, exc)
     * @return - dictionary file
     */
	public DictionaryFile getFile(POS pos, DictionaryFileType fileType) {
		return _files.getDictionaryFile(pos, fileType);
	}

	

    /**
     * Skips the next line in the file. 
     * @param file
     * @throws IOException
     */
	private void skipLine(RandomAccessDictionaryFile file) throws IOException {
		int c;
		while (((c = file.read()) != -1) && c != '\n' && c != '\r');
		c = file.read();
		if (c != '\n' && c != '\r') {
			file.seek(file.getFilePointer()-1);
		}
	}

	/**
     * {@inheritDoc}
	 */
	public String readLineAt(POS pos, DictionaryFileType fileType, long offset) throws IOException {
		RandomAccessDictionaryFile file = (RandomAccessDictionaryFile)getFile(pos, fileType);
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
	
	
	/**
     * Reads the first word from a file (ie offset, index word) 
     * @param file - the file
     * @return - string
     * @throws IOException
	 */
	private String readLineWord(RandomAccessDictionaryFile file) throws IOException {
		StringBuffer input = new StringBuffer();
		int c;
		while (((c = file.read()) != -1) && c != '\n' && c != '\r' && c != ' ') {
			input.append((char) c);
		}
		return input.toString();
	}

    /**
     * {@inheritDoc}
     */
	public long getNextLinePointer(POS pos, DictionaryFileType fileType, long offset) throws IOException {
		RandomAccessDictionaryFile file = (RandomAccessDictionaryFile)getFile(pos, fileType);
		synchronized (file) {
			if (file.isPreviousLineOffset(offset) && offset != file.getNextLineOffset()) {
                return file.getNextLineOffset();
			}
			file.seek(offset);
			skipLine(file);
			return file.getFilePointer();
		}
	}

	/**
     * {@inheritDoc}
	 */
	public long getMatchingLinePointer(POS pos, DictionaryFileType fileType, long offset, String substring)
	    throws IOException {

		RandomAccessDictionaryFile file = (RandomAccessDictionaryFile)getFile(pos, fileType);
		if (file == null || file.length() == 0) return -1;

		synchronized (file) {
			file.seek(offset);
			do {
				String line = readLineWord(file);
				long nextOffset = file.getFilePointer();
				if (line == null) return -1;
				file.setNextLineOffset(offset, nextOffset);
				if (line.indexOf(substring) >= 0) return offset;
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
		RandomAccessDictionaryFile file = (RandomAccessDictionaryFile)getFile(pos, fileType);
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
						word = readLineWord(file);
						if (word.equals(target)) {
							return offset;
						} else {
							file.readLine();
							offset = file.getFilePointer();
						}
					}
					return -1;
				}
				word = readLineWord(file);
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

    /**
     * {@inheritDoc}
     */
    public long getRandomLinePointer(POS pos, DictionaryFileType fileType) throws IOException {
        long fileLength = ((RandomAccessDictionaryFile) getFile(pos, fileType)).length();
        long start = getFirstLinePointer(pos, fileType);
        long offset = start + (long) _rand.nextInt(((int) fileLength) - (int) start);
        return getNextLinePointer(pos, fileType, offset);
    }

    /**
     * {@inheritDoc}
     */
    public long getFirstLinePointer(POS pos, DictionaryFileType fileType) throws IOException {
        long offset = 0;
        RandomAccessDictionaryFile file = (RandomAccessDictionaryFile) getFile(pos, fileType);
        String line = null;
        for (line = null; line == null || line.trim().length() == 0; line = readLineWord(file)) {
            offset = getNextLinePointer(pos, fileType, offset);
        }
        return offset;
    }

    
}