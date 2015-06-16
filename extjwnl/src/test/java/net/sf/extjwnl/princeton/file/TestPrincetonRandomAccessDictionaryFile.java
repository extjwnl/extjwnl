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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

@RunWith(DataProviderRunner.class)
public class TestPrincetonRandomAccessDictionaryFile {

    @DataProvider
    public static Object[][] testFormatOffset() {
        return new Object[][]{
                {0, 0, ""},
                {0, 1, "0"},
                {0, 2, "00"},
                {1, 0, ""},
                {1, 1, "1"},
                {1, 2, "01"},
                {1, 3, "001"},
                {123, 1, "3"},
                {123, 2, "23"},
                {123, 3, "123"},
                {123, 4, "0123"},
                {12345678, 4, "5678"},
                {12345678, 8, "12345678"},
                {12345678, 9, "012345678"},
                {123456789123L, 9, "456789123"},
                {123456789123L, 12, "123456789123"},
                {123456789123L, 13, "0123456789123"}
        };
    }

    @Test
    @UseDataProvider("testFormatOffset")
    public void testFormatOffset(final long i, final int l, final String s) {
        StringBuilder b = new StringBuilder();
        PrincetonRandomAccessDictionaryFile.formatOffset(i, l, b);
        assertEquals(s, b.toString());
    }

    private static byte[] bytes;
    private static byte[] utfBytes;
    private static byte[] offset;

    @BeforeClass
    public static void beforeClass() throws IOException {
        {
            InputStream input = PrincetonRandomAccessDictionaryFile.class.getResourceAsStream("/readLineASCII.txt");
            ByteArrayOutputStream output = new ByteArrayOutputStream(16 * 1024);
            PrincetonResourceDictionaryFile.copyStream(input, output);
            bytes = output.toByteArray();
        }
        {
            InputStream input = PrincetonRandomAccessDictionaryFile.class.getResourceAsStream("/readLineUTF.txt");
            ByteArrayOutputStream output = new ByteArrayOutputStream(16 * 1024);
            PrincetonResourceDictionaryFile.copyStream(input, output);
            utfBytes = output.toByteArray();
        }
        {
            InputStream input = PrincetonRandomAccessDictionaryFile.class.getResourceAsStream("/firstLineOffset.txt");
            ByteArrayOutputStream output = new ByteArrayOutputStream(16 * 1024);
            PrincetonResourceDictionaryFile.copyStream(input, output);
            offset = output.toByteArray();
        }
    }

    private PrincetonRandomAccessDictionaryFile pradf;

