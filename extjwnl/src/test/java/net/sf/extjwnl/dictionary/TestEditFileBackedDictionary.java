package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.file.AbstractDictionaryFile;
import net.sf.extjwnl.dictionary.file.RandomAccessDictionaryFile;
import net.sf.extjwnl.dictionary.file_manager.FileManagerImpl;
import net.sf.extjwnl.princeton.file.AbstractPrincetonDictionaryFile;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests FileBackedDictionary editing.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestEditFileBackedDictionary extends DictionaryEditTester {

    protected String zero = "zero";
    protected String third = "третий";
    protected String encoding = "UTF-8";

    public TestEditFileBackedDictionary() {
        second = "второй";
    }

    @Override
    protected InputStream getProperties() {
        return TestEditFileBackedDictionary.class.getResourceAsStream("/test_clean_file.xml");
    }

    @Test
    public void testCountsSerialization() throws JWNLException, IOException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        dictionary.edit();

        Synset s0 = dictionary.createSynset(POS.NOUN);
        s0.setGloss(zero + " gloss");
        final Word w0 = new Word(dictionary, s0, zero);
        s0.getWords().add(w0);

        Synset s1 = dictionary.createSynset(POS.NOUN);
        s1.setGloss(first + " gloss");
        final Word w1 = new Word(dictionary, s1, first);
        w1.setUseCount(1);
        s1.getWords().add(w1);

        Synset s2 = dictionary.createSynset(POS.NOUN);
        s2.setGloss(second + " gloss");
        final Word w2 = new Word(dictionary, s2, second);
        w2.setUseCount(2);
        s2.getWords().add(w2);

        Synset s3 = dictionary.createSynset(POS.NOUN);
        s3.setGloss(third + " gloss");
        final Word w3 = new Word(dictionary, s3, third);
        w3.setUseCount(3);
        s3.getWords().add(w3);

        dictionary.save();

        // check serialization
        // 3 третий%1:00:00:: 1
        // 2 second%1:00:00:: 1
        // 1 first%1:00:00:: 1

        // hack the path out
        FileBackedDictionary fbd = (FileBackedDictionary) dictionary;
        Field field = FileBackedDictionary.class.getDeclaredField("fileManager");
        field.setAccessible(true);
        FileManagerImpl fm = (FileManagerImpl) field.get(fbd);
        field = FileManagerImpl.class.getDeclaredField("cntList");
        field.setAccessible(true);
        RandomAccessDictionaryFile cntList = (RandomAccessDictionaryFile) field.get(fm);

        field = AbstractDictionaryFile.class.getDeclaredField("path");
        field.setAccessible(true);
        String path = (String) field.get(cntList);

        Method method = AbstractPrincetonDictionaryFile.class.getDeclaredMethod("getFilename");
        method.setAccessible(true);
        String name = (String) method.invoke(cntList);

        dictionary.close();

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path, name)), encoding));
        try {
            String[] lines = new String[3];
            lines[0] = br.readLine();
            lines[1] = br.readLine();
            lines[2] = br.readLine();
            assertEquals("3 " + third + "%1:00:00:: 1", lines[0]);
            assertEquals("2 " + second + "%1:00:00:: 1", lines[1]);
            assertEquals("1 " + first + "%1:00:00:: 1", lines[2]);
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            br.close();
        }
    }
}
