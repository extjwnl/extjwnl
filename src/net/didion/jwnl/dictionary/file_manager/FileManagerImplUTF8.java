package net.didion.jwnl.dictionary.file_manager;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.JWNLRuntimeException;
import net.didion.jwnl.dictionary.file.DictionaryFileType;
import net.didion.jwnl.dictionary.file.RandomAccessDictionaryFile;
import net.didion.jwnl.util.factory.Param;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Map;

/**
 * An implementation of <code>FileManager</code> for files in UTF-8.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class FileManagerImplUTF8 extends FileManagerImpl implements FileManager {

    private static final Charset charset = Charset.forName("UTF-8");
    private static final CharsetDecoder decoder = charset.newDecoder();

    public FileManagerImplUTF8() {
    }

    private int LINE_MAX = 10240;//10K buffer
    private byte[] lineArr = new byte[LINE_MAX];

    /**
     * Reads the first word from a file (ie offset, index word)
     *
     * @param file - the file
     * @return - string
     * @throws IOException
     */
    protected String readLineWord(RandomAccessDictionaryFile file) throws IOException {
        if (file.getFileType() != DictionaryFileType.DATA) {
            int idx = 1;
            int c;
            while (((c = file.read()) != -1) && c != '\n' && c != '\r' && c != ' ') {
                lineArr[idx - 1] = (byte) c;
                idx++;
                if (LINE_MAX == idx) {
                    byte[] t = new byte[LINE_MAX * 2];
                    System.arraycopy(lineArr, 0, t, 0, LINE_MAX);
                    lineArr = t;
                    LINE_MAX = 2 * LINE_MAX;
                }
            }
            if (1 < idx) {
                ByteBuffer bb = ByteBuffer.wrap(lineArr, 0, idx - 1);
                CharBuffer cb = decoder.decode(bb);
                return cb.toString();
            } else {
                return "";
            }
        } else {
            return super.readLineWord(file);
        }
    }

    public Object create(Map params) throws JWNLException {
        Class fileClass;
        try {
            fileClass = Class.forName(((Param) params.get(FILE_TYPE)).getValue());
        } catch (ClassNotFoundException ex) {
            throw new JWNLRuntimeException("DICTIONARY_EXCEPTION_002", ex);
        }
        checkFileType(fileClass);

        String path = ((Param) params.get(PATH)).getValue();

        try {
            return new FileManagerImplUTF8(path, fileClass);
        } catch (IOException ex) {
            throw new JWNLException("DICTIONARY_EXCEPTION_016", fileClass, ex);
        }
    }

    public FileManagerImplUTF8(String searchDir, Class dictionaryFileType) throws IOException {
        super(searchDir, dictionaryFileType);
    }
}