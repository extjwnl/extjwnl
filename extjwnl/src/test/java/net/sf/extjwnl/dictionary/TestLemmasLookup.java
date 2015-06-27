package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

/**
 * Tests lookup of non-english lemmas.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestLemmasLookup {


    private Dictionary dic;

    @Before
    public void initDictionary() throws IOException, JWNLException {
        dic = Dictionary.getDefaultResourceInstance();
    }

    @After
    public void closeDictionary() throws IOException, JWNLException {
        if (null != dic) {
            dic.close();
        }
    }

    /**
     * For https://github.com/extjwnl/extjwnl/issues/8
     * Bug report thanks to Younes Abouelnagah (https://github.com/younes-abouelnagah)
     */
    @Test
    public void testLookups() throws JWNLException {
        String lemma = "™";
        IndexWord iw = dic.getIndexWord(POS.NOUN, lemma);
        assertThat(iw, is(nullValue()));
        lemma = "â";
        iw = dic.getIndexWord(POS.NOUN, lemma);
        assertThat(iw, is(nullValue()));
        lemma = "zywiec";
        iw = dic.getIndexWord(POS.NOUN, lemma);
        assertThat(iw, is(nullValue()));
    }

}