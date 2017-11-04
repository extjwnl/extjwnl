package net.sf.extjwnl.princeton.file;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.JWNLIOException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.file.DictionaryFileFactory;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.util.factory.Param;

import java.io.*;
import java.util.Map;

/**
 * <code>ObjectDictionaryFile</code> that loads dictionary files from classpath.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 * @author Paul Landes
 */
public class PrincetonResourceObjectDictionaryFile extends AbstractPrincetonObjectDictionaryFile
        implements DictionaryFileFactory<PrincetonResourceObjectDictionaryFile> {

    /**
     * Factory constructor.
     *
     * @param dictionary dictionary
     * @param params     params
     */
    public PrincetonResourceObjectDictionaryFile(final Dictionary dictionary, final Map<String, Param> params) {
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
    private PrincetonResourceObjectDictionaryFile(final Dictionary dictionary,
                                                  final String path,
                                                  final POS pos,
                                                  final DictionaryFileType fileType,
                                                  final Map<String, Param> params) {
        super(dictionary, path, pos, fileType, params);
    }

    @Override
    public PrincetonResourceObjectDictionaryFile newInstance(final Dictionary dictionary,
                                                             final String path,
                                                             final POS pos,
                                                             final DictionaryFileType fileType) {
        return new PrincetonResourceObjectDictionaryFile(dictionary, path, pos, fileType, params);
    }

    protected void openOutputStream() throws JWNLException {
        throw new UnsupportedOperationException();
    }

    protected void openInputStream() throws JWNLException {
        try {
            final InputStream resourceStream =
                    PrincetonResourceObjectDictionaryFile.class.getResourceAsStream(path + "/" + getFilename());
            in = new ObjectInputStream(resourceStream);
        } catch (IOException e) {
            throw new JWNLIOException(e);
        }
    }

    @Override
    public void open() throws JWNLException {
        openInputStream();
    }
}
