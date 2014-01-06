package net.sf.extjwnl.princeton.file;

import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.util.factory.Param;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Map;

/**
 * A <code>RandomAccessDictionaryFile</code> that accesses files named with Princeton's dictionary file naming convention.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class PrincetonCharBufferFile extends AbstractPrincetonRandomAccessDictionaryFile {

    protected CharBuffer buffer = null;

    public PrincetonCharBufferFile(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary, params);
    }

    public PrincetonCharBufferFile(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType, Map<String, Param> params) {
        super(dictionary, path, pos, fileType, params);
    }

    public String readLine() throws IOException {
        if (isOpen()) {
            synchronized (file) {
                //The following lines gratuitously lifted from java.io.RandomAccessFile.readLine()
                StringBuilder input = new StringBuilder();
                int c = -1;
                boolean eol = false;

                while (!eol) {
                    c = read();

                    switch (c) {
                        case -1:
                        case (int) '\n':
                            eol = true;
                            break;
                        case (int) '\r':
                            eol = true;
                            if (buffer.position() < (buffer.limit() - 1)) {
                                if ((buffer.get(buffer.position() + 1)) == '\n') {
                                    buffer.position(buffer.position() + 1);
                                }
                            }
                            break;
                        default:
                            input.append((char) c);
                            break;
                    }
                }
                return ((c == -1) && (input.length() == 0)) ? null : input.toString();
            }
        } else {
            throw new JWNLRuntimeException(dictionary.getMessages().resolveMessage("PRINCETON_EXCEPTION_001"));
        }
    }

    public String readLineWord() throws IOException {
        if (isOpen()) {
            synchronized (file) {
                StringBuilder input = new StringBuilder();
                int c;
                while (((c = read()) != -1) && c != '\n' && c != '\r' && c != ' ') {
                    input.append((char) c);
                }
                return input.toString();
            }
        } else {
            throw new JWNLRuntimeException(dictionary.getMessages().resolveMessage("PRINCETON_EXCEPTION_001"));
        }
    }

    public void seek(long pos) throws IOException {
        synchronized (file) {
            buffer.position((int) pos);
        }
    }

    public long getFilePointer() throws IOException {
        synchronized (file) {
            return (long) buffer.position();
        }
    }

    public boolean isOpen() {
        return buffer != null;
    }

    public void save() {
        throw new UnsupportedOperationException();
    }

    public void close() {
        buffer = null;
    }

    public int read() throws IOException {
        if (buffer.position() < buffer.limit()) {
            return (int) buffer.get();
        } else {
            return -1;
        }
    }

    public int getOffsetLength() {
        throw new UnsupportedOperationException();
    }

    public void setOffsetLength(int length) {
        throw new UnsupportedOperationException();
    }
}
