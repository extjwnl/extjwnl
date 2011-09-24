package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

/**
 * Tests the word summarization functionality.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestSummary {

    private static Dictionary dic;

    @BeforeClass
    public static void runOnceBeforeAllTests() throws FileNotFoundException, JWNLException {
        dic = Dictionary.getInstance(new FileInputStream("./src/main/resources/net/sf/extjwnl/file_properties.xml"));
    }

    @Test
    public void testCar() throws JWNLException {
        String queryString = "car";
        IndexWord iw = dic.getIndexWord(POS.NOUN, queryString);
        Assert.assertNotNull(iw);
        String[] tests = {"auto", "railcar", "gondola", "elevator car", "cable car"};
        int[][] indices = {{0, 0}, {1, 0}, {2, 0}, {3, 0}, {4, 1}};
        for (int i = 0; i < tests.length; i++) {
            Assert.assertEquals(tests[i], iw.getSenses().get(indices[i][0]).getWords().get(indices[i][1]).getSummary());
        }
    }

    @Test
    public void testJava() throws JWNLException {
        String queryString = "java";
        IndexWord iw = dic.getIndexWord(POS.NOUN, queryString);
        Assert.assertNotNull(iw);
        String[] tests = {"island", "coffee", "object-oriented programing language"};
        int[][] indices = {{0, 0}, {1, 1}, {2, 0}};
        for (int i = 0; i < tests.length; i++) {
            Assert.assertEquals(tests[i], iw.getSenses().get(indices[i][0]).getWords().get(indices[i][1]).getSummary());
        }
    }

    @Test
    public void testBank() throws JWNLException {
        String queryString = "bank";
        IndexWord iw = dic.getIndexWord(POS.NOUN, queryString);
        Assert.assertNotNull(iw);
        {
            String[] tests = {"side", "banking concern", "ridge", "array", "reserve", "funds", "cant", "coin bank", "bank building", "flight maneuver"};
            int[][] indices = {{0, 0}, {1, 1}, {2, 0}, {3, 0}, {4, 0}, {5, 0}, {6, 0}, {7, 3}, {8, 0}, {9, 0}};
            for (int i = 0; i < tests.length; i++) {
                Assert.assertEquals(tests[i], iw.getSenses().get(indices[i][0]).getWords().get(indices[i][1]).getSummary());
            }
        }

        iw = dic.getIndexWord(POS.VERB, queryString);
        Assert.assertNotNull(iw);
        String[] tests = {"tip", "enclose", "transact", "act", "work", "deposit", "cover", "rely"};
        int[][] indices = {{0, 0}, {1, 0}, {2, 0}, {3, 0}, {4, 0}, {5, 1}, {6, 0}, {7, 3}};
        for (int i = 0; i < tests.length; i++) {
            Assert.assertEquals(tests[i], iw.getSenses().get(indices[i][0]).getWords().get(indices[i][1]).getSummary());
        }
    }

    @Test
    public void testCannon() throws JWNLException {
        String queryString = "cannon";
        IndexWord iw = dic.getIndexWord(POS.NOUN, queryString);
        Assert.assertNotNull(iw);
        {
            String[] tests = {"gun", "cannon", "armor plate", "cannon", "shank", "carom"};
            int[][] indices = {{0, 0}, {1, 0}, {2, 0}, {3, 0}, {4, 0}, {5, 1}};
            for (int i = 0; i < tests.length; i++) {
                Assert.assertEquals(tests[i], iw.getSenses().get(indices[i][0]).getWords().get(indices[i][1]).getSummary());
            }
        }

        iw = dic.getIndexWord(POS.VERB, queryString);
        Assert.assertNotNull(iw);
        String[] tests = {"hit", "discharge"};
        int[][] indices = {{0, 0}, {1, 0}};
        for (int i = 0; i < tests.length; i++) {
            Assert.assertEquals(tests[i], iw.getSenses().get(indices[i][0]).getWords().get(indices[i][1]).getSummary());
        }
    }

    @Test
    public void testCannonContextual() throws JWNLException {
        String queryString = "cannon";
        IndexWord iw = dic.getIndexWord(POS.NOUN, queryString);
        Assert.assertNotNull(iw);
        {
            List<Synset> senses = Arrays.asList(iw.getSenses().get(0), iw.getSenses().get(2), iw.getSenses().get(4), iw.getSenses().get(5));
            String[] tests = {"gun", "armor plate", "shank", "carom"};
            int[][] indices = {{0, 0}, {2, 0}, {4, 0}, {5, 1}};
            for (int i = 0; i < tests.length; i++) {
                Assert.assertEquals(tests[i], iw.getSenses().get(indices[i][0]).getWords().get(indices[i][1]).getSummary(senses));
            }

        }
    }


    @Test
    public void testCase() throws JWNLException {
        String queryString = "case";
        IndexWord iw = dic.getIndexWord(POS.NOUN, queryString);
        Assert.assertNotNull(iw);
        {
            String[] tests = {"example", "event", "suit", "fact", "container", "soul", "subject", "problem", "argument", "caseful",
                    "grammatical case", "state of mind", "type", "font", "sheath", "shell", "casing", "compositor's case", "slip", "vitrine"};
            int[][] indices = {{0, 0}, {1, 1}, {2, 2}, {3, 0}, {4, 0}, {5, 0}, {6, 1}, {7, 0}, {8, 0}, {9, 0},
                    {10, 0}, {11, 0}, {12, 3}, {13, 4}, {14, 1}, {15, 1}, {16, 1}, {17, 0}, {18, 0}, {19, 0}};
            for (int i = 0; i < tests.length; i++) {
                Assert.assertEquals(tests[i], iw.getSenses().get(indices[i][0]).getWords().get(indices[i][1]).getSummary());
            }

        }


        iw = dic.getIndexWord(POS.VERB, queryString);
        Assert.assertNotNull(iw);
        String[] tests = {"inspect", "encase"};
        int[][] indices = {{0, 0}, {1, 2}};
        for (int i = 0; i < tests.length; i++) {
            Assert.assertEquals(tests[i], iw.getSenses().get(indices[i][0]).getWords().get(indices[i][1]).getSummary());
        }
    }

    @Test
    public void testNice() throws JWNLException {
        String queryString = "nice";
        IndexWord iw = dic.getIndexWord(POS.NOUN, queryString);
        Assert.assertNotNull(iw);
        {
            String[] tests = {"city"};
            int[][] indices = {{0, 0}};
            for (int i = 0; i < tests.length; i++) {
                Assert.assertEquals(tests[i], iw.getSenses().get(indices[i][0]).getWords().get(indices[i][1]).getSummary());
            }

        }
        iw = dic.getIndexWord(POS.ADJECTIVE, queryString);
        Assert.assertNotNull(iw);
        String[] tests = {"good", "decent", "skillful", "dainty", "gracious"};
        int[][] indices = {{0, 0}, {1, 1}, {2, 0}, {3, 1}, {4, 2}};
        for (int i = 0; i < tests.length; i++) {
            Assert.assertEquals(tests[i], iw.getSenses().get(indices[i][0]).getWords().get(indices[i][1]).getSummary());
        }
    }
}