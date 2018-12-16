package net.sf.extjwnl.princeton.file;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.JWNLIOException;
import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.file.DictionaryDiskFile;
import net.sf.extjwnl.dictionary.file.DictionaryFileFactory;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.util.ByteArrayCharSequence;
import net.sf.extjwnl.util.CharBufferCharSequence;
import net.sf.extjwnl.util.PointedCharSequence;
import net.sf.extjwnl.util.factory.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.*;

/**
 * A <code>RandomAccessDictionaryFile</code> that accesses files
 * named with Princeton's dictionary file naming convention.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class PrincetonRandomAccessDictionaryFile extends AbstractPrincetonRandomAccessDictionaryFile
        implements DictionaryFileFactory<PrincetonRandomAccessDictionaryFile>, DictionaryDiskFile {

    private static final Logger log = LoggerFactory.getLogger(PrincetonRandomAccessDictionaryFile.class);

    /**
     * Whether to add standard princeton header to files on save, default: false.
     */
    public static final String WRITE_PRINCETON_HEADER_KEY = "write_princeton_header";
    private boolean writePrincetonHeader = false;

    /**
     * Whether to warn about lex file numbers correctness, default: true.
     */
    public static final String CHECK_LEX_FILE_NUMBER_KEY = "check_lex_file_number";
    private boolean checkLexFileNumber = true;

    /**
     * Whether to warn about relation count being off limits, default: true.
     */
    public static final String CHECK_RELATION_LIMIT_KEY = "check_relation_limit";
    private boolean checkRelationLimit = true;

    /**
     * Whether to warn about word count being off limits, default: true.
     */
    public static final String CHECK_WORD_COUNT_LIMIT_KEY = "check_word_count_limit";
    private boolean checkWordCountLimit = true;

    /**
     * Whether to warn about lex id being off limits, default: true.
     */
    public static final String CHECK_LEX_ID_LIMIT_KEY = "check_lex_id_limit";
    private boolean checkLexIdLimit = true;

    /**
     * Whether to warn about pointer target indices being off limits, default: true
     */
    public static final String CHECK_POINTER_INDEX_LIMIT_KEY = "check_pointer_index_limit";
    private boolean checkPointerIndexLimit = true;

    /**
     * Whether to warn about verb frame indices being off limits, default: true
     */
    public static final String CHECK_VERB_FRAME_LIMIT_KEY = "check_verb_frame_limit";
    private boolean checkVerbFrameLimit = true;

    /**
     * Whether to warn about data file line length being off limits, default: true. The line length was
     * found empirically by testing wnb.exe for crashes. It equals 15360.
     */
    public static final String CHECK_DATA_FILE_LINE_LENGTH_LIMIT_KEY = "check_data_file_line_length_limit";
    private static final int dataFileLineLengthLimit = 15360;
    private boolean checkDataFileLineLengthLimit = true;

    private static final String PRINCETON_HEADER_HEAD = "  1 This software and database is being provided to you, the LICENSEE, by  \n" +
            "  2 Princeton University under the following license.  By obtaining, using  \n" +
            "  3 and/or copying this software and database, you agree that you have  \n" +
            "  4 read, understood, and will comply with these terms and conditions.:  \n" +
            "  5   \n" +
            "  6 Permission to use, copy, modify and distribute this software and  \n" +
            "  7 database and its documentation for any purpose and without fee or  \n" +
            "  8 royalty is hereby granted, provided that you agree to comply with  \n" +
            "  9 the following copyright notice and statements, including the disclaimer,  \n" +
            "  10 and that the same appear on ALL copies of the software, database and  \n" +
            "  11 documentation, including modifications that you make for internal  \n" +
            "  12 use or for distribution.  \n" +
            "  13   \n";

    private static final String PRINCETON_HEADER_21 =
            "  14 WordNet 2.1 Copyright 2005 by Princeton University.  All rights reserved.  \n";
    private static final String PRINCETON_HEADER_30 =
            "  14 WordNet 3.0 Copyright 2006 by Princeton University.  All rights reserved.  \n";
    private static final String PRINCETON_HEADER_31 =
            "  14 WordNet 3.1 Copyright 2011 by Princeton University.  All rights reserved.  \n";

    private static final String PRINCETON_HEADER_TAIL =
            "  15   \n" +
            "  16 THIS SOFTWARE AND DATABASE IS PROVIDED \"AS IS\" AND PRINCETON  \n" +
            "  17 UNIVERSITY MAKES NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR  \n" +
            "  18 IMPLIED.  BY WAY OF EXAMPLE, BUT NOT LIMITATION, PRINCETON  \n" +
            "  19 UNIVERSITY MAKES NO REPRESENTATIONS OR WARRANTIES OF MERCHANT-  \n" +
            "  20 ABILITY OR FITNESS FOR ANY PARTICULAR PURPOSE OR THAT THE USE  \n" +
            "  21 OF THE LICENSED SOFTWARE, DATABASE OR DOCUMENTATION WILL NOT  \n" +
            "  22 INFRINGE ANY THIRD PARTY PATENTS, COPYRIGHTS, TRADEMARKS OR  \n" +
            "  23 OTHER RIGHTS.  \n" +
            "  24   \n" +
            "  25 The name of Princeton University or Princeton may not be used in  \n" +
            "  26 advertising or publicity pertaining to distribution of the software  \n" +
            "  27 and/or database.  Title to copyright in this software, database and  \n" +
            "  28 any associated documentation shall at all times remain with  \n" +
            "  29 Princeton University and LICENSEE agrees to preserve same.  \n";

    private static final long PRINCETON_HEADER_LENGTH =
            PRINCETON_HEADER_HEAD.length() + PRINCETON_HEADER_21.length() + PRINCETON_HEADER_TAIL.length();

    /**
     * Read-only file permission.
     */
    private static final String READ_ONLY = "r";

    /**
     * Read-write file permission.
     */
    private static final String READ_WRITE = "rw";

    private final static char[] digits = {
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z'
    };

    private final static char[] DigitTens = {
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
            '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
            '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
            '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
            '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
            '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
            '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
            '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
    };

    private final static char[] DigitOnes = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    };

    private final static Comparator<Synset> synsetOffsetComparator = (o1, o2) -> (int) (o1.getOffset() - o2.getOffset());

    protected final File file;
    protected RandomAccessFile raFile;
    protected long raFileLength;

    private final Charset charset;
    private final CharsetDecoder decoder;
    private final CharsetEncoder encoder;
    private char[] chars;
    private ByteBuffer bytes;
    private byte[] bytesBacker;

    private int offsetLength;
    private long firstLineOffset;

    public static void formatOffset(long i, int formatLength, StringBuilder target) {
        int lastIdx = target.length();
        target.setLength(target.length() + formatLength);

        // lifted from Long.java
        long q;
        int r;
        int charPos = lastIdx + formatLength;

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i > Integer.MAX_VALUE && 0 < charPos) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = (int) (i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            target.setCharAt(--charPos, DigitOnes[r]);
            target.setCharAt(--charPos, DigitTens[r]);
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int) i;
        while (i2 >= 65536 && 0 < charPos) {
            q2 = i2 / 100;
            // really: r = i2 - (q * 100);
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
            i2 = q2;
            target.setCharAt(--charPos, DigitOnes[r]);
            target.setCharAt(--charPos, DigitTens[r]);
        }

        // Fall through to fast mode for smaller numbers
        // assert(i2 <= 65536, i2);
        while (0 < charPos) {
            q2 = (i2 * 52429) >>> (16 + 3);
            r = i2 - ((q2 << 3) + (q2 << 1));  // r = i2-(q2*10) ...
            target.setCharAt(--charPos, digits[r]);
            i2 = q2;
            if (i2 == 0) {
                break;
            }
        }

        while (lastIdx < charPos && 0 < charPos) {
            target.setCharAt(--charPos, '0');
        }
    }

    /**
     * Factory constructor.
     *
     * @param dictionary dictionary
     * @param params     params
     */
    public PrincetonRandomAccessDictionaryFile(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary, params);
        this.file = null;
        this.charset = null;
        this.decoder = null;
        this.encoder = null;
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
    public PrincetonRandomAccessDictionaryFile(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType, Map<String, Param> params) {
        super(dictionary, path, pos, fileType, params);
        this.firstLineOffset = -1;
        this.offsetLength = -1;
        this.file = new File(path, getFilename());
        if (null != encoding) {
            this.charset = Charset.forName(encoding);
        } else {
            this.charset = StandardCharsets.US_ASCII;
        }
        this.decoder = charset.newDecoder();
        this.encoder = charset.newEncoder();

        if (params.containsKey(WRITE_PRINCETON_HEADER_KEY)) {
            writePrincetonHeader = Boolean.parseBoolean(params.get(WRITE_PRINCETON_HEADER_KEY).getValue());
        }
        if (params.containsKey(CHECK_LEX_FILE_NUMBER_KEY)) {
            checkLexFileNumber = Boolean.parseBoolean(params.get(CHECK_LEX_FILE_NUMBER_KEY).getValue());
        }
        if (params.containsKey(CHECK_RELATION_LIMIT_KEY)) {
            checkRelationLimit = Boolean.parseBoolean(params.get(CHECK_RELATION_LIMIT_KEY).getValue());
        }
        if (params.containsKey(CHECK_WORD_COUNT_LIMIT_KEY)) {
            checkWordCountLimit = Boolean.parseBoolean(params.get(CHECK_WORD_COUNT_LIMIT_KEY).getValue());
        }
        if (params.containsKey(CHECK_LEX_ID_LIMIT_KEY)) {
            checkLexIdLimit = Boolean.parseBoolean(params.get(CHECK_LEX_ID_LIMIT_KEY).getValue());
        }
        if (params.containsKey(CHECK_POINTER_INDEX_LIMIT_KEY)) {
            checkPointerIndexLimit = Boolean.parseBoolean(params.get(CHECK_POINTER_INDEX_LIMIT_KEY).getValue());
        }
        if (params.containsKey(CHECK_VERB_FRAME_LIMIT_KEY)) {
            checkVerbFrameLimit = Boolean.parseBoolean(params.get(CHECK_VERB_FRAME_LIMIT_KEY).getValue());
        }
        if (params.containsKey(CHECK_DATA_FILE_LINE_LENGTH_LIMIT_KEY)) {
            checkDataFileLineLengthLimit = Boolean.parseBoolean(params.get(CHECK_DATA_FILE_LINE_LENGTH_LIMIT_KEY).getValue());
        }
    }

    @Override
    public PrincetonRandomAccessDictionaryFile newInstance(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType) {
        return new PrincetonRandomAccessDictionaryFile(dictionary, path, pos, fileType, params);
    }

    @Override
    public PointedCharSequence readLine(long offset) throws JWNLException {
        if (isInvalidOffset(offset)) return null;

        // max line values
        // exc files - 46
        // cntlists - 56, 97% of lines is covered by buffer length 35
        // data.adj - 2794, 92% - 300
        // data.adv - 638, 95% - 250
        // data.noun - 12972, 97% - 400
        // data.verb - 7713, 95% - 400
        // index.adj - 272, 97% - 60
        // index.adv - 139, 98% - 60
        // index.noun - 331, 97% - 60
        // index.verb - 567, 95% - 80
        // index.sense - 94, 99.9% - 60
        int LINE_MAX = 64;
        if (DictionaryFileType.DATA == fileType) {
            LINE_MAX = 512;
        }

        byte[] line = new byte[LINE_MAX];
        int i = 0;
        try {
            final long tailLength = raFileLength - offset;
            out:
            while (i < tailLength) {
                if (line.length == i) {
                    byte[] t = new byte[line.length * 2];
                    System.arraycopy(line, 0, t, 0, line.length);
                    line = t;
                }
                int readBytes = line.length - i;
                if (readBytes > tailLength - i) {
                    readBytes = (int) tailLength - i;
                }

                synchronized (this) {
                    raFile.seek(offset + i);
                    readBytes = i + raFile.read(line, i, readBytes);
                }

                while (i < readBytes) {
                    if ('\n' == line[i]) {
                        break out;
                    }
                    i++;
                }
            }
        } catch (IOException e) {
            throw new JWNLIOException(e);
        }

        // resulting line ends at i (eol or eof)
        return getPointedCharSequence(offset, line, i);
    }

    @Override
    public PointedCharSequence readWord(long offset) throws JWNLException {
        if (isInvalidOffset(offset)) return null;

        int LINE_MAX = 32;

        byte[] line = new byte[LINE_MAX];
        int i = 0;
        try {
            final long tailLength = raFileLength - offset;
            out:
            while (i < tailLength) {
                if (line.length == i) {
                    byte[] t = new byte[line.length * 2];
                    System.arraycopy(line, 0, t, 0, line.length);
                    line = t;
                }
                int readBytes = line.length - i;
                if (readBytes > tailLength - i) {
                    readBytes = (int) tailLength - i;
                }

                synchronized (this) {
                    raFile.seek(offset + i);
                    readBytes = i + raFile.read(line, i, readBytes);
                }

                while (i < readBytes) {
                    if (' ' == line[i] || '\n' == line[i]) {
                        break out;
                    }
                    i++;
                }
            }
        } catch (IOException e) {
            throw new JWNLIOException(e);
        }

        // resulting word ends at i (space, eol or eof)
        return getPointedCharSequence(offset, line, i);
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

                    long offset = 0;

                    int LINE_MAX = 2048;

                    byte[] line = new byte[LINE_MAX];
                    int i = 0;
                    boolean eol = true;
                    try {
                        final long tailLength = raFileLength;
                        out:
                        while (i < tailLength) {
                            if (line.length == i) {
                                byte[] t = new byte[line.length * 2];
                                System.arraycopy(line, 0, t, 0, line.length);
                                line = t;
                            }
                            int readBytes = line.length - i;
                            if (readBytes > tailLength - i) {
                                readBytes = (int) tailLength - i;
                            }

                            raFile.seek(offset + i);
                            readBytes = i + raFile.read(line, i, readBytes);

                            while (i < readBytes) {
                                if (eol && ' ' != line[i]) {
                                    break out;
                                }
                                eol = '\n' == line[i];
                                i++;
                            }
                        }
                    } catch (IOException e) {
                        throw new JWNLIOException(e);
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

        if (offset >= raFileLength || offset < 0) {
            return -1;
        }

        int LINE_MAX = 64;
        if (DictionaryFileType.DATA == fileType) {
            LINE_MAX = 512;
        }

        byte[] line = new byte[LINE_MAX];
        int i = 0;
        try {
            final long tailLength = raFileLength - offset;
            out:
            while (i < tailLength) {
                if (line.length == i) {
                    byte[] t = new byte[line.length * 2];
                    System.arraycopy(line, 0, t, 0, line.length);
                    line = t;
                }
                int readBytes = line.length - i;
                if (readBytes > tailLength - i) {
                    readBytes = (int) tailLength - i;
                }

                synchronized (this) {
                    raFile.seek(offset + i);
                    readBytes = i + raFile.read(line, i, readBytes);
                }

                while (i < readBytes) {
                    if ('\n' == line[i]) {
                        break out;
                    }
                    i++;
                }
            }
        } catch (IOException e) {
            throw new JWNLIOException(e);
        }
        // we've read the line

        long result = offset + i + 1;
        if (result >= raFileLength) {
            result = -1;
        }

        return result;
    }

    @Override
    public void open() throws JWNLException {
        synchronized (this) {
            if (!isOpen()) {
                try {

                    /*
                     * Here we try to be intelligent about opening files.
                     * If the file exists, we assume that we are going to be reading from it,
                     * otherwise we assume that we are going to be creating it and writing to it.
                     */
                    if (file.exists()) {
                        raFile = new RandomAccessFile(file, READ_ONLY);
                    } else {
                        raFile = new RandomAccessFile(file, READ_WRITE);
                    }
                    raFileLength = raFile.length();
                } catch (IOException e) {
                    throw new JWNLIOException(e);
                }
            }
        }
    }

    @Override
    public boolean isOpen() {
        return raFile != null;
    }

    @Override
    public void close() throws JWNLException {
        synchronized (this) {
            try {
                if (null != raFile) {
                    raFile.close();
                }
            } catch (IOException e) {
                throw new JWNLIOException(e);
            } finally {
                raFile = null;
            }
        }
    }

    @Override
    public void edit() throws JWNLException {
        synchronized (this) {
            try {
                raFile.close();
                raFile = new RandomAccessFile(file, READ_WRITE);
            } catch (IOException e) {
                throw new JWNLIOException(e);
            }
        }
    }

    @Override
    public long length() throws JWNLException {
        return raFileLength;
    }

    @Override
    public void save() throws JWNLException {
        if (log.isDebugEnabled()) {
            log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_004", getFilename()));
        }

        try {
            initBuffers();

            if (DictionaryFileType.EXCEPTION == fileType) {
                ArrayList<String> exceptions = new ArrayList<>();
                Iterator<Exc> ei = dictionary.getExceptionIterator(getPOS());
                StringBuilder sb = new StringBuilder(512);
                while (ei.hasNext()) {
                    sb.delete(0, sb.length());
                    renderException(ei.next(), sb);
                    exceptions.add(sb.toString());
                }
                if (log.isDebugEnabled()) {
                    log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_005", exceptions.size()));
                }
                Collections.sort(exceptions);

                synchronized (this) {
                    raFile.seek(0);
                    writeStrings(exceptions);
                    truncate();
                }
            } else if (DictionaryFileType.DATA == fileType) {
                ArrayList<Synset> synsets = new ArrayList<>();
                Iterator<Synset> si = dictionary.getSynsetIterator(getPOS());
                while (si.hasNext()) {
                    synsets.add(si.next());
                }

                if (log.isDebugEnabled()) {
                    log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_005", synsets.size()));
                }
                // dictionary is cached in a hashmap, synset order is not guaranteed
                synsets.sort(synsetOffsetComparator);

                if (log.isDebugEnabled()) {
                    log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_007", synsets.size()));
                }
                if (log.isDebugEnabled()) {
                    log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_008", getFilename()));
                }
                long counter = 0;
                long total = synsets.size();
                long reportInt = (total / 20) + 1; // i.e. report every 5%
                synchronized (this) {
                    raFile.seek(0);
                    writePrincetonHeader();
                    if (log.isDebugEnabled()) {
                        log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_021", getFilename()));
                    }
                    StringBuilder s = new StringBuilder(16 * 1024);
                    for (Synset synset : synsets) {
                        counter++;
                        if (0 == (counter % reportInt)) {
                            if (log.isDebugEnabled()) {
                                log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_014", 100 * counter / total));
                            }
                        }

                        s.delete(0, s.length());
                        renderSynset(synset, s);
                        writeLine(s);
                    }
                    truncate();
                }
                if (log.isDebugEnabled()) {
                    log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_009", getFilename()));
                }
            } else if (DictionaryFileType.INDEX == fileType) {
                ArrayList<String> indexes = new ArrayList<>();

                if (log.isDebugEnabled()) {
                    log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_011", getFilename()));
                }
                Iterator<IndexWord> ii = dictionary.getIndexWordIterator(getPOS());
                StringBuilder sb = new StringBuilder(512);
                while (ii.hasNext()) {
                    sb.delete(0, sb.length());
                    renderIndexWord(ii.next(), sb);
                    indexes.add(sb.toString());
                }

                if (log.isDebugEnabled()) {
                    log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_005", indexes.size()));
                }
                Collections.sort(indexes);
                synchronized (this) {
                    raFile.seek(0);
                    writePrincetonHeader();
                    writeIndexStrings(indexes);
                    truncate();
                }
            } else if (DictionaryFileType.REVCNTLIST == fileType) {
                ArrayList<Word> toRender = collectWordsToRender();

                // sort by sense key
                toRender.sort((o1, o2) -> {
                    try {
                        return o1.getSenseKeyWithAdjClass().compareTo(o2.getSenseKeyWithAdjClass());
                    } catch (JWNLException e) {
                        throw new JWNLRuntimeException(e);
                    }
                });


                raFile.seek(0);
                if (log.isDebugEnabled()) {
                    log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_008", getFilename()));
                }
                long counter = 0;
                long total = toRender.size();
                long reportInt = (total / 20) + 1; // i.e. report every 5%
                StringBuilder s = new StringBuilder(100);
                for (Word word : toRender) {
                    counter++;
                    if (0 == (counter % reportInt)) {
                        if (log.isDebugEnabled()) {
                            log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_014", 100 * counter / total));
                        }
                    }

                    s.delete(0, s.length());

                    // sense_key  sense_number  tag_cnt
                    s.append(word.getSenseKeyWithAdjClass()).
                            append(' ').append(word.getSenseNumber()).
                            append(' ').append(word.getUseCount());

                    writeLine(s);
                }
                truncate();
                if (log.isDebugEnabled()) {
                    log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_013", getFilename()));
                }
            } else if (DictionaryFileType.CNTLIST == fileType) {
                ArrayList<Word> toRender = collectWordsToRender();

                // sort by count in descending order
                toRender.sort((o1, o2) -> {
                    int result = o2.getUseCount() - o1.getUseCount();
                    if (0 == result) {
                        try {
                            result = o2.getSenseKeyWithAdjClass().compareTo(o1.getSenseKeyWithAdjClass());
                        } catch (JWNLException e) {
                            throw new JWNLRuntimeException(e);
                        }
                    }
                    return result;
                });

                raFile.seek(0);
                if (log.isDebugEnabled()) {
                    log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_008", getFilename()));
                }
                long counter = 0;
                long total = toRender.size();
                long reportInt = (total / 20) + 1; // i.e. report every 5%
                StringBuilder s = new StringBuilder(100);
                for (Word word : toRender) {
                    counter++;
                    if (0 == (counter % reportInt)) {
                        if (log.isDebugEnabled()) {
                            log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_014", 100 * counter / total));
                        }
                    }

                    s.delete(0, s.length());

                    // tag_cnt  sense_key  sense_number
                    s.append(word.getUseCount()).
                            append(' ').append(word.getSenseKeyWithAdjClass()).
                            append(' ').append(word.getSenseNumber());

                    writeLine(s);

                    // WN TRICK
                    // differences in re-rendering of cntlist are due to discrepancies(?) in wordnet files
                    // below from 2.1 (more in 3.0 and 3.1)
                    // e.g. 232 world%1:17:01:: 2 VS 232 world%1:17:01:: 1
                    //      original                 re-render
                    // however,
                    // world n 8 6 @ ~ #m %m %p + 8 7 09330440 05738136 07857779 09138104 05600868 09344389 08066556 02450463
                    // world%1:17:01:: corresponds to
                    // 09330440 17 n 06 universe 0 existence 0 creation 0 world 1 cosmos 0 macrocosm 0 011 @ 00018635 n 0000 + 02987805 a 0601 + 01437993 a 0501 + 02789059 a 0501 + 00553777 a 0105 %m 08157282 n 0000 %p 09107111 n 0000 ~ 09114410 n 0000 %p 09144850 n 0000 ~ 09232164 n 0000 ~ 09232329 n 0000 | everything that exists anywhere; "they study the evolution of the universe"; "the biggest tree in existence"
                    //          ^^                                        ^^^^^^^
                    // which is sense number 1, not 2.
                    // sense number 2 is
                    // 05738136 09 n 02 world 0 reality 0 002 @ 05908392 n 0000 ~ 05738508 n 0000 | all of your experiences that determine how things appear to you; "his world was shattered"; "we live in different worlds"; "for them demons were as much a part of reality as trees were"
                    //          ^^      ^^^^^^^
                    // that is "156 world%1:09:00:: 2" (2 and not 3: "156 world%1:09:00:: 3")
                    // most differing indices are off-by-one
                    // some are off-by-two or more

                    // ussr%1:15:00:: is duplicated in original wordnet 2.1  files

                    // 7 old%5:00:00:preceding:00 9
                    // and
                    // 5 small%5:00:00:little:03 13
                    // are discrepancies in adjective satellite clusters
                    // e.g. (a) is missing from the key
                }
                truncate();
                if (log.isDebugEnabled()) {
                    log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_013", getFilename()));
                }
            } else if (DictionaryFileType.SENSEINDEX == fileType) {
                Set<String> senseIndexContent = new TreeSet<>();
                StringBuilder result = new StringBuilder(100);
                for (POS pos : POS.getAllPOS()) {
                    Iterator<IndexWord> ii = dictionary.getIndexWordIterator(pos);
                    while (ii.hasNext()) {
                        IndexWord iw = ii.next();
                        for (int i = 0; i < iw.getSenses().size(); i++) {
                            Synset synset = iw.getSenses().get(i);
                            for (Word w : synset.getWords()) {
                                if (w.getLemma().equalsIgnoreCase(iw.getLemma())) {
                                    result.delete(0, result.length());

                                    // sense_key  synset_offset  sense_number  tag_cnt
                                    result.append(w.getSenseKey()).append(" ");
                                    formatOffset(synset.getOffset(), offsetLength, result);
                                    result.append(" ");
                                    result.append(i + 1);
                                    result.append(" ");
                                    result.append(w.getUseCount());
                                    senseIndexContent.add(result.toString());
                                }
                            }
                        }
                    }
                }

                raFile.seek(0);
                writeStrings(senseIndexContent);
                truncate();
            }
        } catch (IOException e) {
            throw new JWNLIOException(e);
        } finally {
            freeBuffers();
        }

        if (log.isDebugEnabled()) {
            log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_012", getFilename()));
        }
    }

    private void truncate() throws IOException {
        raFile.setLength(raFile.getFilePointer());
        raFileLength = raFile.length();
    }

    private ArrayList<Word> collectWordsToRender() throws JWNLException {
        ArrayList<Word> result = new ArrayList<>();
        Set<String> renderedKeys = new HashSet<>();
        for (POS pos : POS.getAllPOS()) {
            Iterator<IndexWord> ii = dictionary.getIndexWordIterator(pos);
            while (ii.hasNext()) {
                IndexWord iw = ii.next();
                for (int i = 0; i < iw.getSenses().size(); i++) {
                    for (Word w : iw.getSenses().get(i).getWords()) {
                        if (0 < w.getUseCount()) {
                            String key = w.getSenseKeyWithAdjClass();
                            if (!renderedKeys.contains(key)) {
                                result.add(w);
                                renderedKeys.add(key);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public int getOffsetLength() throws JWNLException {
        if (DictionaryFileType.DATA == fileType) {
            ArrayList<Synset> synsets = new ArrayList<>();
            Iterator<Synset> si = dictionary.getSynsetIterator(getPOS());
            while (si.hasNext()) {
                synsets.add(si.next());
            }

            if (log.isDebugEnabled()) {
                log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_005", synsets.size()));
            }
            synsets.sort(synsetOffsetComparator);

            initBuffers();

            offsetLength = 8;
            int offsetDigitCount = 8; // 8 by default for WN compatibility
            do {
                if (offsetLength < offsetDigitCount) {
                    offsetLength = offsetDigitCount;
                    if (log.isWarnEnabled()) {
                        log.warn(dictionary.getMessages().resolveMessage("PRINCETON_WARN_010", offsetLength));
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_006", new Object[]{synsets.size(), getFilename()}));
                }
                long offset = 0;
                if (writePrincetonHeader) {
                    offset = offset + PRINCETON_HEADER_LENGTH;
                }
                long safeOffset = Integer.MAX_VALUE - 1;
                StringBuilder sb = new StringBuilder(16 * 1024);
                for (Synset s : synsets) {
                    sb.delete(0, sb.length());
                    renderSynset(s, sb);
                    Synset oldSynset = dictionary.getSynsetAt(s.getPOS(), offset);
                    if (null != oldSynset) {
                        oldSynset.setOffset(safeOffset);
                        safeOffset--;
                    }
                    s.setOffset(offset);
                    offset = offset + getByteLength(sb) + 1;//\n should be 1 byte
                }

                // calculate used offset length
                offsetDigitCount = Math.max(offsetLength, getDigitCount(offset));
            } while (offsetLength < offsetDigitCount);

            freeBuffers();
        } else if (DictionaryFileType.INDEX == fileType) {
            Iterator<IndexWord> ii = dictionary.getIndexWordIterator(getPOS());
            long maxOffset = 0;
            while (ii.hasNext()) {
                IndexWord indexWord = ii.next();
                for (Synset synset : indexWord.getSenses()) {
                    if (maxOffset < synset.getOffset()) {
                        maxOffset = synset.getOffset();
                    }
                }
            }
            offsetLength = Math.max(8, getDigitCount(maxOffset));
        }
        return offsetLength;
    }

    @Override
    public void setOffsetLength(int length) throws JWNLException {
        if (offsetLength != length) {
            offsetLength = length;

            // recalculate offsets which might change due to changed offset length
            if (DictionaryFileType.DATA == fileType) {
                ArrayList<Synset> synsets = new ArrayList<>();
                Iterator<Synset> si = dictionary.getSynsetIterator(getPOS());
                while (si.hasNext()) {
                    synsets.add(si.next());
                }

                if (log.isInfoEnabled()) {
                    log.info(dictionary.getMessages().resolveMessage("PRINCETON_INFO_005", synsets.size()));
                }
                synsets.sort(synsetOffsetComparator);

                if (log.isInfoEnabled()) {
                    log.info(dictionary.getMessages().resolveMessage("PRINCETON_INFO_006", new Object[]{synsets.size(), getFilename()}));
                }
                long offset = 0;
                if (writePrincetonHeader) {
                    offset = offset + PRINCETON_HEADER_LENGTH;
                }
                long safeOffset = Integer.MAX_VALUE - 1;
                StringBuilder sb = new StringBuilder(16 * 1024);
                initBuffers();
                for (Synset s : synsets) {
                    sb.delete(0, sb.length());
                    renderSynset(s, sb);
                    Synset oldSynset = dictionary.getSynsetAt(s.getPOS(), offset);
                    if (null != oldSynset) {
                        oldSynset.setOffset(safeOffset);
                        safeOffset--;
                    }
                    s.setOffset(offset);
                    offset = offset + getByteLength(sb) + 1;//\n should be 1 byte
                }
                freeBuffers();
            }
        }
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public boolean delete() throws JWNLException {
        close();
        return file.delete();
    }

    protected PointedCharSequence getPointedCharSequence(long offset, byte[] line, int i) throws JWNLIOException {
        final PointedCharSequence result;

        if (null == encoding) {
            result = new ByteArrayCharSequence(line, 0, i, offset + i);
        } else {
            final ByteBuffer bb = ByteBuffer.wrap(line, 0, i);

            try {
                synchronized (this) {
                    CharBuffer cb = decoder.decode(bb);
                    result = new CharBufferCharSequence(cb, offset + i);
                }
            } catch (CharacterCodingException e) {
                throw new JWNLIOException(dictionary.getMessages().resolveMessage("PRINCETON_EXCEPTION_003",
                        new Object[]{getFilename(), offset}), e);
            }
        }
        return result;
    }

    protected boolean isInvalidOffset(long offset) throws JWNLException {
        if (!isOpen()) {
            throw new JWNLException(dictionary.getMessages().resolveMessage("PRINCETON_EXCEPTION_001"));
        }

        if (offset >= raFileLength || offset < 0) {
            return true;
        }
        return false;
    }

    protected void writePrincetonHeader() throws IOException {
        if (writePrincetonHeader) {
            if (log.isDebugEnabled()) {
                log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_020", getFilename()));
            }
            raFile.write(PRINCETON_HEADER_HEAD.getBytes(charset));
            if (dictionary.getVersion().getNumber() > 3.05) {
                raFile.write(PRINCETON_HEADER_31.getBytes(charset));
            } else if (dictionary.getVersion().getNumber() > 2.9) {
                raFile.write(PRINCETON_HEADER_30.getBytes(charset));
            } else {
                raFile.write(PRINCETON_HEADER_21.getBytes(charset));
            }
            raFile.write(PRINCETON_HEADER_TAIL.getBytes(charset));
        }
    }

    private void writeLine(String line) throws JWNLException {
        synchronized (this) {
            try {
                if (checkDataFileLineLengthLimit && line.length() > dataFileLineLengthLimit) {
                    if (log.isWarnEnabled()) {
                        log.warn(dictionary.getMessages().resolveMessage("PRINCETON_WARN_009",
                                new Object[]{getFilename(), dataFileLineLengthLimit, line.length()}));
                    }
                }

                if (chars.length < line.length()) {
                    chars = new char[line.length()];
                }
                line.getChars(0, line.length(), chars, 0);
                CharBuffer lchars = CharBuffer.wrap(chars);
                lchars.limit(line.length());

                CoderResult coderResult;
                do {
                    bytes.clear();
                    coderResult = encoder.encode(lchars, bytes, true);

                    if (coderResult.isError()) {
                        if (log.isWarnEnabled()) {
                            log.warn(dictionary.getMessages().resolveMessage("PRINCETON_WARN_011",
                                    new Object[]{lchars.toString()}));
                        }
                    }
                    raFile.write(bytesBacker, 0, bytes.position());
                } while (coderResult.isOverflow());
                raFile.writeByte((byte) '\n');
            } catch (IOException e) {
                throw new JWNLIOException(e);
            }
        }
    }

    private void writeLine(StringBuilder line) throws JWNLException {
        synchronized (this) {
            try {
                if (checkDataFileLineLengthLimit && line.length() > dataFileLineLengthLimit) {
                    if (log.isWarnEnabled()) {
                        log.warn(dictionary.getMessages().resolveMessage("PRINCETON_WARN_009",
                                new Object[]{getFilename(), dataFileLineLengthLimit, bytes.position()}));
                    }
                }

                if (chars.length < line.length()) {
                    chars = new char[line.length()];
                }
                line.getChars(0, line.length(), chars, 0);
                CharBuffer lchars = CharBuffer.wrap(chars);
                lchars.limit(line.length());

                CoderResult coderResult;
                do {
                    bytes.clear();
                    coderResult = encoder.encode(lchars, bytes, true);

                    if (coderResult.isError()) {
                        if (log.isWarnEnabled()) {
                            log.warn(dictionary.getMessages().resolveMessage("PRINCETON_WARN_011",
                                    new Object[]{lchars.toString()}));
                        }
                    }
                    raFile.write(bytesBacker, 0, bytes.position());
                } while (coderResult.isOverflow());
                raFile.writeByte((byte) '\n');
            } catch (IOException e) {
                throw new JWNLIOException(e);
            }
        }
    }

    private long getByteLength(StringBuilder line) {
        long result = 0;

        if (chars.length < line.length()) {
            chars = new char[line.length()];
        }
        line.getChars(0, line.length(), chars, 0);
        CharBuffer lchars = CharBuffer.wrap(chars);
        lchars.limit(line.length());

        CoderResult coderResult;
        do {
            bytes.clear();
            coderResult = encoder.encode(lchars, bytes, true);
            result = result + bytes.position();
        } while (coderResult.isOverflow());

        return result;
    }

    private void writeStrings(Collection<String> strings) throws JWNLException {
        synchronized (this) {
            if (log.isDebugEnabled()) {
                log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_008", getFilename()));
            }
            long counter = 0;
            long total = strings.size();
            long reportInt = (total / 20) + 1; // i.e. report every 5%
            for (String s : strings) {
                counter++;
                if (0 == (counter % reportInt)) {
                    if (log.isDebugEnabled()) {
                        log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_014", 100 * counter / total));
                    }
                }
                writeLine(s);
            }
            if (log.isDebugEnabled()) {
                log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_013", getFilename()));
            }
        }
    }

    private void writeIndexStrings(ArrayList<String> strings) throws JWNLException {
        synchronized (this) {
            if (log.isDebugEnabled()) {
                log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_008", getFilename()));
            }
            long counter = 0;
            long total = strings.size();
            long reportInt = (total / 20) + 1; // i.e. report every 5%
            //see makedb.c FixLastRecord
            /* Funky routine to pad the second to the last record of the
             index file to be longer than the last record so the binary
             search in the search code works properly. */
            for (int i = 0; i < strings.size() - 2; i++) {
                counter++;
                if (0 == (counter % reportInt)) {
                    if (log.isDebugEnabled()) {
                        log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_014", 100 * counter / total));
                    }
                }
                writeLine(strings.get(i));
            }
            if (1 < strings.size()) {
                StringBuilder nextToLast = new StringBuilder(strings.get(strings.size() - 2));
                String last = strings.get(strings.size() - 1);
                while (nextToLast.length() <= last.length()) {
                    nextToLast.append(' ');
                }
                writeLine(nextToLast);
                writeLine(last);
            } else if (1 == strings.size()) {
                String last = strings.get(strings.size() - 1);
                writeLine(last);
            }
            if (log.isDebugEnabled()) {
                log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_014", 100));
            }
            if (log.isDebugEnabled()) {
                log.debug(dictionary.getMessages().resolveMessage("PRINCETON_INFO_013", getFilename()));
            }
        }
    }

    private int getDigitCount(long number) {
        return (number == 0) ? 1 : (int) Math.log10(number) + 1;
    }

    private void renderSynset(Synset synset, StringBuilder result) throws JWNLException {
        // synset_offset lex_filenum ss_type w_cnt word lex_id [word lex_id...] p_cnt [ptr...] [frames...] | gloss
        // w_cnt Two digit hexadecimal integer indicating the number of words in the synset.
        String posKey = synset.getPOS().getKey();
        if (POS.ADJECTIVE == synset.getPOS() && synset.isAdjectiveCluster()) {
            posKey = POS.ADJECTIVE_SATELLITE_KEY;
        }
        if (checkLexFileNumber && log.isWarnEnabled() && !LexFileIdFileNameMap.getMap().containsKey(synset.getLexFileNum())) {
            log.warn(dictionary.getMessages().resolveMessage("PRINCETON_WARN_001", synset.getLexFileNum()));
        }
        if (checkWordCountLimit && log.isWarnEnabled() && (0xFF < synset.getWords().size())) {
            log.warn(dictionary.getMessages().resolveMessage("PRINCETON_WARN_004",
                    new Object[]{synset.getOffset(), synset.getWords().size()}));
        }

        formatOffset(synset.getOffset(), offsetLength, result);
        if (synset.getLexFileNum() < 10) {
            result.append(" 0").append(synset.getLexFileNum());
        } else {
            result.append(' ').append(synset.getLexFileNum());
        }
        result.append(' ').append(posKey);
        if (synset.getWords().size() < 0x10) {
            result.append(" 0").append(Integer.toHexString(synset.getWords().size())).append(' ');
        } else {
            result.append(' ').append(Integer.toHexString(synset.getWords().size())).append(' ');
        }
        for (Word w : synset.getWords()) {
            // ASCII form of a word as entered in the synset by the lexicographer,
            // with spaces replaced by underscore characters (_). The text of the word is case sensitive.

            // lex_id One digit hexadecimal integer that, when appended onto lemma,
            // uniquely identifies a sense within a lexicographer file.
            String lemma = w.getLemma().replace(' ', '_');
            if (w instanceof Adjective) {
                Adjective a = (Adjective) w;
                if (AdjectivePosition.NONE != a.getAdjectivePosition()) {
                    lemma = lemma + "(" + a.getAdjectivePosition().getKey() + ")";
                }
            }
            if (checkLexIdLimit && log.isWarnEnabled() && (0xF < w.getLexId())) {
                log.warn(dictionary.getMessages().resolveMessage("PRINCETON_WARN_005",
                        new Object[]{synset.getOffset(), w.getLemma(), w.getLexId()}));
            }
            result.append(lemma).append(' ');
            result.append(Long.toHexString(w.getLexId())).append(' ');
        }
        // Three digit decimal integer indicating the number of pointers from this synset to other synsets.
        // If p_cnt is 000 the synset has no pointers.
        if (checkRelationLimit && log.isWarnEnabled() && (999 < synset.getPointers().size())) {
            log.warn(dictionary.getMessages().resolveMessage("PRINCETON_WARN_002",
                    new Object[]{synset.getOffset(), synset.getPointers().size()}));
        }
        if (synset.getPointers().size() < 100) {
            result.append("0");
            if (synset.getPointers().size() < 10) {
                result.append("0");
            }
        }
        result.append(synset.getPointers().size()).append(' ');
        for (Pointer p : synset.getPointers()) {
            // pointer_symbol  synset_offset  pos  source/target
            result.append(p.getType().getKey()).append(' ');
            // synset_offset is the byte offset of the target synset in the data file corresponding to pos
            formatOffset(p.getTargetOffset(), offsetLength, result);
            result.append(' ');
            // pos
            result.append(p.getTargetPOS().getKey()).append(' ');
            // source/target
            // The source/target field distinguishes lexical and semantic pointers.
            // It is a four byte field, containing two two-digit hexadecimal integers.
            // The first two digits indicates the word number in the current (source) synset,
            // the last two digits indicate the word number in the target synset.
            // A value of 0000 means that pointer_symbol represents a semantic relation
            // between the current (source) synset and the target synset indicated by synset_offset.

            // A lexical relation between two words in different synsets is represented
            // by non-zero values in the source and target word numbers.
            // The first and last two bytes of this field indicate the word numbers
            // in the source and target synsets, respectively, between which the relation holds.
            // Word numbers are assigned to the word fields in a synset, from left to right, beginning with 1.
            if (checkPointerIndexLimit && log.isWarnEnabled() && (0xFF < p.getSourceIndex())) {
                log.warn(dictionary.getMessages().resolveMessage("PRINCETON_WARN_006", new Object[]{synset.getOffset(),
                        p.getSource().getSynset().getOffset(), p.getSourceIndex()}));
            }
            if (checkPointerIndexLimit && log.isWarnEnabled() && (0xFF < p.getTargetIndex())) {
                log.warn(dictionary.getMessages().resolveMessage("PRINCETON_WARN_006", new Object[]{synset.getOffset(),
                        p.getTarget().getSynset().getOffset(), p.getTargetIndex()}));
            }
            if (p.getSourceIndex() < 0x10) {
                result.append("0");
            }
            result.append(Integer.toHexString(p.getSourceIndex()));
            if (p.getTargetIndex() < 0x10) {
                result.append("0");
            }
            result.append(Integer.toHexString(p.getTargetIndex())).append(' ');
        }

        // frames In data.verb only
        if (POS.VERB == synset.getPOS()) {
            BitSet verbFrames = synset.getVerbFrameFlags();
            int verbFramesCount = verbFrames.cardinality();
            for (Word word : synset.getWords()) {
                if (word instanceof Verb) {
                    BitSet bits = ((Verb) word).getVerbFrameFlags();
                    for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
                        //WN TRICK - there are duplicates in data
                        //02593551 41 v 04 lord_it_over 0 queen_it_over 0 put_on_airs 0 act_superior 0 001 @ 02367363 v 0000
                        // 09 + 02 00 + 02 04 + 22 04 + 02 03 + 22 03 + 08 02 + 09 02 + 08 01 + 09 01 | act like the master of; "He is lording it over the students"
                        // + 02 04 and + 02 03 duplicate + 02 00
                        // it is the only one, but it causes offsets to differ on WN30 rewrite
                        if (!verbFrames.get(i)) {
                            verbFramesCount++;
                        }
                    }
                }
            }
            if (checkVerbFrameLimit && log.isWarnEnabled() && (99 < verbFramesCount)) {
                log.warn(dictionary.getMessages().resolveMessage("PRINCETON_WARN_007", new Object[]{synset.getOffset(), verbFramesCount}));
            }
            if (verbFramesCount < 10) {
                result.append("0");
            }
            result.append(verbFramesCount).append(' ');
            // render frames applicable to all words
            for (int i = verbFrames.nextSetBit(0); i >= 0; i = verbFrames.nextSetBit(i + 1)) {
                if (checkVerbFrameLimit && log.isWarnEnabled() && (99 < i)) {
                    log.warn(dictionary.getMessages().resolveMessage("PRINCETON_WARN_008",
                            new Object[]{synset.getOffset(), i}));
                }
                result.append("+ ");
                if (i < 10) {
                    result.append("0");
                }
                result.append(i);
                result.append(" 00 ");
            }
            // render word-specific frames
            for (int idx = synset.getWords().size() - 1; idx >= 0; idx--) {
                Word word = synset.getWords().get(idx);
                if (word instanceof Verb) {
                    BitSet bits = ((Verb) word).getVerbFrameFlags();
                    for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
                        if (!verbFrames.get(i)) {
                            if (checkVerbFrameLimit && log.isWarnEnabled() && (0xFF < word.getIndex())) {
                                log.warn(dictionary.getMessages().resolveMessage("PRINCETON_WARN_008",
                                        new Object[]{synset.getOffset(), word.getIndex()}));
                            }
                            result.append("+ ");
                            if (i < 10) {
                                result.append("0");
                            }
                            result.append(i).append(' ');
                            if (word.getIndex() < 0x10) {
                                result.append("0");
                            }
                            result.append(Integer.toHexString(word.getIndex())).append(' ');
                        }
                    }
                }
            }
        }

        result.append("| ").append(synset.getGloss()).append("  ");// why every line in most WN files ends with two spaces?
    }

    private void renderIndexWord(IndexWord indexWord, StringBuilder result) {
        ArrayList<PointerType> pointerTypes = new ArrayList<>();
        // find all the pointers that come from this word
        for (Synset synset : indexWord.getSenses()) {
            for (Pointer pointer : synset.getPointers()) {
                if ((pointer.getSource() instanceof Word) && !indexWord.getLemma().equals(((Word) pointer.getSource()).getLemma().toLowerCase())) {
                    continue;
                }
                // WN TRICK
                // see makedb.c line 370
                PointerType pt = pointer.getType();
                char c = pointer.getType().getKey().charAt(0);
                if (';' == c || '-' == c || '@' == c || '~' == c) {
                    pt = PointerType.getPointerTypeForKey(Character.toString(c));
                }
                if (!pointerTypes.contains(pt)) {
                    pointerTypes.add(pt);
                }
            }
        }
        Collections.sort(pointerTypes);

        // sort senses and find out tagged sense count
        int tagSenseCnt = indexWord.sortSenses();

        // lemma pos synset_cnt p_cnt [ptr_symbol...] sense_cnt tagsense_cnt synset_offset [synset_offset...]
        result.append(indexWord.getLemma().replace(' ', '_'));
        result.append(' ');
        result.append(indexWord.getPOS().getKey()).append(' '); // pos
        result.append(indexWord.getSenses().size()).append(' '); // synset_cnt
        result.append(pointerTypes.size()).append(' '); // p_cnt
        for (PointerType pointerType : pointerTypes) {
            result.append(pointerType.getKey()).append(' ');
        }

        result.append(indexWord.getSenses().size()).append(' '); // sense_cnt

        result.append(tagSenseCnt).append(' '); // tagsense_cnt

        for (Synset synset : indexWord.getSenses()) {
            formatOffset(synset.getOffset(), offsetLength, result);
            result.append(' '); // synset_offset
        }

        result.append(' '); // the second of those 2 spaces at the end of line
    }

    private void renderException(Exc exc, StringBuilder result) {
        result.append(exc.getLemma().replace(' ', '_'));
        for (String e : exc.getExceptions()) {
            result.append(' ').append(e.replace(' ', '_'));
        }
    }

    private void initBuffers() {
        chars = new char[16 * 1024];
        bytesBacker = new byte[(int) Math.ceil(encoder.maxBytesPerChar() * 16 * 1024)];
        bytes = ByteBuffer.wrap(bytesBacker);
    }

    private void freeBuffers() {
        chars = null;
        bytes = null;
        bytesBacker = null;
    }
}