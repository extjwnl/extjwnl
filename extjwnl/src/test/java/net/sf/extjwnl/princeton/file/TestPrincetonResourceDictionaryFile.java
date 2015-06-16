package net.sf.extjwnl.princeton.file;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.dictionary.file.DictionaryFileType;
import net.sf.extjwnl.util.PointedCharSequence;
import net.sf.extjwnl.util.ResourceBundleSet;
import net.sf.extjwnl.util.factory.NameValueParam;
import net.sf.extjwnl.util.factory.Param;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(DataProviderRunner.class)
public class TestPrincetonResourceDictionaryFile {

    private PrincetonResourceDictionaryFile pradf;

    private class PrincetonResourceDictionaryFileName extends PrincetonResourceDictionaryFile {

        private final String fileName;

        public PrincetonResourceDictionaryFileName(Dictionary dictionary, String path, POS pos, DictionaryFileType fileType, Map<String, Param> params, String fileName) {
            super(dictionary, path, pos, fileType, params);
            this.fileName = fileName;
        }

        @Override
        public String getFilename() {
            return fileName;
        }
    }

    protected void initPRADF(String encoding, String file) throws NoSuchFieldException, IllegalAccessException, JWNLException {
        Dictionary d = mock(Dictionary.class);
        ResourceBundleSet b = mock(ResourceBundleSet.class);
        when(d.getMessages()).thenReturn(b);
        when(b.resolveMessage("PRINCETON_EXCEPTION_001")).thenReturn("Illegal Operation: file is not open or is not readable");

        Map<String, Param> params = new HashMap<String, Param>();
        if (null != encoding) {
            NameValueParam e = new NameValueParam(null, AbstractPrincetonRandomAccessDictionaryFile.ENCODING_KEY, encoding);
            params.put(AbstractPrincetonRandomAccessDictionaryFile.ENCODING_KEY, e);
        }

        pradf = new PrincetonResourceDictionaryFileName(d, "", POS.NOUN, DictionaryFileType.DATA, params, file);
        pradf.open();
    }

    @Test
    public void testReadLinePre() throws JWNLException, NoSuchFieldException, IllegalAccessException {
        initPRADF(null, "readLineASCII.txt");

        assertNull(pradf.readLine(-1));
        assertNull(pradf.readLine(Integer.MAX_VALUE));
    }


    @Test(expected = JWNLException.class)
    public void testReadLineJWNLException() throws JWNLException, NoSuchFieldException, IllegalAccessException {
        initPRADF(null, "readLineASCII.txt");

        Field field = PrincetonResourceDictionaryFile.class.getDeclaredField("buffer");
        field.setAccessible(true);
        field.set(pradf, null);
        pradf.readLine(0);
    }

    @DataProvider
    public static Object[][] testReadLine() {
        return new Object[][]{
                {0, "a", 1},
                {1, "", 1},
                {3, "b", 4},
                {9, "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", 529}
        };
    }

    @Test
    @UseDataProvider("testReadLine")
    public void testReadLine(final int o, final String s, final long p) throws JWNLException, NoSuchFieldException, IllegalAccessException {
        initPRADF(null, "readLineASCII.txt");

        PointedCharSequence sequence = pradf.readLine(o);
        assertNotNull(sequence);
        assertEquals(s, sequence.toString());
        assertEquals(p, sequence.getLastBytePosition());
    }

    @DataProvider
    public static Object[][] testReadLineUTF() {
        return new Object[][]{
                {0, "a", 1},
                {1, "", 1},
                {3, "b", 4},
                {9, "lunedìпонедельник", 38},
                {16, "понедельник", 38}
        };
    }

    @Test
    @UseDataProvider("testReadLineUTF")
    public void testReadLineUTF(final int o, final String s, final long p) throws JWNLException, NoSuchFieldException, IllegalAccessException {
        initPRADF("UTF-8", "readLineUTF.txt");

        PointedCharSequence sequence = pradf.readLine(o);
        assertNotNull(sequence);
        assertEquals(s, sequence.toString());
        assertEquals(p, sequence.getLastBytePosition());
    }


    @Test
    public void testReadWordPre() throws JWNLException, NoSuchFieldException, IllegalAccessException {
        initPRADF(null, "readLineASCII.txt");

        assertNull(pradf.readWord(-1));
        assertNull(pradf.readWord(Integer.MAX_VALUE));
    }


