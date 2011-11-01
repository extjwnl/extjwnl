package net.sf.extjwnl.princeton.file;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.data.DictionaryElement;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.MapBackedDictionary;
import net.sf.extjwnl.dictionary.file.DictionaryFileFactory;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.dictionary.file.ObjectDictionaryFile;
import net.sf.extjwnl.util.factory.Param;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * <code>ObjectDictionaryFile</code> that accesses files names with the Princeton dictionary file naming convention.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class PrincetonObjectDictionaryFile extends AbstractPrincetonDictionaryFile implements ObjectDictionaryFile, DictionaryFileFactory<PrincetonObjectDictionaryFile> {

    private static final Log log = LogFactory.getLog(PrincetonObjectDictionaryFile.class);

    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    private FileInputStream fin = null;
    private FileOutputStream fout = null;

    public PrincetonObjectDictionaryFile(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary, params);
    }

    public PrincetonObjectDictionaryFile(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType, Map<String, Param> params) {
        super(dictionary, path, pos, fileType, params);
    }

    public PrincetonObjectDictionaryFile newInstance(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType) {
        return new PrincetonObjectDictionaryFile(dictionary, path, pos, fileType, params);
    }

    public boolean isOpen() {
        return (null != in || null != out);
    }

    public void save() throws IOException {
        if (dictionary instanceof MapBackedDictionary) {
            MapBackedDictionary dic = (MapBackedDictionary) dictionary;
            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("PRINCETON_INFO_004", getFilename()));
            }
            Map<Object, ? extends DictionaryElement> map = dic.getTable(getPOS(), getFileType());

            getOutputStream().reset();
            writeObject(map);

            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("PRINCETON_INFO_012", getFilename()));
            }
        }
    }

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
            super.close();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(JWNL.resolveMessage("EXCEPTION_001", e.getMessage()), e);
            }
        } finally {
            in = null;
            fin = null;
            out = null;
            fout = null;
        }
    }

    public void edit() throws IOException {
        openStreams();
    }

    /**
     * Open the input and output streams.
     *
     * @throws IOException IOException
     */
    public synchronized void openStreams() throws IOException {
        if (!canWrite()) {
            openOutputStream();
        }
        if (!canRead()) {
            openInputStream();
        }
    }

    private void openOutputStream() throws IOException {
        fout = new FileOutputStream(getFile());
        out = new ObjectOutputStream(fout);
    }

    private void openInputStream() throws IOException {
        fin = new FileInputStream(getFile());
        in = new ObjectInputStream(fin);
    }

    public ObjectInputStream getInputStream() throws IOException {
        if (!canRead()) {
            openInputStream();
        }
        return in;
    }

    public ObjectOutputStream getOutputStream() throws IOException {
        if (!canWrite()) {
            openOutputStream();
        }
        return out;
    }

    public boolean canRead() {
        return in != null;
    }

    public boolean canWrite() {
        return out != null;
    }

    public Object readObject() throws IOException, ClassNotFoundException {
        if (isOpen()) {
            if (canRead()) {
                return getInputStream().readObject();
            } else {
                return new HashMap<Object, DictionaryElement>();
            }
        } else {
            throw new JWNLRuntimeException("PRINCETON_EXCEPTION_001");
        }
    }

    public void writeObject(Object obj) throws IOException {
        if (isOpen() && canWrite()) {
            getOutputStream().writeObject(obj);
        } else {
            throw new JWNLRuntimeException("PRINCETON_EXCEPTION_002");
        }
    }

    /**
     * Here we try to be intelligent about opening streams.
     * If the file does not already exist, we assume that we are going
     * to be creating it and writing to it, otherwise we assume that
     * we are going to be reading from it. If you want the other stream
     * open, you must do it explicitly by calling <code>openStreams</code>.
     */
    protected void openFile() throws IOException {
        if (!file.exists()) {
            file.createNewFile();
            openOutputStream();
        } else {
            openInputStream();
        }
    }
}