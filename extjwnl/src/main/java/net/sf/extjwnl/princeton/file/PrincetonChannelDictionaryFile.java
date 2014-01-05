package net.sf.extjwnl.princeton.file;

import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.file.DictionaryFileFactory;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.util.factory.Param;

import java.io.FileInputStream;
import java.io.IOException;
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
public class PrincetonChannelDictionaryFile extends PrincetonCharBufferFile implements DictionaryFileFactory<PrincetonChannelDictionaryFile> {

    private long size;

    public PrincetonChannelDictionaryFile(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary, params);
    }

    public PrincetonChannelDictionaryFile(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType, Map<String, Param> params) {
        super(dictionary, path, pos, fileType, params);
    }

    public PrincetonChannelDictionaryFile newInstance(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType) {
        return new PrincetonChannelDictionaryFile(dictionary, path, pos, fileType, params);
    }

    public void openFile() throws IOException {
        FileInputStream stream = new FileInputStream(file);
        try {
            FileChannel channel = stream.getChannel();
            try {
                size = channel.size();
                if (null != encoding) {
                    buffer = Charset.forName(encoding).newDecoder().decode(channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()));
                } else {
                    buffer = Charset.defaultCharset().newDecoder().decode(channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()));
                }
            } finally {
                channel.close();
            }
        } finally {
            stream.close();
        }
    }

    public long length() throws IOException {
        return size;
    }
}