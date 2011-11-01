package net.sf.extjwnl.princeton.file;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.file.DictionaryFileFactory;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.util.factory.Param;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.MalformedInputException;
import java.util.*;

/**
 * A <code>RandomAccessDictionaryFile</code> that accesses files
 * named with Princeton's dictionary file naming convention.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class PrincetonRandomAccessDictionaryFile extends AbstractPrincetonRandomAccessDictionaryFile implements DictionaryFileFactory<PrincetonRandomAccessDictionaryFile> {

    private static final Log log = LogFactory.getLog(PrincetonRandomAccessDictionaryFile.class);

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

    protected RandomAccessFile raFile = null;

    private static final String PRINCETON_HEADER = "  1 This software and database is being provided to you, the LICENSEE, by  \n" +
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
            "  13   \n" +
            "  14 WordNet 3.0 Copyright 2006 by Princeton University.  All rights reserved.  \n" +
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

    private final static Comparator<Synset> synsetOffsetComparator = new Comparator<Synset>() {
        public int compare(Synset o1, Synset o2) {
            return (int) Math.signum(o1.getOffset() - o2.getOffset());
        }
    };

    private CharsetDecoder decoder;

    private int LINE_MAX = 1024;//1K buffer
    private byte[] lineArr = new byte[LINE_MAX];

    private int offsetLength = -1;

    public static void formatOffset(long i, int formatLength, StringBuilder target) {
        int lastIdx = target.length();
        target.setLength(target.length() + formatLength);

        //lifted from Long.java
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

        // Fall thru to fast mode for smaller numbers
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

    public PrincetonRandomAccessDictionaryFile(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary, params);
    }

    public PrincetonRandomAccessDictionaryFile(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType, Map<String, Param> params) {
        super(dictionary, path, pos, fileType, params);
        if (null != encoding) {
            Charset charset = Charset.forName(encoding);
            decoder = charset.newDecoder();
        }
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

    public PrincetonRandomAccessDictionaryFile newInstance(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType) {
        return new PrincetonRandomAccessDictionaryFile(dictionary, path, pos, fileType, params);
    }

    public String readLine() throws IOException {
        if (isOpen()) {
            synchronized (file) {
                if (null == encoding) {
                    return raFile.readLine();
                } else {
                    int c = -1;
                    boolean eol = false;
                    int idx = 1;

                    while (!eol) {
                        switch (c = read()) {
                            case -1:
                            case '\n':
                                eol = true;
                                break;
                            case '\r':
                                eol = true;
                                long cur = getFilePointer();
                                if ((read()) != '\n') {
                                    seek(cur);
                                }
                                break;
                            default: {
                                lineArr[idx - 1] = (byte) c;
                                idx++;
                                if (LINE_MAX == idx) {
                                    byte[] t = new byte[LINE_MAX * 2];
                                    System.arraycopy(lineArr, 0, t, 0, LINE_MAX);
                                    lineArr = t;
                                    LINE_MAX = 2 * LINE_MAX;
                                }
                                break;
                            }
                        }
                    }

                    if ((c == -1) && (1 == idx)) {
                        return null;
                    }
                    if (1 < idx) {
                        ByteBuffer bb = ByteBuffer.wrap(lineArr, 0, idx - 1);
                        try {
                            CharBuffer cb = decoder.decode(bb);
                            return cb.toString();
                        } catch (MalformedInputException e) {
                            return " ";
                        }
                    } else {
                        return null;
                    }
                }
            }
        } else {
            throw new JWNLRuntimeException("PRINCETON_EXCEPTION_001");
        }
    }

    public String readLineWord() throws IOException {
        if (isOpen()) {
            synchronized (file) {
                //in data files offset needs no decoding, it is numeric
                if (null == encoding || DictionaryFileType.DATA == getFileType()) {
                    StringBuilder input = new StringBuilder();
                    int c;
                    while (((c = raFile.read()) != -1) && c != '\n' && c != '\r' && c != ' ') {
                        input.append((char) c);
                    }
                    return input.toString();
                } else {
                    int idx = 1;
                    int c;
                    while (((c = raFile.read()) != -1) && c != '\n' && c != '\r' && c != ' ') {
                        lineArr[idx - 1] = (byte) c;
                        idx++;
                        if (LINE_MAX == idx) {
                            byte[] t = new byte[LINE_MAX * 2];
                            System.arraycopy(lineArr, 0, t, 0, LINE_MAX);
                            lineArr = t;
                            LINE_MAX = 2 * LINE_MAX;
                        }
                    }
                    if (1 < idx) {
                        ByteBuffer bb = ByteBuffer.wrap(lineArr, 0, idx - 1);
                        CharBuffer cb = decoder.decode(bb);
                        return cb.toString();
                    } else {
                        return "";
                    }
                }
            }
        } else {
            throw new JWNLRuntimeException("PRINCETON_EXCEPTION_001");
        }
    }

    public void seek(long pos) throws IOException {
        synchronized (file) {
            raFile.seek(pos);
        }
    }

    public long getFilePointer() throws IOException {
        synchronized (file) {
            return raFile.getFilePointer();
        }
    }

    public boolean isOpen() {
        return raFile != null;
    }

    public void close() {
        synchronized (file) {
            try {
                if (null != raFile) {
                    raFile.close();
                }
                super.close();
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error(JWNL.resolveMessage("EXCEPTION_001", e.getMessage()), e);
                }
            } finally {
                raFile = null;
            }
        }
    }

    /**
     * Here we try to be intelligent about opening files.
     * If the file does not already exist, we assume that we are going
     * to be creating it and writing to it, otherwise we assume that
     * we are going to be reading from it.
     */
    protected void openFile() throws IOException {
        if (!file.exists()) {
            raFile = new RandomAccessFile(file, READ_WRITE);
        } else {
            raFile = new RandomAccessFile(file, READ_ONLY);
        }
    }

    public void edit() throws IOException {
        synchronized (file) {
            raFile.close();
            raFile = new RandomAccessFile(file, READ_WRITE);
        }
    }

    public long length() throws IOException {
        synchronized (file) {
            return raFile.length();
        }
    }

    public int read() throws IOException {
        synchronized (file) {
            return raFile.read();
        }
    }

    public void save() throws IOException, JWNLException {
        if (log.isInfoEnabled()) {
            log.info(JWNL.resolveMessage("PRINCETON_INFO_004", getFilename()));
        }
        if (DictionaryFileType.EXCEPTION == getFileType()) {
            ArrayList<String> exceptions = new ArrayList<String>();
            Iterator<Exc> ei = dictionary.getExceptionIterator(getPOS());
            while (ei.hasNext()) {
                exceptions.add(renderException(ei.next()));
            }
            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("PRINCETON_INFO_005", exceptions.size()));
            }
            Collections.sort(exceptions);

            synchronized (file) {
                seek(0);
                writeStrings(exceptions);
            }
        } else if (DictionaryFileType.DATA == getFileType()) {
            ArrayList<Synset> synsets = new ArrayList<Synset>();
            Iterator<Synset> si = dictionary.getSynsetIterator(getPOS());
            while (si.hasNext()) {
                synsets.add(si.next());
            }

            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("PRINCETON_INFO_005", synsets.size()));
            }
            Collections.sort(synsets, synsetOffsetComparator);

            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("PRINCETON_INFO_007", synsets.size()));
            }
            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("PRINCETON_INFO_008", getFilename()));
            }
            long counter = 0;
            long total = synsets.size();
            long reportInt = (total / 20) + 1;//i.e. report every 5%
            synchronized (file) {
                seek(0);
                if (writePrincetonHeader) {
                    if (log.isInfoEnabled()) {
                        log.info(JWNL.resolveMessage("PRINCETON_INFO_020", getFilename()));
                    }
                    raFile.writeBytes(PRINCETON_HEADER);
                }
                if (log.isInfoEnabled()) {
                    log.info(JWNL.resolveMessage("PRINCETON_INFO_021", getFilename()));
                }
                for (Synset synset : synsets) {
                    counter++;
                    if (0 == (counter % reportInt)) {
                        if (log.isInfoEnabled()) {
                            log.info(JWNL.resolveMessage("PRINCETON_INFO_014", 100 * counter / total));
                        }
                    }
                    String renderedSynset = renderSynset(synset);
                    if (null == encoding) {
                        raFile.write(renderedSynset.getBytes());
                    } else {
                        raFile.write(renderedSynset.getBytes(encoding));
                    }
                    raFile.writeBytes("\n");
                }
            }
            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("PRINCETON_INFO_009", getFilename()));
            }
        } else if (DictionaryFileType.INDEX == getFileType()) {
            ArrayList<String> indexes = new ArrayList<String>();

            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("PRINCETON_INFO_011", getFilename()));
            }
            Iterator<IndexWord> ii = dictionary.getIndexWordIterator(getPOS());
            while (ii.hasNext()) {
                indexes.add(renderIndexWord(ii.next()));
            }

            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("PRINCETON_INFO_005", indexes.size()));
            }
            Collections.sort(indexes);
            synchronized (file) {

                seek(0);
                if (writePrincetonHeader) {
                    raFile.writeBytes(PRINCETON_HEADER);
                }
                writeIndexStrings(indexes);
            }
        }
        if (log.isInfoEnabled()) {
            log.info(JWNL.resolveMessage("PRINCETON_INFO_012", getFilename()));
        }
    }

    @Override
    public void writeLine(String line) throws IOException {
        synchronized (file) {
            byte[] bytes;
            if (null == encoding) {
                bytes = line.getBytes();
            } else {
                bytes = line.getBytes(encoding);
            }
            if (checkDataFileLineLengthLimit && bytes.length > dataFileLineLengthLimit) {
                if (log.isWarnEnabled()) {
                    log.warn(JWNL.resolveMessage("PRINCETON_WARN_009", new Object[]{getFilename(), dataFileLineLengthLimit, bytes.length}));
                }
            }
            raFile.write(bytes);
            raFile.writeBytes("\n");
        }
    }

    public void writeStrings(Collection<String> strings) throws IOException {
        synchronized (file) {
            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("PRINCETON_INFO_008", getFilename()));
            }
            long counter = 0;
            long total = strings.size();
            long reportInt = (total / 20) + 1;//i.e. report every 5%
            for (String s : strings) {
                counter++;
                if (0 == (counter % reportInt)) {
                    if (log.isInfoEnabled()) {
                        log.info(JWNL.resolveMessage("PRINCETON_INFO_014", 100 * counter / total));
                    }
                }
                writeLine(s);
            }
            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("PRINCETON_INFO_013", getFilename()));
            }
        }
    }

    public void writeIndexStrings(ArrayList<String> strings) throws IOException {
        synchronized (file) {
            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("PRINCETON_INFO_008", getFilename()));
            }
            long counter = 0;
            long total = strings.size();
            long reportInt = (total / 20) + 1;//i.e. report every 5%
            //see makedb.c FixLastRecord
            /* Funky routine to pad the second to the last record of the
             index file to be longer than the last record so the binary
             search in the search code works properly. */
            for (int i = 0; i < strings.size() - 2; i++) {
                counter++;
                if (0 == (counter % reportInt)) {
                    if (log.isInfoEnabled()) {
                        log.info(JWNL.resolveMessage("PRINCETON_INFO_014", 100 * counter / total));
                    }
                }
                writeLine(strings.get(i));
            }
            if (1 < strings.size()) {
                String nextToLast = strings.get(strings.size() - 2);
                String last = strings.get(strings.size() - 1);
                while (nextToLast.length() <= last.length()) {
                    nextToLast = nextToLast + " ";
                }
                writeLine(nextToLast);
                writeLine(last);
            }
            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("PRINCETON_INFO_014", 100));
            }
            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("PRINCETON_INFO_013", getFilename()));
            }
        }
    }

    private int getDigitCount(long number) {
        return (number == 0) ? 1 : (int) Math.log10(number) + 1;
    }

    private String renderSynset(Synset synset) {
        int estLength = offsetLength + 1//offset
                + 2 + 1 //lexfilenum
                + 1//ss_type
                + offsetLength + 1//w_cnt
                + (10 + 3 + 1) * synset.getWords().size()//avg word 10 chars + lex_id max 3 chars
                + offsetLength + 1//p_cnt
                + (1 + 1 + offsetLength + 1 + 1 + 1 + 4 + 1) * synset.getPointers().size()
                + synset.getGloss().length() + 2 + 2;
        if (POS.VERB == synset.getPOS()) {
            estLength = estLength + 8 * synset.getWords().size();//8 for verb flag, about one per word
        }

        //synset_offset  lex_filenum  ss_type  w_cnt  word  lex_id  [word  lex_id...]  p_cnt  [ptr...]  [frames...]  |   gloss
        //w_cnt Two digit hexadecimal integer indicating the number of words in the synset.
        String posKey = synset.getPOS().getKey();
        if (POS.ADJECTIVE == synset.getPOS() && synset.isAdjectiveCluster()) {
            posKey = POS.ADJECTIVE_SATELLITE_KEY;
        }
        if (checkLexFileNumber && log.isWarnEnabled() && !LexFileIdFileNameMap.getMap().containsKey(synset.getLexFileNum())) {
            log.warn(JWNL.resolveMessage("PRINCETON_WARN_001", synset.getLexFileNum()));
        }
        if (checkWordCountLimit && log.isWarnEnabled() && (0xFF < synset.getWords().size())) {
            log.warn(JWNL.resolveMessage("PRINCETON_WARN_004", new Object[]{synset.getOffset(), synset.getWords().size()}));
        }
        StringBuilder result = new StringBuilder(estLength);
        formatOffset(synset.getOffset(), offsetLength, result);
        if (synset.getLexFileNum() < 10) {
            result.append(" 0").append(synset.getLexFileNum());
        } else {
            result.append(" ").append(synset.getLexFileNum());
        }
        result.append(" ").append(posKey);
        if (synset.getWords().size() < 0x10) {
            result.append(" 0").append(Integer.toHexString(synset.getWords().size())).append(" ");
        } else {
            result.append(" ").append(Integer.toHexString(synset.getWords().size())).append(" ");
        }
        for (Word w : synset.getWords()) {
            //ASCII form of a word as entered in the synset by the lexicographer, with spaces replaced by underscore characters (_ ). The text of the word is case sensitive.
            //lex_id One digit hexadecimal integer that, when appended onto lemma , uniquely identifies a sense within a lexicographer file.
            String lemma = w.getLemma().replace(' ', '_');
            if (w instanceof Adjective) {
                Adjective a = (Adjective) w;
                if (AdjectivePosition.NONE != a.getAdjectivePosition()) {
                    lemma = lemma + "(" + a.getAdjectivePosition().getKey() + ")";
                }
            }
            if (checkLexIdLimit && log.isWarnEnabled() && (0xF < w.getLexId())) {
                log.warn(JWNL.resolveMessage("PRINCETON_WARN_005", new Object[]{synset.getOffset(), w.getLemma(), w.getLexId()}));
            }
            result.append(lemma).append(" ");
            result.append(Long.toHexString(w.getLexId())).append(" ");
        }
        //Three digit decimal integer indicating the number of pointers from this synset to other synsets. If p_cnt is 000 the synset has no pointers.
        if (checkRelationLimit && log.isWarnEnabled() && (999 < synset.getPointers().size())) {
            log.warn(JWNL.resolveMessage("PRINCETON_WARN_002", new Object[]{synset.getOffset(), synset.getPointers().size()}));
        }
        if (synset.getPointers().size() < 100) {
            result.append("0");
            if (synset.getPointers().size() < 10) {
                result.append("0");
            }
        }
        result.append(synset.getPointers().size()).append(" ");
        for (Pointer p : synset.getPointers()) {
            //pointer_symbol  synset_offset  pos  source/target
            result.append(p.getType().getKey()).append(" ");
            //synset_offset is the byte offset of the target synset in the data file corresponding to pos
            formatOffset(p.getTargetOffset(), offsetLength, result);
            result.append(" ");
            //pos
            result.append(p.getTargetPOS().getKey()).append(" ");
            //source/target
            //The source/target field distinguishes lexical and semantic pointers.
            // It is a four byte field, containing two two-digit hexadecimal integers.
            // The first two digits indicates the word number in the current (source) synset,
            // the last two digits indicate the word number in the target synset.
            // A value of 0000 means that pointer_symbol represents a semantic relation between the current (source) synset and the target synset indicated by synset_offset .

            //A lexical relation between two words in different synsets is represented by non-zero values in the source and target word numbers.
            // The first and last two bytes of this field indicate the word numbers in the source and target synsets, respectively, between which the relation holds.
            // Word numbers are assigned to the word fields in a synset, from left to right, beginning with 1 .
            if (checkPointerIndexLimit && log.isWarnEnabled() && (0xFF < p.getSourceIndex())) {
                log.warn(JWNL.resolveMessage("PRINCETON_WARN_006", new Object[]{synset.getOffset(), p.getSource().getSynset().getOffset(), p.getSourceIndex()}));
            }
            if (checkPointerIndexLimit && log.isWarnEnabled() && (0xFF < p.getTargetIndex())) {
                log.warn(JWNL.resolveMessage("PRINCETON_WARN_006", new Object[]{synset.getOffset(), p.getTarget().getSynset().getOffset(), p.getTargetIndex()}));
            }
            if (p.getSourceIndex() < 0x10) {
                result.append("0");
            }
            result.append(Integer.toHexString(p.getSourceIndex()));
            if (p.getTargetIndex() < 0x10) {
                result.append("0");
            }
            result.append(Integer.toHexString(p.getTargetIndex())).append(" ");
        }

        //frames In data.verb only
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
                log.warn(JWNL.resolveMessage("PRINCETON_WARN_007", new Object[]{synset.getOffset(), verbFramesCount}));
            }
            if (verbFramesCount < 10) {
                result.append("0");
            }
            result.append(Integer.toString(verbFramesCount)).append(" ");
            for (int i = verbFrames.nextSetBit(0); i >= 0; i = verbFrames.nextSetBit(i + 1)) {
                if (checkVerbFrameLimit && log.isWarnEnabled() && (99 < i)) {
                    log.warn(JWNL.resolveMessage("PRINCETON_WARN_008", new Object[]{synset.getOffset(), i}));
                }
                result.append("+ ");
                if (i < 10) {
                    result.append("0");
                }
                result.append(Integer.toString(i));
                result.append(" 00 ");
            }
            for (Word word : synset.getWords()) {
                if (word instanceof Verb) {
                    BitSet bits = ((Verb) word).getVerbFrameFlags();
                    for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
                        if (!verbFrames.get(i)) {
                            if (checkVerbFrameLimit && log.isWarnEnabled() && (0xFF < word.getIndex())) {
                                log.warn(JWNL.resolveMessage("PRINCETON_WARN_008", new Object[]{synset.getOffset(), word.getIndex()}));
                            }
                            result.append("+ ");
                            if (i < 10) {
                                result.append("0");
                            }
                            result.append(Integer.toString(i)).append(" ");
                            if (word.getIndex() < 0x10) {
                                result.append("0");
                            }
                            result.append(Integer.toHexString(word.getIndex())).append(" ");
                        }
                    }
                }
            }
        }

        result.append("| ").append(synset.getGloss()).append("  ");//why every line in most WN files ends with two spaces?

        return result.toString();
    }

    private String renderIndexWord(IndexWord indexWord) {
        ArrayList<PointerType> pointerTypes = new ArrayList<PointerType>();
        //find all the pointers that come from this word
        for (Synset synset : indexWord.getSenses()) {
            for (Pointer pointer : synset.getPointers()) {
                if (pointer.isLexical() && !indexWord.getLemma().equals(((Word) pointer.getSource()).getLemma().toLowerCase())) {
                    continue;
                }
                //WN TRICK
                //see makedb.c line 370
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

        //sort senses and find out tagged sense count
        int tagSenseCnt = indexWord.sortSenses();

        int estLength = indexWord.getLemma().length() + 1 //lemma
                + 2 //pos
                + 2 * (offsetLength + 1) //synset_cnt + sense_cnt
                + 1 + offsetLength//p_cnt
                + 3 * pointerTypes.size()//ptrs, each max 2 chars + 1 space
                + offsetLength + 1//tagsense_cnt
                + (offsetLength + 1) * indexWord.getSenses().size() + 2;

        //lemma  pos  synset_cnt  p_cnt  [ptr_symbol...]  sense_cnt  tagsense_cnt   synset_offset  [synset_offset...]
        StringBuilder result = new StringBuilder(estLength);
        result.append(indexWord.getLemma().replace(' ', '_'));
        result.append(" ");
        result.append(indexWord.getPOS().getKey()).append(" ");//pos
        result.append(Integer.toString(indexWord.getSenses().size())).append(" ");//synset_cnt
        result.append(Integer.toString(pointerTypes.size())).append(" ");//p_cnt
        for (PointerType pointerType : pointerTypes) {
            result.append(pointerType.getKey()).append(" ");
        }

        result.append(Integer.toString(indexWord.getSenses().size())).append(" ");//sense_cnt

        result.append(Integer.toString(tagSenseCnt)).append(" ");//tagsense_cnt

        for (Synset synset : indexWord.getSenses()) {
            formatOffset(synset.getOffset(), offsetLength, result);
            result.append(" ");//synset_offset
        }

        result.append(" ");
        return result.toString();
    }

    private String renderException(Exc exc) {
        StringBuilder result = new StringBuilder();
        result.append(exc.getLemma().replace(' ', '_'));
        for (String e : exc.getExceptions()) {
            result.append(" ").append(e.replace(' ', '_'));
        }
        return result.toString();
    }

    public int getOffsetLength() throws IOException, JWNLException {
        if (DictionaryFileType.DATA == getFileType()) {
            ArrayList<Synset> synsets = new ArrayList<Synset>();
            Iterator<Synset> si = dictionary.getSynsetIterator(getPOS());
            while (si.hasNext()) {
                synsets.add(si.next());
            }

            if (log.isInfoEnabled()) {
                log.info(JWNL.resolveMessage("PRINCETON_INFO_005", synsets.size()));
            }
            Collections.sort(synsets, synsetOffsetComparator);

            offsetLength = 8;
            int offsetDigitCount = 8;//8 by default for WN compatibility
            do {
                if (offsetLength < offsetDigitCount) {
                    offsetLength = offsetDigitCount;
                    if (log.isWarnEnabled()) {
                        log.warn(JWNL.resolveMessage("PRINCETON_WARN_010", offsetLength));
                    }
                }
                if (log.isInfoEnabled()) {
                    log.info(JWNL.resolveMessage("PRINCETON_INFO_006", new Object[]{synsets.size(), getFilename()}));
                }
                long offset = 0;
                if (writePrincetonHeader) {
                    offset = offset + PRINCETON_HEADER.length();
                }
                long safeOffset = Integer.MAX_VALUE - 1;
                for (Synset s : synsets) {
                    String renderedSynset = renderSynset(s);
                    Synset oldSynset = dictionary.getSynsetAt(s.getPOS(), offset);
                    if (null != oldSynset) {
                        oldSynset.setOffset(safeOffset);
                        safeOffset--;
                    }
                    s.setOffset(offset);
                    if (null == encoding) {
                        offset = offset + renderedSynset.getBytes().length + 1;//\n should be 1 byte
                    } else {
                        offset = offset + renderedSynset.getBytes(encoding).length + 1;//\n should be 1 byte
                    }
                }

                //calculate used offset length
                offsetDigitCount = Math.max(offsetLength, getDigitCount(offset));
            } while (offsetLength < offsetDigitCount);
        } else if (DictionaryFileType.INDEX == getFileType()) {
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

    public void setOffsetLength(int length) throws JWNLException, IOException {
        if (offsetLength != length) {
            offsetLength = length;

            //recalculate offsets which might change due to changed offset length
            if (DictionaryFileType.DATA == getFileType()) {
                ArrayList<Synset> synsets = new ArrayList<Synset>();
                Iterator<Synset> si = dictionary.getSynsetIterator(getPOS());
                while (si.hasNext()) {
                    synsets.add(si.next());
                }

                if (log.isInfoEnabled()) {
                    log.info(JWNL.resolveMessage("PRINCETON_INFO_005", synsets.size()));
                }
                Collections.sort(synsets, synsetOffsetComparator);

                if (log.isInfoEnabled()) {
                    log.info(JWNL.resolveMessage("PRINCETON_INFO_006", new Object[]{synsets.size(), getFilename()}));
                }
                long offset = 0;
                if (writePrincetonHeader) {
                    offset = offset + PRINCETON_HEADER.length();
                }
                long safeOffset = Integer.MAX_VALUE - 1;
                for (Synset s : synsets) {
                    String renderedSynset = renderSynset(s);
                    Synset oldSynset = dictionary.getSynsetAt(s.getPOS(), offset);
                    if (null != oldSynset) {
                        oldSynset.setOffset(safeOffset);
                        safeOffset--;
                    }
                    s.setOffset(offset);
                    if (null == encoding) {
                        offset = offset + renderedSynset.getBytes().length + 1;//\n should be 1 byte
                    } else {
                        offset = offset + renderedSynset.getBytes(encoding).length + 1;//\n should be 1 byte
                    }
                }
            }
        }
    }
}