    protected void initPRADF(String encoding, byte[] bytes) throws NoSuchFieldException, IllegalAccessException, IOException {
        Dictionary d = mock(Dictionary.class);
        ResourceBundleSet b = mock(ResourceBundleSet.class);
        when(d.getMessages()).thenReturn(b);
        when(b.resolveMessage("PRINCETON_EXCEPTION_001")).thenReturn("Illegal Operation: file is not open or is not readable");

        Map<String, Param> params = new HashMap<String, Param>();
        if (null != encoding) {
            NameValueParam e = new NameValueParam(null, AbstractPrincetonRandomAccessDictionaryFile.ENCODING_KEY, encoding);
            params.put(AbstractPrincetonRandomAccessDictionaryFile.ENCODING_KEY, e);
        }

        pradf = new PrincetonRandomAccessDictionaryFile(d, "", POS.NOUN, DictionaryFileType.DATA, params);

        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        final RandomAccessFile raFile = mock(RandomAccessFile.class);
        when(raFile.length()).thenReturn((long) byteBuffer.limit());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                long offset = (Long) args[0];
                byteBuffer.position((int) offset);
                return null;
            }
        }).when(raFile).seek(anyLong());
        doAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                byte[] bytes = (byte[]) args[0];
                int off = (Integer) args[1];
                int len = (Integer) args[2];
                byteBuffer.get(bytes, off, len);
                return len;
            }
        }).when(raFile).read(any(byte[].class), anyInt(), anyInt());

        Field field = PrincetonRandomAccessDictionaryFile.class.getDeclaredField("raFile");
        field.setAccessible(true);
        field.set(pradf, raFile);
        field = PrincetonRandomAccessDictionaryFile.class.getDeclaredField("raFileLength");
        field.setAccessible(true);
        field.set(pradf, byteBuffer.limit());
    }

    @Test
    public void testReadLinePre() throws JWNLException, NoSuchFieldException, IllegalAccessException, IOException {
        initPRADF(null, bytes);

        assertNull(pradf.readLine(-1));
        assertNull(pradf.readLine(Integer.MAX_VALUE));
    }


    @Test(expected = JWNLException.class)
    public void testReadLineJWNLException() throws JWNLException, NoSuchFieldException, IllegalAccessException, IOException {
        initPRADF(null, bytes);

        Field field = PrincetonRandomAccessDictionaryFile.class.getDeclaredField("raFile");
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
    public void testReadLine(final int o, final String s, final long p) throws JWNLException, NoSuchFieldException, IllegalAccessException, IOException {
        initPRADF(null, bytes);

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
    public void testReadLineUTF(final int o, final String s, final long p) throws JWNLException, NoSuchFieldException, IllegalAccessException, IOException {
        initPRADF("UTF-8", utfBytes);

        PointedCharSequence sequence = pradf.readLine(o);
        assertNotNull(sequence);
        assertEquals(s, sequence.toString());
        assertEquals(p, sequence.getLastBytePosition());
    }


    @Test
    public void testReadWordPre() throws JWNLException, NoSuchFieldException, IllegalAccessException, IOException {
        initPRADF(null, bytes);

        assertNull(pradf.readWord(-1));
        assertNull(pradf.readWord(Integer.MAX_VALUE));
    }


    @Test(expected = JWNLException.class)
    public void testReadWordJWNLException() throws JWNLException, NoSuchFieldException, IllegalAccessException, IOException {
        initPRADF(null, bytes);

        Field field = PrincetonRandomAccessDictionaryFile.class.getDeclaredField("raFile");
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
    public void testReadWord(final int o, final String s, final long p) throws JWNLException, NoSuchFieldException, IllegalAccessException, IOException {
        initPRADF(null, bytes);

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
    public void testReadWordUTF(final int o, final String s, final long p) throws JWNLException, NoSuchFieldException, IllegalAccessException, IOException {
        initPRADF("UTF-8", utfBytes);

        PointedCharSequence sequence = pradf.readWord(o);
        assertNotNull(sequence);
        assertEquals(s, sequence.toString());
        assertEquals(p, sequence.getLastBytePosition());
    }

    @Test(expected = JWNLException.class)
    public void testGetFirstLineOffsetJWNLException() throws JWNLException, NoSuchFieldException, IllegalAccessException, IOException {
        initPRADF(null, offset);

        Field field = PrincetonRandomAccessDictionaryFile.class.getDeclaredField("raFile");
        field.setAccessible(true);
        field.set(pradf, null);
        pradf.getFirstLineOffset();
    }

    @Test
    public void testGetFirstLineOffset() throws NoSuchFieldException, IllegalAccessException, JWNLException, IOException {
        initPRADF(null, offset);
        assertEquals(2210, pradf.getFirstLineOffset());
        assertEquals(2210, pradf.getFirstLineOffset()); // to cover also cached branch
        assertEquals("line", pradf.readLine(2210).toString());

        initPRADF(null, bytes);
        assertEquals(0, pradf.getFirstLineOffset());
    }

    @Test
    public void testGetNextLineOffsetPre() throws JWNLException, NoSuchFieldException, IllegalAccessException, IOException {
        initPRADF(null, bytes);

        assertEquals(-1, pradf.getNextLineOffset(-1));
        assertEquals(-1, pradf.getNextLineOffset(Integer.MAX_VALUE));
    }


    @Test(expected = JWNLException.class)
    public void testGetNextLineOffsetJWNLException() throws JWNLException, NoSuchFieldException, IllegalAccessException, IOException {
        initPRADF(null, bytes);

        Field field = PrincetonRandomAccessDictionaryFile.class.getDeclaredField("raFile");
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
    public void testGetNextLineOffset(final int x, final int y) throws JWNLException, NoSuchFieldException, IllegalAccessException, IOException {
        initPRADF(null, bytes);

        assertEquals(y, pradf.getNextLineOffset(x));
    }
}