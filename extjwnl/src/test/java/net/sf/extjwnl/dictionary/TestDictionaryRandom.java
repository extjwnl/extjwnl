package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class TestDictionaryRandom {

    @Test
    public void testGetRandomIndexWord() throws JWNLException {
        final Dictionary d = Dictionary.getDefaultResourceInstance();
        final IndexWord indexWord1 = d.getRandomIndexWord(POS.NOUN);
        final IndexWord indexWord2 = d.getRandomIndexWord(POS.NOUN);
        Assert.assertNotEquals(indexWord1, indexWord2);
    }

    @Test
    public void testGetNonRandomIndexWord() throws JWNLException {
        Random r = new Random(1);
        Dictionary d = Dictionary.getDefaultResourceInstance();
        d.setRandom(r);

        IndexWord indexWord = d.getRandomIndexWord(POS.NOUN);
        final String lemma = indexWord.getLemma();

        r = new Random(1);
        d = Dictionary.getDefaultResourceInstance();
        d.setRandom(r);
        indexWord = d.getRandomIndexWord(POS.NOUN);

        Assert.assertEquals(lemma, indexWord.getLemma());
    }
}