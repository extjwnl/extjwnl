package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestIndexWord extends BaseData {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullLemma() throws JWNLException {
        new IndexWord(null, null, POS.NOUN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullPOS() throws JWNLException {
        new IndexWord(null, "test", null);
    }

    @Test
    public void testConstructor() throws JWNLException {
        dictionary.edit();
        IndexWord iw = new IndexWord(dictionary, "test", POS.NOUN);

        List<IndexWord> indexWords = new ArrayList<IndexWord>(1);
        Iterator<IndexWord> i = dictionary.getIndexWordIterator(POS.NOUN);
        while (i.hasNext()) {
            indexWords.add(i.next());
        }
        Assert.assertEquals(1, indexWords.size());
        Assert.assertEquals(iw, indexWords.get(0));
        Assert.assertNotNull(iw.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullSynset() throws JWNLException {
        new IndexWord(null, "test", POS.NOUN, (Synset) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSynsetPOS() throws JWNLException {
        Synset s = new Synset(null, POS.NOUN);
        new IndexWord(null, "test", POS.VERB, s);
    }

    @Test
    public void testConstructorSynset() throws JWNLException {
        Synset s = new Synset(null, POS.NOUN);
        IndexWord iw = new IndexWord(null, "test", POS.NOUN, s);
        Assert.assertEquals(1, iw.getSenses().size());
        Assert.assertEquals(s, iw.getSenses().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSynsetNullOffset() throws JWNLException {
        new IndexWord(null, "test", POS.VERB, (long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSynsetOffset0() throws JWNLException {
        new IndexWord(null, "test", POS.VERB, new long[] {});
    }

    @Test
    public void testType() throws JWNLException {
        IndexWord iw = new IndexWord(null, "test", POS.NOUN, new long[] {1});
        Assert.assertEquals(DictionaryElementType.INDEX_WORD, iw.getType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSynsetSetNull() throws JWNLException {
        Synset s = new Synset(null, POS.NOUN);
        IndexWord iw = new IndexWord(null, "test", POS.NOUN, s);
        iw.getSenses().set(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSynsetSetAlien() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN);
        IndexWord iw = new IndexWord(dictionary, "test", POS.NOUN, s);
        iw.getSenses().set(0, new Synset(null, POS.NOUN));
    }

    @Test
    public void testSynsetSet() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN);
        s.setGloss("test gloss");
        Synset t = new Synset(dictionary, POS.NOUN);
        t.setGloss("car gloss");
        IndexWord iws = new IndexWord(dictionary, "test", POS.NOUN, s);
        IndexWord iwt = new IndexWord(dictionary, "car", POS.NOUN, t);

        iwt.getSenses().set(0, s);
        Assert.assertTrue(iwt.getSenses().isEmpty());
        Assert.assertEquals(2, s.getWords().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSynsetAddIndexedNull() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN);
        s.setGloss("test gloss");
        IndexWord iws = new IndexWord(dictionary, "test", POS.NOUN, s);

        iws.getSenses().add(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSynsetAddIndexedAlien() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN);
        s.setGloss("test gloss");
        IndexWord iws = new IndexWord(dictionary, "test", POS.NOUN, s);

        iws.getSenses().add(0, new Synset(null, POS.NOUN));
    }

    @Test
    public void testSynsetAdd() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN);
        s.setGloss("test gloss");
        Synset t = new Synset(dictionary, POS.NOUN);
        t.setGloss("car gloss");
        IndexWord iws = new IndexWord(dictionary, "test", POS.NOUN, s);

        iws.getSenses().add(0, t);
        Assert.assertEquals(2, iws.getSenses().size());
        Assert.assertEquals(1, s.getWords().size());
        Assert.assertEquals(1, t.getWords().size());
        Assert.assertTrue(s.containsWord("test"));
        Assert.assertTrue(t.containsWord("test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSynsetAddAlien() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN);
        new IndexWord(null, "test", POS.NOUN, s);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSynsetAddNull() throws JWNLException {
        Synset s = new Synset(null, POS.NOUN);
        IndexWord iw = new IndexWord(null, "test", POS.NOUN, s);
        iw.getSenses().add(null);
    }

    @Test
    public void testSynsetRemove() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN);
        IndexWord iw = new IndexWord(dictionary, "test", POS.NOUN, s);
        iw.getSenses().remove(0);
        Assert.assertEquals(0, iw.getSenses().size());

        List<IndexWord> indexWords = new ArrayList<IndexWord>(1);
        Iterator<IndexWord> i = dictionary.getIndexWordIterator(POS.NOUN);
        while (i.hasNext()) {
            indexWords.add(i.next());
        }
        Assert.assertEquals(0, indexWords.size());
    }

    @Test
    public void testSynsetClear() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN);
        IndexWord iw = new IndexWord(dictionary, "test", POS.NOUN, s);
        iw.getSenses().clear();
        Assert.assertEquals(0, iw.getSenses().size());

        List<IndexWord> indexWords = new ArrayList<IndexWord>(1);
        Iterator<IndexWord> i = dictionary.getIndexWordIterator(POS.NOUN);
        while (i.hasNext()) {
            indexWords.add(i.next());
        }
        Assert.assertEquals(0, indexWords.size());
    }
}