    @Test(expected = JWNLException.class)
    public void testReadWordJWNLException() throws JWNLException, NoSuchFieldException, IllegalAccessException {
        initPRADF(null, "readLineASCII.txt");

        Field field = PrincetonResourceDictionaryFile.class.getDeclaredField("buffer");
        field.setAccessible(true);
        field.set(pradf, null);
        pradf.readWord(0);
    }

    @DataProvider
    public static Object[][] testReadWord() {
        return new Object[][]{
                {0, "a", 1},
                {1, "", 1},
                {530, "word", 534},
                {535, "second", 541}
        };
    }

    @Test
    @UseDataProvider("testReadWord")
    public void testReadWord(final int o, final String s, final long p) throws JWNLException, NoSuchFieldException, IllegalAccessException {
        initPRADF(null, "readLineASCII.txt");

        PointedCharSequence sequence = pradf.readWord(o);
        assertNotNull(sequence);
        assertEquals(s, sequence.toString());
        assertEquals(p, sequence.getLastBytePosition());
    }

    @DataProvider
    public static Object[][] testReadWordUTF() {
        return new Object[][]{
                {0, "a", 1},
                {1, "", 1},
                {3, "b", 4},
                {9, "lunedìпонедельник", 38},
                {16, "понедельник", 38},
                {39, "слово", 49},
                {50, "ещё", 56},
                {68, "supercalifragilisticexpialidocious", 102}
        };
    }

    @Test
    @UseDataProvider("testReadWordUTF")
    public void testReadWordUTF(final int o, final String s, final long p) throws JWNLException, NoSuchFieldException, IllegalAccessException {
        initPRADF("UTF-8", "readLineUTF.txt");

        PointedCharSequence sequence = pradf.readWord(o);
        assertNotNull(sequence);
        assertEquals(s, sequence.toString());
        assertEquals(p, sequence.getLastBytePosition());
    }

    @Test(expected = JWNLException.class)
    public void testGetFirstLineOffsetJWNLException() throws JWNLException, NoSuchFieldException, IllegalAccessException {
        initPRADF(null, "firstLineOffset.txt");

        Field field = PrincetonResourceDictionaryFile.class.getDeclaredField("buffer");
        field.setAccessible(true);
        field.set(pradf, null);
        pradf.getFirstLineOffset();
    }

    @Test
    public void testGetFirstLineOffset() throws NoSuchFieldException, IllegalAccessException, JWNLException {
        initPRADF(null, "firstLineOffset.txt");
        assertEquals(2210, pradf.getFirstLineOffset());
        assertEquals(2210, pradf.getFirstLineOffset()); // to cover also cached branch
        assertEquals("line", pradf.readLine(2210).toString());

        initPRADF(null, "readLineASCII.txt");
        assertEquals(0, pradf.getFirstLineOffset());
    }

    @Test
    public void testGetNextLineOffsetPre() throws JWNLException, NoSuchFieldException, IllegalAccessException {
        initPRADF(null, "readLineASCII.txt");

        assertEquals(-1, pradf.getNextLineOffset(-1));
        assertEquals(-1, pradf.getNextLineOffset(Integer.MAX_VALUE));
    }


    @Test(expected = JWNLException.class)
    public void testGetNextLineOffsetJWNLException() throws JWNLException, NoSuchFieldException, IllegalAccessException {
        initPRADF(null, "readLineASCII.txt");

        Field field = PrincetonResourceDictionaryFile.class.getDeclaredField("buffer");
        field.setAccessible(true);
        field.set(pradf, null);
        pradf.getNextLineOffset(0);
    }

    @DataProvider
    public static Object[][] testGetNextLineOffset() {
        return new Object[][]{
                {0, 2},
                {1, 2},
                {2, 3},
                {3, 5},
                {9, 530},
                {530, 535},
                {545, -1}
        };
    }

    @Test
    @UseDataProvider("testGetNextLineOffset")
    public void testGetNextLineOffset(final int x, final int y) throws JWNLException, NoSuchFieldException, IllegalAccessException {
        initPRADF(null, "readLineASCII.txt");

        assertEquals(y, pradf.getNextLineOffset(x));
    }
}