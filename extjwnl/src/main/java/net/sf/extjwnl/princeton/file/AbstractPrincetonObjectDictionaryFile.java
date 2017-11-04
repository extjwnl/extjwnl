package net.sf.extjwnl.princeton.file;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.JWNLIOException;
import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.data.DictionaryElement;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.MapBackedDictionary;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.dictionary.file.ObjectDictionaryFile;
import net.sf.extjwnl.util.factory.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Parent for <code>ObjectDictionaryFile</code>s that access files with the Princeton dictionary file naming convention.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class AbstractPrincetonObjectDictionaryFile extends AbstractPrincetonDictionaryFile
        implements ObjectDictionaryFile {

    private static final Logger log = LoggerFactory.getLogger(AbstractPrincetonObjectDictionaryFile.class);

    protected ObjectInputStream in = null;
    protected ObjectOutputStream out = null;

    /**
     * Factory constructor.
     *
     * @param dictionary dictionary
     * @param params     params
     */
    public AbstractPrincetonObjectDictionaryFile(final Dictionary dictionary, final Map<String, Param> params) {
        super(dictionary, params);
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
    protected AbstractPrincetonObjectDictionaryFile(final Dictionary dictionary,
                                                    final String path,
                                                    final POS pos,
                                                    final DictionaryFileType fileType,
                                                    final Map<String, Param> params) {
        super(dictionary, path, pos, fileType, params);
    }

    @Override
    public boolean isOpen() {
        return (null != in || null != out);
    }

    @Override
    public void save() throws JWNLException {
        if (dictionary instanceof MapBackedDictionary) {
            final MapBackedDictionary dic = (MapBackedDictionary) dictionary;
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
            closeStreams(in, out);
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error(dictionary.getMessages().resolveMessage("EXCEPTION_001", e.getMessage()), e);
            }
        } finally {
            in = null;
            out = null;
        }
    }

    protected void closeStreams(final InputStream in, final OutputStream out) throws IOException {
        if (canRead()) {
            in.close();
        }
        if (canWrite()) {
            out.flush();
            out.close();
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

    protected abstract void openOutputStream() throws JWNLException;

    protected abstract void openInputStream() throws JWNLException;

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
                } catch (IOException | ClassNotFoundException e) {
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
    public abstract void open() throws JWNLException;
}
