package net.sf.extjwnl.princeton.file;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.JWNLIOException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.file.DictionaryDiskFile;
import net.sf.extjwnl.dictionary.file.DictionaryFileFactory;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.util.factory.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;

/**
 * <code>ObjectDictionaryFile</code> that loads dictionary files from file system.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class PrincetonObjectDictionaryFile extends AbstractPrincetonObjectDictionaryFile
        implements DictionaryDiskFile, DictionaryFileFactory<PrincetonObjectDictionaryFile> {

    private static final Logger log = LoggerFactory.getLogger(PrincetonObjectDictionaryFile.class);

    protected final File file;

    private FileInputStream fin = null;
    private FileOutputStream fout = null;

    /**
     * Factory constructor.
     *
     * @param dictionary dictionary
     * @param params     params
     */
    public PrincetonObjectDictionaryFile(final Dictionary dictionary, final Map<String, Param> params) {
        super(dictionary, params);
        file = null;
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
    private PrincetonObjectDictionaryFile(final Dictionary dictionary,
                                            final String path,
                                            final POS pos,
                                            final DictionaryFileType fileType,
                                            final Map<String, Param> params) {
        super(dictionary, path, pos, fileType, params);
        file = new File(path, getFilename());
    }

    @Override
    public PrincetonObjectDictionaryFile newInstance(final Dictionary dictionary,
                                                     final String path,
                                                     final POS pos,
                                                     final DictionaryFileType fileType) {
        return new PrincetonObjectDictionaryFile(dictionary, path, pos, fileType, params);
    }

    @Override
    public void close() {
        super.close();
        try {
            closeStreams(fin, fout);
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(dictionary.getMessages().resolveMessage("EXCEPTION_001", e.getMessage()), e);
            }
        } finally {
            fin = null;
            fout = null;
        }
    }

    protected void openOutputStream() throws JWNLException {
        try {
            fout = new FileOutputStream(getFile());
            out = new ObjectOutputStream(fout);
        } catch (IOException e) {
            throw new JWNLIOException(e);
        }
    }

    protected void openInputStream() throws JWNLException {
        try {
            fin = new FileInputStream(getFile());
            in = new ObjectInputStream(new BufferedInputStream(fin));
        } catch (IOException e) {
            throw new JWNLIOException(e);
        }
    }

    /**
     * Here we try to be intelligent about opening streams.
     * If the file does not already exist, we assume that we are going
     * to be creating it and writing to it, otherwise we assume that
     * we are going to be reading from it. If you want the other stream
     * open, you must do it explicitly by calling <code>openStreams</code>.
     */
    @Override
    public void open() throws JWNLException {
        synchronized (file) {
            if (!isOpen()) {
                try {
                    if (file.createNewFile()) {
                        openOutputStream();
                    } else {
                        openInputStream();
                    }
                } catch (IOException e) {
                    throw new JWNLIOException(e);
                }
            }
        }
    }

    @Override
    public boolean delete() throws JWNLException {
        close();
        return file.delete();
    }

    @Override
    public File getFile() {
        return file;
    }
}
