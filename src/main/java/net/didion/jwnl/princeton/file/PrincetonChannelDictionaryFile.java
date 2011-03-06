package net.didion.jwnl.princeton.file;

import net.didion.jwnl.JWNLRuntimeException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.dictionary.file.DictionaryFileFactory;
import net.didion.jwnl.dictionary.file.DictionaryFileType;
import net.didion.jwnl.util.factory.Param;

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
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
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
            //The following lines gratuitously lifted from java.io.RandomAccessFile.readLine()
            StringBuffer input = new StringBuffer();
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
        } else {
            throw new JWNLRuntimeException("PRINCETON_EXCEPTION_001");
        }
    }

    public String readLineWord() throws IOException {
        if (isOpen()) {
            StringBuffer input = new StringBuffer();
            int c;
            while (((c = read()) != -1) && c != '\n' && c != '\r' && c != ' ') {
                input.append((char) c);
            }
            return input.toString();
        } else {
            throw new JWNLRuntimeException("PRINCETON_EXCEPTION_001");
        }
    }

    public void seek(long pos) throws IOException {
        buffer.position((int) pos);
    }

    public long getFilePointer() throws IOException {
        return (long) buffer.position();
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

    public void edit() throws IOException {
        throw new UnsupportedOperationException();
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
}