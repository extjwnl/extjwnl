package net.sf.extjwnl.princeton.file;

import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.file.DictionaryFileFactory;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.util.factory.Param;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * A <code>RandomAccessDictionaryFile</code> that accesses files named with Princeton's dictionary file naming convention.
 * The file is loaded from classpath and stored in CharBuffer.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class PrincetonResourceDictionaryFile extends PrincetonCharBufferFile implements DictionaryFileFactory<PrincetonResourceDictionaryFile> {

    private long size;

    public PrincetonResourceDictionaryFile(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary, params);
    }

    public PrincetonResourceDictionaryFile(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType, Map<String, Param> params) {
        super(dictionary, path, pos, fileType, params);
    }

    public PrincetonResourceDictionaryFile newInstance(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType) {
        return new PrincetonResourceDictionaryFile(dictionary, path, pos, fileType, params);
    }

    public void openFile() throws IOException {
        InputStream input = PrincetonResourceDictionaryFile.class.getResourceAsStream(path + "/" + getFilename());
        try {
            // data.noun is about 16M
            ByteArrayOutputStream output = new ByteArrayOutputStream(16 * 1024 * 1024);
            try {
                copyStream(input, output);
                size = output.size();
                if (null != encoding) {
                    buffer = Charset.forName(encoding).newDecoder().decode(ByteBuffer.wrap(output.toByteArray()));
                } else {
                    buffer = Charset.defaultCharset().newDecoder().decode(ByteBuffer.wrap(output.toByteArray()));
                }
            } finally {
                output.close();
            }
        } finally {
            input.close();
        }
    }

    public long length() throws IOException {
        return size;
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

    private static void copyStream(InputStream input, OutputStream output) throws IOException {
        WritableByteChannel outputChannel = Channels.newChannel(output);
        try {
            ReadableByteChannel inputChannel = Channels.newChannel(input);
            try {
                fastChannelCopy(inputChannel, outputChannel);
            } finally {
                inputChannel.close();
            }
        } finally {
            outputChannel.close();
        }
    }
}