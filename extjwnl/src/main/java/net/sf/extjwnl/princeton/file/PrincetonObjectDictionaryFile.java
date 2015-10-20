package net.sf.extjwnl.princeton.file;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.JWNLIOException;
import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.data.DictionaryElement;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.MapBackedDictionary;
import net.sf.extjwnl.dictionary.file.DictionaryFileFactory;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.dictionary.file.ObjectDictionaryFile;
import net.sf.extjwnl.util.factory.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * <code>ObjectDictionaryFile</code> that accesses files names with the Princeton dictionary file naming convention.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class PrincetonObjectDictionaryFile extends AbstractPrincetonDictionaryFile
        implements ObjectDictionaryFile, DictionaryFileFactory<PrincetonObjectDictionaryFile> {

    private static final Logger log = LoggerFactory.getLogger(PrincetonObjectDictionaryFile.class);

    protected final File file;

    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    private FileInputStream fin = null;
    private FileOutputStream fout = null;

    /**
     * Factory constructor.
     *
     * @param dictionary dictionary
     * @param params     params
     */
    public PrincetonObjectDictionaryFile(Dictionary dictionary, Map<String, Param> params) {
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
    protected PrincetonObjectDictionaryFile(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType, Map<String, Param> params) {
        super(dictionary, path, pos, fileType, params);
        file = new File(path, getFilename());
    }

    @Override
    public PrincetonObjectDictionaryFile newInstance(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType) {
        return new PrincetonObjectDictionaryFile(dictionary, path, pos, fileType, params);
    }

    @Override
    public boolean isOpen() {
        return (null != in || null != out);
    }

    @Override
    public void save() throws JWNLException {
        if (dictionary instanceof MapBackedDictionary) {
            MapBackedDictionary dic = (MapBackedDictionary) dictionary;
            if (log.isDebugEnabled()) {
                log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_004", getFilename()));
            }
            Map<Object, ? extends DictionaryElement> map = dic.getTable(getPOS(), getFileType());

            try {
                getOutputStream().reset();
            } catch (IOException e) {
                throw new JWNLIOException(e);
            }

            writeObject(map);

            if (log.isDebugEnabled()) {
                log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_012", getFilename()));
            }
        }
    }

    @Override
    public void close() {
        try {
            if (canRead()) {
                in.close();
                fin.close();
            }
            if (canWrite()) {
                out.flush();
                out.close();
                fout.flush();
                fout.close();
            }
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(dictionary.getMessages().resolveMessage("EXCEPTION_001", e.getMessage()), e);
            }
        } finally {
            in = null;
            fin = null;
            out = null;
            fout = null;
        }
    }

    @Override
    public void edit() throws JWNLException {
        openStreams();
    }

    /**
     * Open the input and output streams.
     *
     * @throws JWNLException JWNLException
     */
    protected synchronized void openStreams() throws JWNLException {
        if (!canWrite()) {
            openOutputStream();
        }
        if (!canRead()) {
            openInputStream();
        }
    }

    private void openOutputStream() throws JWNLException {
        try {
            fout = new FileOutputStream(getFile());
            out = new ObjectOutputStream(fout);
        } catch (IOException e) {
            throw new JWNLIOException(e);
        }
    }

    private void openInputStream() throws JWNLException {
        try {
            fin = new FileInputStream(getFile());
            in = new ObjectInputStream(new BufferedInputStream(fin));
        } catch (IOException e) {
            throw new JWNLIOException(e);
        }
    }

    protected ObjectInputStream getInputStream() throws JWNLException {
        if (!canRead()) {
            openInputStream();
        }
        return in;
    }

    protected ObjectOutputStream getOutputStream() throws JWNLException {
        if (!canWrite()) {
            openOutputStream();
        }
        return out;
    }

    protected boolean canRead() {
        return in != null;
    }

    protected boolean canWrite() {
        return out != null;
    }

    @Override
    public Object readObject() throws JWNLException {
        if (isOpen()) {
            if (canRead()) {
                try {
                    return getInputStream().readObject();
                } catch (IOException e) {
                    throw new JWNLIOException(e);
                } catch (ClassNotFoundException e) {
                    throw new JWNLException(e);
                }
            } else {
                return new HashMap<Object, DictionaryElement>();
            }
        } else {
            throw new JWNLRuntimeException(dictionary.getMessages().resolveMessage("PRINCETON_EXCEPTION_001"));
        }
    }

    @Override
    public void writeObject(Object obj) throws JWNLException {
        if (isOpen() && canWrite()) {
            try {
                getOutputStream().writeObject(obj);
            } catch (IOException e) {
                throw new JWNLIOException(e);
            }
        } else {
            throw new JWNLRuntimeException(dictionary.getMessages().resolveMessage("PRINCETON_EXCEPTION_002"));
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
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        throw new JWNLIOException(e);
                    }
                    openOutputStream();
                } else {
                    openInputStream();
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
