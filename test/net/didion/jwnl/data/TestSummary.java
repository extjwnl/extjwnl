package net.didion.jwnl.data;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;
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
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class TestSummary {

    private static Dictionary dic;

    @BeforeClass
    public static void runOnceBeforeAllTests() throws FileNotFoundException, JWNLException {
        JWNL.initialize(new FileInputStream("./config/file_properties.xml"));
        dic = Dictionary.getInstance();
    }

    @Test
    public void testCar() throws JWNLException {
        String queryString = "car";
        IndexWord iw = dic.getIndexWord(POS.NOUN, queryString);
        Assert.assertNotNull(iw);
        Assert.assertEquals("auto", iw.getSense(1).getWord(0).getSummary());
        Assert.assertEquals("railcar", iw.getSense(2).getWord(0).getSummary());
        Assert.assertEquals("gondola", iw.getSense(3).getWord(0).getSummary());
        Assert.assertEquals("elevator car", iw.getSense(4).getWord(0).getSummary());
        Assert.assertEquals("cable car", iw.getSense(5).getWord(1).getSummary());
    }

    @Test
    public void testJava() throws JWNLException {
        String queryString = "java";
        IndexWord iw = dic.getIndexWord(POS.NOUN, queryString);
        Assert.assertNotNull(iw);
        Assert.assertEquals("island", iw.getSense(1).getWord(0).getSummary());
        Assert.assertEquals("coffee", iw.getSense(2).getWord(1).getSummary());
        Assert.assertEquals("object-oriented programing language", iw.getSense(3).getWord(0).getSummary());
    }

    @Test
    public void testBank() throws JWNLException {
        String queryString = "bank";
        IndexWord iw = dic.getIndexWord(POS.NOUN, queryString);
        Assert.assertNotNull(iw);
        Assert.assertEquals("side", iw.getSense(1).getWord(0).getSummary());
        Assert.assertEquals("banking concern", iw.getSense(2).getWord(1).getSummary());
        Assert.assertEquals("ridge", iw.getSense(3).getWord(0).getSummary());
        Assert.assertEquals("array", iw.getSense(4).getWord(0).getSummary());
        Assert.assertEquals("reserve", iw.getSense(5).getWord(0).getSummary());
        Assert.assertEquals("funds", iw.getSense(6).getWord(0).getSummary());
        Assert.assertEquals("cant", iw.getSense(7).getWord(0).getSummary());
        Assert.assertEquals("coin bank", iw.getSense(8).getWord(3).getSummary());
        Assert.assertEquals("bank building", iw.getSense(9).getWord(0).getSummary());
        Assert.assertEquals("flight maneuver", iw.getSense(10).getWord(0).getSummary());

        iw = dic.getIndexWord(POS.VERB, queryString);
        Assert.assertNotNull(iw);
        Assert.assertEquals("tip", iw.getSense(1).getWord(0).getSummary());
        Assert.assertEquals("enclose", iw.getSense(2).getWord(0).getSummary());
        Assert.assertEquals("transact", iw.getSense(3).getWord(0).getSummary());
        Assert.assertEquals("act", iw.getSense(4).getWord(0).getSummary());
        Assert.assertEquals("work", iw.getSense(5).getWord(0).getSummary());
        Assert.assertEquals("deposit", iw.getSense(6).getWord(1).getSummary());
        Assert.assertEquals("cover", iw.getSense(7).getWord(0).getSummary());
        Assert.assertEquals("rely", iw.getSense(8).getWord(3).getSummary());
    }

    @Test
    public void testCannon() throws JWNLException {
        String queryString = "cannon";
        IndexWord iw = dic.getIndexWord(POS.NOUN, queryString);
        Assert.assertNotNull(iw);
        Assert.assertEquals("gun", iw.getSense(1).getWord(0).getSummary());
        Assert.assertEquals("cannon", iw.getSense(2).getWord(0).getSummary());
        Assert.assertEquals("armor plate", iw.getSense(3).getWord(0).getSummary());
        Assert.assertEquals("cannon", iw.getSense(4).getWord(0).getSummary());
        Assert.assertEquals("shank", iw.getSense(5).getWord(0).getSummary());
        Assert.assertEquals("carom", iw.getSense(6).getWord(1).getSummary());

        iw = dic.getIndexWord(POS.VERB, queryString);
        Assert.assertNotNull(iw);
        Assert.assertEquals("hit", iw.getSense(1).getWord(0).getSummary());
        Assert.assertEquals("discharge", iw.getSense(2).getWord(0).getSummary());
    }

    @Test
    public void testCannonContextual() throws JWNLException {
        String queryString = "cannon";
        IndexWord iw = dic.getIndexWord(POS.NOUN, queryString);
        Assert.assertNotNull(iw);
        List<Synset> senses = Arrays.asList(iw.getSense(1), iw.getSense(3), iw.getSense(5), iw.getSense(6));
        Assert.assertEquals("gun", iw.getSense(1).getWord(0).getSummary(senses));
        Assert.assertEquals("armor plate", iw.getSense(3).getWord(0).getSummary(senses));
        Assert.assertEquals("shank", iw.getSense(5).getWord(0).getSummary(senses));
        Assert.assertEquals("carom", iw.getSense(6).getWord(1).getSummary(senses));
    }


    @Test
    public void testCase() throws JWNLException {
        String queryString = "case";
        IndexWord iw = dic.getIndexWord(POS.NOUN, queryString);
        Assert.assertNotNull(iw);
        Assert.assertEquals("example", iw.getSense(1).getWord(0).getSummary());
        Assert.assertEquals("event", iw.getSense(2).getWord(1).getSummary());
        Assert.assertEquals("suit", iw.getSense(3).getWord(2).getSummary());
        Assert.assertEquals("fact", iw.getSense(4).getWord(0).getSummary());
        Assert.assertEquals("container", iw.getSense(5).getWord(0).getSummary());
        Assert.assertEquals("soul", iw.getSense(6).getWord(0).getSummary());
        Assert.assertEquals("subject", iw.getSense(7).getWord(1).getSummary());
        Assert.assertEquals("problem", iw.getSense(8).getWord(0).getSummary());
        Assert.assertEquals("argument", iw.getSense(9).getWord(0).getSummary());
        Assert.assertEquals("caseful", iw.getSense(10).getWord(0).getSummary());
        Assert.assertEquals("grammatical case", iw.getSense(11).getWord(0).getSummary());
        Assert.assertEquals("state of mind", iw.getSense(12).getWord(0).getSummary());
        Assert.assertEquals("type", iw.getSense(13).getWord(3).getSummary());
        Assert.assertEquals("font", iw.getSense(14).getWord(4).getSummary());
        Assert.assertEquals("sheath", iw.getSense(15).getWord(1).getSummary());
        Assert.assertEquals("shell", iw.getSense(16).getWord(1).getSummary());
        Assert.assertEquals("casing", iw.getSense(17).getWord(1).getSummary());
        Assert.assertEquals("compositor's case", iw.getSense(18).getWord(0).getSummary());
        Assert.assertEquals("slip", iw.getSense(19).getWord(0).getSummary());
        Assert.assertEquals("vitrine", iw.getSense(20).getWord(0).getSummary());


        iw = dic.getIndexWord(POS.VERB, queryString);
        Assert.assertNotNull(iw);
        Assert.assertEquals("inspect", iw.getSense(1).getWord(0).getSummary());
        Assert.assertEquals("encase", iw.getSense(2).getWord(2).getSummary());
    }

    @Test
    public void testNice() throws JWNLException {
        String queryString = "nice";
        IndexWord iw = dic.getIndexWord(POS.NOUN, queryString);
        Assert.assertNotNull(iw);
        Assert.assertEquals("city", iw.getSense(1).getWord(0).getSummary());

        iw = dic.getIndexWord(POS.ADJECTIVE, queryString);
        Assert.assertNotNull(iw);
        Assert.assertEquals("good", iw.getSense(1).getWord(0).getSummary());
        Assert.assertEquals("decent", iw.getSense(2).getWord(1).getSummary());
        Assert.assertEquals("skillful", iw.getSense(3).getWord(0).getSummary());
        Assert.assertEquals("dainty", iw.getSense(4).getWord(1).getSummary());
        Assert.assertEquals("gracious", iw.getSense(5).getWord(2).getSummary());
    }
}