package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

/**
 * Tests DefaultMorphologicalProcessor.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestMorphologicalProcessor {

    private Dictionary dic;

    @Before
    public void initDictionary() throws IOException, JWNLException {
        dic = Dictionary.getInstance(
                TestMorphologicalProcessor.class.getResourceAsStream("/test_file_properties.xml"));
    }

    @After
    public void closeDictionary() throws IOException, JWNLException {
        if (null != dic) {
            dic.close();
        }
    }

    /**
     * For https://github.com/extjwnl/extjwnl/issues/6
     * Bug report thanks to Tristan Miller (https://github.com/logological)
     */
    @Test
    public void testRepeatedInvocations() throws JWNLException {
        MorphologicalProcessor mp = dic.getMorphologicalProcessor();
        assertThat(mp.lookupAllBaseForms(POS.NOUN, "guts"), hasItems("gut", "guts"));
        assertThat(mp.lookupAllBaseForms(POS.NOUN, "guts"), hasItems("gut", "guts"));
        assertThat(mp.lookupAllBaseForms(POS.NOUN, "spectacles"), hasItems("spectacle", "spectacles"));
        assertThat(mp.lookupAllBaseForms(POS.NOUN, "spectacles"), hasItems("spectacle", "spectacles"));
    }
}
