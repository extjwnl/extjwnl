package net.didion.jwnl.princeton.file;

import net.didion.jwnl.JWNLRuntimeException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.dictionary.file.DictionaryFileFactory;
import net.didion.jwnl.dictionary.file.DictionaryFileType;
import net.didion.jwnl.dictionary.file.ObjectDictionaryFile;
import net.didion.jwnl.util.MessageLog;
import net.didion.jwnl.util.MessageLogLevel;
import net.didion.jwnl.util.factory.Param;

import java.io.*;
import java.util.Map;

/**
 * <code>ObjectDictionaryFile</code> that accesses files names with the Princeton dictionary file naming convention.
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class PrincetonObjectDictionaryFile extends AbstractPrincetonDictionaryFile implements ObjectDictionaryFile, DictionaryFileFactory<PrincetonObjectDictionaryFile> {

    private static final MessageLog log = new MessageLog(PrincetonObjectDictionaryFile.class);

    private File file = null;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;

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
        return (file != null);
    }

    public void save() {
        //TODO save
    }

    public void close() {
        try {
            if (canRead()) {
                getInputStream().close();
            }
            if (canWrite()) {
                getOutputStream().close();
            }
        } catch (Exception e) {
            log.log(MessageLogLevel.ERROR, "EXCEPTION_001", e.getMessage(), e);
        } finally {
            in = null;
            out = null;
            file = null;
        }
    }

    /**
     * Open the input and output streams.
     */
    public void openStreams() throws IOException {
        if (!canWrite()) {
            openOutputStream();
        }
        if (!canRead()) {
            openInputStream();
        }
    }

    private void openOutputStream() throws IOException {
        out = new ObjectOutputStream(new FileOutputStream(file));
    }

    private void openInputStream() throws IOException {
        in = new ObjectInputStream(new FileInputStream(file));
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
        if (isOpen() && canRead()) {
            return getInputStream().readObject();
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
    protected void openFile(File path) throws IOException {
        file = path;
        if (!file.exists()) {
            file.createNewFile();
            openOutputStream();
        } else {
            openInputStream();
        }
    }
}