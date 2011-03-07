package net.sf.extjwnl.princeton.file;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.JWNLRuntimeException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.file.DictionaryFileFactory;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.util.MessageLog;
import net.sf.extjwnl.util.MessageLogLevel;
import net.sf.extjwnl.util.factory.Param;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.MalformedInputException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * A <code>RandomAccessDictionaryFile</code> that accesses files
 * named with Princeton's dictionary file naming convention.
 *
 * @author John Didion <jdidion@users.sourceforge.net>
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public class PrincetonRandomAccessDictionaryFile extends AbstractPrincetonRandomAccessDictionaryFile implements DictionaryFileFactory<PrincetonRandomAccessDictionaryFile> {

    private static final MessageLog log = new MessageLog(PrincetonRandomAccessDictionaryFile.class);

    /**
     * Read-only file permission.
     */
    public static final String READ_ONLY = "r";
    /**
     * Read-write file permission.
     */
    public static final String READ_WRITE = "rw";

    /**
     * The random-access file.
     */
    protected RandomAccessFile raFile = null;

    private CharsetDecoder decoder;

    private int LINE_MAX = 1024;//1K buffer
    private byte[] lineArr = new byte[LINE_MAX];

    private DecimalFormat dfOff;
    private String decimalFormatString = "00000000";

    public PrincetonRandomAccessDictionaryFile(Dictionary dictionary, Map<String, Param> params) {
        super(dictionary, params);
    }

    public PrincetonRandomAccessDictionaryFile(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType, Map<String, Param> params) {
        super(dictionary, path, pos, fileType, params);
        if (null != encoding) {
            Charset charset = Charset.forName(encoding);
            decoder = charset.newDecoder();
        }
    }

    public PrincetonRandomAccessDictionaryFile newInstance(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType) {
        return new PrincetonRandomAccessDictionaryFile(dictionary, path, pos, fileType, params);
    }

    public String readLine() throws IOException {
        if (isOpen()) {
            if (null == encoding) {
                return raFile.readLine();
            } else {
                int c = -1;
                boolean eol = false;
                int idx = 1;
                StringBuilder input = new StringBuilder();

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
                            input.append((char) c);
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

                if ((c == -1) && (input.length() == 0)) {
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
                    return input.toString();
                }
            }
        } else {
            throw new JWNLRuntimeException("PRINCETON_EXCEPTION_001");
        }
    }

    public String readLineWord() throws IOException {
        if (isOpen()) {
            //in data files offset needs no decoding, it is numeric
            if (null == encoding || getFileType().equals(DictionaryFileType.DATA)) {
                StringBuffer input = new StringBuffer();
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
        } else {
            throw new JWNLRuntimeException("PRINCETON_EXCEPTION_001");
        }
    }

    public void seek(long pos) throws IOException {
        raFile.seek(pos);
    }

    public long getFilePointer() throws IOException {
        return raFile.getFilePointer();
    }

    public boolean isOpen() {
        return raFile != null;
    }

    public void close() {
        try {
            if (null != raFile) {
                raFile.close();
            }
            super.close();
        } catch (Exception e) {
            log.log(MessageLogLevel.ERROR, "EXCEPTION_001", e.getMessage(), e);
        } finally {
            raFile = null;
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
        raFile.close();
        raFile = new RandomAccessFile(file, READ_WRITE);
    }


    public long length() throws IOException {
        return raFile.length();
    }

    public int read() throws IOException {
        return raFile.read();
    }

    public void save() throws IOException, JWNLException {
        log.log(MessageLogLevel.INFO, "PRINCETON_INFO_004", makeFilename());
        if (DictionaryFileType.EXCEPTION.equals(getFileType())) {
            ArrayList<String> exceptions = new ArrayList<String>();
            Iterator<Exc> ei = dictionary.getExceptionIterator(getPOS());
            while (ei.hasNext()) {
                exceptions.add(renderException(ei.next()));
            }
            log.log(MessageLogLevel.INFO, "PRINCETON_INFO_005", exceptions.size());
            Collections.sort(exceptions);

            seek(0);
            writeStrings(exceptions);
        } else if (DictionaryFileType.DATA.equals(getFileType())) {
            ArrayList<Synset> synsets = new ArrayList<Synset>();
            Iterator<Synset> si = dictionary.getSynsetIterator(getPOS());
            while (si.hasNext()) {
                synsets.add(si.next());
            }

            log.log(MessageLogLevel.INFO, "PRINCETON_INFO_005", synsets.size());
            Collections.sort(synsets, new Comparator<Synset>() {
                public int compare(Synset o1, Synset o2) {
                    return (int) Math.signum(o1.getOffset() - o2.getOffset());
                }
            });

            dfOff = new DecimalFormat("00000000");//8 by default
            log.log(MessageLogLevel.INFO, "PRINCETON_INFO_006", synsets.size());
            long offset = raFile.getFilePointer();
            for (Synset s : synsets) {
                s.setOffset(offset);
                if (null == encoding) {
                    offset = offset + renderSynset(s).getBytes().length + 1;//\n should be 1 byte
                } else {
                    offset = offset + renderSynset(s).getBytes(encoding).length + 1;//\n should be 1 byte
                }
            }
            //calculate offset length
            decimalFormatString = createOffsetFormatString(offset);
            dfOff =  new DecimalFormat(decimalFormatString);//there is a small chance another update might be necessary

            log.log(MessageLogLevel.INFO, "PRINCETON_INFO_007", synsets.size());
            log.log(MessageLogLevel.INFO, "PRINCETON_INFO_008", makeFilename());
            long counter = 0;
            long total = synsets.size();
            long reportInt = (total / 20) + 1;//i.e. report every 5%
            seek(0);
            for (Synset synset : synsets) {
                counter++;
                if (0 == (counter % reportInt)) {
                    log.log(MessageLogLevel.INFO, "PRINCETON_INFO_014", 100 * counter / total);
                }
                if (null == encoding) {
                    raFile.write(renderSynset(synset).getBytes());
                } else {
                    raFile.write(renderSynset(synset).getBytes(encoding));
                }
                raFile.writeBytes("\n");
            }
            log.log(MessageLogLevel.INFO, "PRINCETON_INFO_009", makeFilename());

        } else if (DictionaryFileType.INDEX.equals(getFileType())) {
            ArrayList<String> indexes = new ArrayList<String>();

            Iterator<IndexWord> ii = dictionary.getIndexWordIterator(getPOS());
            long maxOffset = 0;
            while (ii.hasNext()) {
                for (Synset synset : ii.next().getSenses()) {
                    if (maxOffset < synset.getOffset()) {
                        maxOffset = synset.getOffset();
                    }
                }
            }
            decimalFormatString = createOffsetFormatString(maxOffset);
            dfOff =  new DecimalFormat(decimalFormatString);

            log.log(MessageLogLevel.INFO, "PRINCETON_INFO_011", makeFilename());
            ii = dictionary.getIndexWordIterator(getPOS());
            while (ii.hasNext()) {
                indexes.add(renderIndexWord(ii.next()));
            }

            log.log(MessageLogLevel.INFO, "PRINCETON_INFO_005", indexes.size());
            Collections.sort(indexes);

            seek(0);
            writeStrings(indexes);
        }
        log.log(MessageLogLevel.INFO, "PRINCETON_INFO_012", makeFilename());
    }

    public void writeStrings(Collection<String> strings) throws IOException {
        log.log(MessageLogLevel.INFO, "PRINCETON_INFO_008", makeFilename());
        long counter = 0;
        long total = strings.size();
        long reportInt = (total / 20) + 1;//i.e. report every 5%
        for (String s : strings) {
            counter++;
            if (0 == (counter % reportInt)) {
                log.log(MessageLogLevel.INFO, "PRINCETON_INFO_014", 100 * counter / total);
            }
            if (null == encoding) {
                raFile.write(s.getBytes());
            } else {
                raFile.write(s.getBytes(encoding));
            }
            raFile.writeBytes("\n");
        }
        log.log(MessageLogLevel.INFO, "PRINCETON_INFO_013", makeFilename());
    }

    @Override
    public String getOffsetFormatString() {
        return decimalFormatString;
    }

    private String createOffsetFormatString(long offset) {
        int offsetLength = 0;
        while (0 < offset) {
            offset = offset / 10;
            offsetLength++;
        }
        offsetLength = Math.max(8, offsetLength);
        StringBuilder formatString = new StringBuilder();
        while (0 < offsetLength) {
            formatString.append("0");
            offsetLength--;
        }
        return formatString.toString();
    }

    private String renderSynset(Synset synset) throws JWNLException {
        //synset_offset  lex_filenum  ss_type  w_cnt  word  lex_id  [word  lex_id...]  p_cnt  [ptr...]  [frames...]  |   gloss
        //w_cnt Two digit hexadecimal integer indicating the number of words in the synset.
        StringBuilder result = new StringBuilder(String.format("%s %02d %s %02x ", dfOff.format(synset.getOffset()), synset.getLexFileNum(), synset.getPOS().getKey(), synset.getWords().size()));
        for (Word w : synset.getWords()) {
            //ASCII form of a word as entered in the synset by the lexicographer, with spaces replaced by underscore characters (_ ). The text of the word is case sensitive.
            //lex_id One digit hexadecimal integer that, when appended onto lemma , uniquely identifies a sense within a lexicographer file.
            result.append(String.format("%s %x ", w.getLemma().replace(' ', '_'), w.getLexId()));
        }
        //Three digit decimal integer indicating the number of pointers from this synset to other synsets. If p_cnt is 000 the synset has no pointers.
        result.append(String.format("%03d ", synset.getPointers().size()));
        for (Pointer p : synset.getPointers()) {
            //pointer_symbol  synset_offset  pos  source/target
            result.append(p.getType().getKey()).append(" ");
            //synset_offset is the byte offset of the target synset in the data file corresponding to pos
            result.append(dfOff.format(p.getTargetOffset())).append(" ");
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
            result.append(String.format("%02x%02x ", p.getSourceIndex(), p.getTargetIndex()));
        }
        //frames In data.verb only
        if (POS.VERB.equals(synset.getPOS())) {
            BitSet verbFrames = synset.getVerbFrameFlags();
            int verbFramesCount = verbFrames.cardinality();
            for (Word word : synset.getWords()) {
                if (word instanceof Verb) {
                    BitSet bits = ((Verb) word).getVerbFrameFlags();
                    for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
                        if (!verbFrames.get(i)) {
                            verbFramesCount++;
                        }
                    }
                }
            }
            result.append(String.format("%02d ", verbFramesCount));
            for (int i = verbFrames.nextSetBit(0); i >= 0; i = verbFrames.nextSetBit(i + 1)) {
                result.append(String.format("+ %02d 00 ", i));
            }
            for (Word word : synset.getWords()) {
                if (word instanceof Verb) {
                    BitSet bits = ((Verb) word).getVerbFrameFlags();
                    for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
                        if (!verbFrames.get(i)) {
                            result.append(String.format("+ %02d %02x ", i, word.getIndex()));
                        }
                    }
                }
            }
        }

        result.append("| ").append(synset.getGloss()).append("  ");//why every line in most WN files ends with two spaces?

        return result.toString();
    }

    private String renderIndexWord(IndexWord indexWord) throws JWNLException {
        //lemma  pos  synset_cnt  p_cnt  [ptr_symbol...]  sense_cnt  tagsense_cnt   synset_offset  [synset_offset...]
        StringBuilder result = new StringBuilder(indexWord.getLemma().replace(' ', '_'));
        result.append(" ");
        result.append(indexWord.getPOS().getKey()).append(" ");//pos
        result.append(Integer.toString(indexWord.getSenses().size())).append(" ");//synset_cnt
        Set<PointerType> pointerTypes = new HashSet<PointerType>();
        for (Synset synset : indexWord.getSenses()) {
            for (Pointer pointer : synset.getPointers()) {
                pointerTypes.add(pointer.getType());
            }
        }
        result.append(Integer.toString(pointerTypes.size())).append(" ");//p_cnt
        for (PointerType pt : pointerTypes) {
            result.append(pt.getKey()).append(" ");
        }
        result.append(Integer.toString(indexWord.getSenses().size())).append(" ");//sense_cnt

        //sort senses and find out tagged sense count
        int tagSenseCnt = indexWord.sortSenses();
        result.append(Integer.toString(tagSenseCnt)).append(" ");//tagsense_cnt

        for (Synset synset : indexWord.getSenses()) {
            result.append(dfOff.format(synset.getOffset())).append(" ");//synset_offset
        }

        result.append(" ");
        return result.toString();
    }

    private String renderException(Exc exc) {
        StringBuilder result = new StringBuilder();
        result.append(exc.getLemma());
        for (String e : exc.getExceptions()) {
            result.append(" ").append(e.replace(' ', '_'));
        }
        return result.toString();
    }
}