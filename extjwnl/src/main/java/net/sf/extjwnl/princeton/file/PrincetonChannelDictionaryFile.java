package net.sf.extjwnl.princeton.file;

import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.file.DictionaryFileFactory;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.util.factory.Param;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * A <code>RandomAccessDictionaryFile</code> that accesses files named with Princeton's dictionary file naming convention.
 * Uses java.nio.channels.FileChannel for file access.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class PrincetonChannelDictionaryFile extends AbstractPrincetonRandomAccessDictionaryFile implements DictionaryFileFactory<PrincetonChannelDictionaryFile> {
    /**
     * The random-access file.
     */
    private CharBuffer buffer = null;
    private FileChannel channel = null;

    public PrincetonChannelDictionaryFile(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary, params);
    }

    public PrincetonChannelDictionaryFile(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType, Map<String, Param> params) {
        super(dictionary, path, pos, fileType, params);
    }

    public PrincetonChannelDictionaryFile newInstance(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType) {
        return new PrincetonChannelDictionaryFile(dictionary, path, pos, fileType, params);
    }

    public String readLine() throws IOException {
        if (isOpen()) {
            synchronized (file) {
                //The following lines gratuitously lifted from java.io.RandomAccessFile.readLine()
                StringBuilder input = new StringBuilder();
                char c = (char) -1;
                boolean eol = false;

                while (!eol) {
                    c = buffer.get((int) getFilePointer());
                    buffer.position((int) getFilePointer() + 1);

                    switch (c) {
                        case (char) -1:
                        case '\n':
                            eol = true;
                            break;
                        case '\r':
                            eol = true;
                            if ((buffer.get((int) getFilePointer() + 1)) == '\n') {
                                buffer.position((int) getFilePointer() + 1);
                            }
                            break;
                        default:
                            input.append(c);
                            break;
                    }
                }
                return ((c == -1) && (input.length() == 0)) ? null : input.toString();
            }
        } else {
            throw new JWNLRuntimeException("PRINCETON_EXCEPTION_001");
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
            throw new JWNLRuntimeException("PRINCETON_EXCEPTION_001");
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
        return channel != null;
    }

    public void save() {
        throw new UnsupportedOperationException();
    }

    public void close() {
        try {
            if (null != channel) {
                buffer = null;
                channel.close();
            }
        } catch (IOException ex) {
            //nop
        } finally {
            channel = null;
        }
    }

    protected void openFile() throws IOException {
        channel = new FileInputStream(file).getChannel();
        if (null != encoding) {
            buffer = Charset.forName(encoding).newDecoder().decode(channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()));
        } else {
            buffer = Charset.defaultCharset().newDecoder().decode(channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()));
        }
    }

    public long length() throws IOException {
        // Do not use "buffer.length()" because it returns the
        // buffer length, not the total length of the file
        return channel.size();
    }

    public int read() throws IOException {
        return (int) buffer.get();
    }

    public int getOffsetLength() {
        throw new UnsupportedOperationException();
    }

    public void setOffsetLength(int length) {
        throw new UnsupportedOperationException();
    }
}