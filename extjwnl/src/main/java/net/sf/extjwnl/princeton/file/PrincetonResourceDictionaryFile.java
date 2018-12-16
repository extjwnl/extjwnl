package net.sf.extjwnl.princeton.file;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.JWNLIOException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.file.DictionaryFileFactory;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.util.ByteArrayCharSequence;
import net.sf.extjwnl.util.CharBufferCharSequence;
import net.sf.extjwnl.util.PointedCharSequence;
import net.sf.extjwnl.util.factory.Param;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Map;

/**
 * Loads dictionary files from classpath.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class PrincetonResourceDictionaryFile extends AbstractPrincetonRandomAccessDictionaryFile
        implements DictionaryFileFactory<PrincetonResourceDictionaryFile> {

    private byte[] buffer;
    private int firstLineOffset;
    private final CharsetDecoder decoder;

    /**
     * Factory constructor.
     *
     * @param dictionary dictionary
     * @param params     params
     */
    public PrincetonResourceDictionaryFile(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary, params);
        this.decoder = null;
    }

    /**
     * Instance constructor.
     *
     * @param dictionary dictionary
     * @param path       file path
     * @param pos        part of speech
     * @param fileType   file type
     * @param params     params
     */
    public PrincetonResourceDictionaryFile(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType, Map<String, Param> params) {
        super(dictionary, path, pos, fileType, params);
        this.firstLineOffset = -1;
        this.buffer = null;
        if (null != encoding) {
            Charset charset = Charset.forName(encoding);
            decoder = charset.newDecoder();
        } else {
            decoder = null;
        }
    }

    @Override
    public PrincetonResourceDictionaryFile newInstance(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType) {
        return new PrincetonResourceDictionaryFile(dictionary, path, pos, fileType, params);
    }

    @Override
    public void open() throws JWNLException {
        try (final InputStream input =
                     PrincetonResourceDictionaryFile.class.getResourceAsStream(path + "/" + getFilename())) {
            // data.noun is about 16M
            try (final ByteArrayOutputStream output = new ByteArrayOutputStream(16 * 1024 * 1024)) {
                copyStream(input, output);
                buffer = output.toByteArray();
            }
        } catch (IOException e) {
            throw new JWNLIOException(e);
        }
    }

    @Override
    public boolean isOpen() {
        return null != buffer;
    }

    @Override
    public void close() {
        buffer = null;
    }

    @Override
    public void save() throws JWNLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void edit() throws JWNLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getFirstLineOffset() throws JWNLException {
        // fixed DCL idiom: http://en.wikipedia.org/wiki/Double-checked_locking
        if (-1 == firstLineOffset) {
            synchronized (this) {
                if (-1 == firstLineOffset) {
                    if (!isOpen()) {
                        throw new JWNLException(dictionary.getMessages().resolveMessage("PRINCETON_EXCEPTION_001"));
                    }

                    int i = 0;
                    boolean eol = true;
                    while (i < buffer.length) {
                        if (eol && ' ' != buffer[i]) {
                            break;
                        }
                        eol = '\n' == buffer[i];
                        i++;
                    }

                    firstLineOffset = i;
                }
            }
        }

        return firstLineOffset;
    }

    @Override
    public long getNextLineOffset(long offset) throws JWNLException {
        if (!isOpen()) {
            throw new JWNLException(dictionary.getMessages().resolveMessage("PRINCETON_EXCEPTION_001"));
        }

        int loffset = (int) offset;

        if (loffset >= buffer.length || loffset < 0) {
            return -1;
        }

        int i = loffset;
        while (i < buffer.length && '\n' != buffer[i]) {
            i++;
        }
        // we've read the line

        long result = i + 1;
        if (result >= buffer.length) {
            result = -1;
        }

        return result;
    }

    @Override
    public int getOffsetLength() throws JWNLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOffsetLength(int length) throws JWNLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PointedCharSequence readLine(long offset) throws JWNLException {
        if (!isOpen()) {
            throw new JWNLException(dictionary.getMessages().resolveMessage("PRINCETON_EXCEPTION_001"));
        }

        // long files limitation due to limitation in ByteBuffer.
        int loffset = (int) offset;

        if (loffset >= buffer.length || loffset < 0) {
            return null;
        }

        int i = loffset;
        while (i < buffer.length && '\n' != buffer[i]) {
            i++;
        }

        // resulting line ends at i (eol or eof)
        final PointedCharSequence result;

        if (null == encoding) {
            result = new ByteArrayCharSequence(buffer, loffset, i, i);
        } else {
            final ByteBuffer bb = ByteBuffer.wrap(buffer, loffset, i - loffset);

            try {
                synchronized (this) {
                    CharBuffer cb = decoder.decode(bb);
                    result = new CharBufferCharSequence(cb, i);
                }
            } catch (CharacterCodingException e) {
                throw new JWNLIOException(dictionary.getMessages().resolveMessage("PRINCETON_EXCEPTION_003",
                        new Object[]{getFilename(), loffset}), e);
            }
        }
        return result;
    }

    @Override
    public PointedCharSequence readWord(long offset) throws JWNLException {
        if (!isOpen()) {
            throw new JWNLException(dictionary.getMessages().resolveMessage("PRINCETON_EXCEPTION_001"));
        }

        // long files limitation due to limitation in ByteBuffer.
        int loffset = (int) offset;

        if (loffset >= buffer.length || loffset < 0) {
            return null;
        }

        int i = loffset;
        while (i < buffer.length && ' ' != buffer[i] && '\n' != buffer[i]) {
            i++;
        }

        // resulting word ends at i (space, eol or eof)
        final PointedCharSequence result;

        if (null == encoding) {
            result = new ByteArrayCharSequence(buffer, loffset, i, i);
        } else {
            final ByteBuffer bb = ByteBuffer.wrap(buffer, loffset, i - loffset);

            try {
                synchronized (this) {
                    CharBuffer cb = decoder.decode(bb);
                    result = new CharBufferCharSequence(cb, i);
                }
            } catch (CharacterCodingException e) {
                throw new JWNLIOException(dictionary.getMessages().resolveMessage("PRINCETON_EXCEPTION_003",
                        new Object[]{getFilename(), loffset}), e);
            }
        }
        return result;
    }

    public long length() throws JWNLException {
        return buffer.length;
    }

    private static void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
        while (src.read(buffer) != -1) {
            // prepare the buffer to be drained
            buffer.flip();
            // write to the channel, may block
            dest.write(buffer);
            // If partial transfer, shift remainder down
            // If buffer is empty, same as doing clear()
            buffer.compact();
        }
        // EOF will leave buffer in fill state
        buffer.flip();
        // make sure the buffer is fully drained.
        while (buffer.hasRemaining()) {
            dest.write(buffer);
        }
    }

    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        try (final WritableByteChannel outputChannel = Channels.newChannel(output)) {
            try (final ReadableByteChannel inputChannel = Channels.newChannel(input)) {
                fastChannelCopy(inputChannel, outputChannel);
            }
        }
    }
}