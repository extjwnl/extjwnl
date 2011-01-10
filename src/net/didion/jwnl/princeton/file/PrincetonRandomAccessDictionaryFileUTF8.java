package net.didion.jwnl.princeton.file;

import net.didion.jwnl.JWNLRuntimeException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.file.DictionaryFile;
import net.didion.jwnl.dictionary.file.DictionaryFileType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * A <code>RandomAccessDictionaryFile</code> that accesses WordNet files in UTF-8.
 *
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class PrincetonRandomAccessDictionaryFileUTF8 extends PrincetonRandomAccessDictionaryFile {

    private static final Charset charset = Charset.forName("UTF-8");
    private static final CharsetDecoder decoder = charset.newDecoder();

    public PrincetonRandomAccessDictionaryFileUTF8() {
    }

    public DictionaryFile newInstance(String path, POS pos, DictionaryFileType fileType) {
        return new PrincetonRandomAccessDictionaryFileUTF8(path, pos, fileType);
    }

    public PrincetonRandomAccessDictionaryFileUTF8(String path, POS pos, DictionaryFileType fileType) {
        super(path, pos, fileType, READ_ONLY);
    }

    private int LINE_MAX = 10240;//10K buffer
    private byte[] lineArr = new byte[LINE_MAX];

    public String readLine() throws IOException {
        if (isOpen()) {
            long offStart = _file.getFilePointer();
            String result = super.readLine();
            if (null != result) {
                long offEnd = _file.getFilePointer();
                _file.seek(offStart);
                int c;
                int idx = 1;
                while (((c = read()) != -1) && c != '\n' && c != '\r') {
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
                    result = cb.toString();
                } else {
                    result = "";
                    if (-1 == c) {
                        result = null;
                    }
                }
                _file.seek(offEnd);
            }
            return result;
        } else {
            throw new JWNLRuntimeException("PRINCETON_EXCEPTION_001");
        }
    }
}