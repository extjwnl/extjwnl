package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestIndexWord extends BaseData {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullLemma() throws JWNLException {
        new IndexWord(dictionary, null, POS.NOUN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullPOS() throws JWNLException {
        new IndexWord(dictionary, "test", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor2NullLemma() throws JWNLException {
        new IndexWord(null, null, POS.NOUN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor2NullPOS() throws JWNLException {
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
        new IndexWord(dictionary, "test", POS.NOUN, (Synset) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor2NullSynset() throws JWNLException {
        new IndexWord(null, "test", POS.NOUN, (Synset) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSynsetPOS() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN);
        new IndexWord(dictionary, "test", POS.VERB, s);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor2SynsetPOS() throws JWNLException {
        Synset s = new Synset(null, POS.NOUN);
        new IndexWord(null, "test", POS.VERB, s);
    }

    @Test
    public void testConstructorSynset() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN);
        IndexWord iw = new IndexWord(dictionary, "test", POS.NOUN, s);
        Assert.assertEquals(1, iw.getSenses().size());
        Assert.assertEquals(s, iw.getSenses().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSynsetNullOffset() throws JWNLException {
        new IndexWord(dictionary, "test", POS.VERB, (long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor2SynsetNullOffset() throws JWNLException {
        new IndexWord(null, "test", POS.VERB, (long[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSynsetOffset0() throws JWNLException {
        new IndexWord(dictionary, "test", POS.VERB, new long[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor2SynsetOffset0() throws JWNLException {
        new IndexWord(null, "test", POS.VERB, new long[]{});
    }

    @Test
    public void testType() throws JWNLException {
        IndexWord iw = new IndexWord(dictionary, "test", POS.NOUN, new long[]{1});
        Assert.assertEquals(DictionaryElementType.INDEX_WORD, iw.getType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSynsetSetNull() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN);
        IndexWord iw = new IndexWord(dictionary, "test", POS.NOUN, s);
        iw.getSenses().set(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSynsetSetAlien() throws JWNLException {
        Synset s = new Synset(mapDictionary, POS.NOUN);
        IndexWord iw = new IndexWord(dictionary, "test", POS.NOUN, new Synset(dictionary, POS.NOUN));
        iw.getSenses().set(0, s);
    }

    @Test
    public void testSynsetSet() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        s.setGloss("test gloss");
        Synset t = new Synset(dictionary, POS.NOUN, 2);
        t.setGloss("car gloss");
        IndexWord iwt = new IndexWord(dictionary, "car", POS.NOUN, t);

        iwt.getSenses().set(0, s);
        Assert.assertEquals(1, iwt.getSenses().size());
        Assert.assertEquals(s, iwt.getSenses().get(0));
    }

    @Test
    public void testSynsetSetNullD() throws JWNLException {
        Synset s = new Synset(null, POS.NOUN);
        s.setGloss("test gloss");
        Synset t = new Synset(null, POS.NOUN);
        t.setGloss("car gloss");
        IndexWord iwt = new IndexWord(null, "car", POS.NOUN, t);

        iwt.getSenses().set(0, s);
        Assert.assertEquals(1, iwt.getSenses().size());
        Assert.assertEquals(s, iwt.getSenses().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSynsetAddIndexedNull() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN);
        s.setGloss("test gloss");
        IndexWord iws = new IndexWord(dictionary, "test", POS.NOUN, s);

        iws.getSenses().add(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSynsetAddIndexedAlien() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN);
        s.setGloss("test gloss");
        IndexWord iws = new IndexWord(dictionary, "test", POS.NOUN, s);

        iws.getSenses().add(0, new Synset(mapDictionary, POS.NOUN));
    }

    @Test
    public void testSynsetAdd() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        s.setGloss("test gloss");
        Synset t = new Synset(dictionary, POS.NOUN, 2);
        t.setGloss("car gloss");
        IndexWord iws = new IndexWord(dictionary, "test", POS.NOUN, s);

        iws.getSenses().add(0, t);
        Assert.assertEquals(2, iws.getSenses().size());
        Assert.assertEquals(1, s.getWords().size());
        Assert.assertEquals(1, t.getWords().size());
        Assert.assertTrue(s.containsWord("test"));
        Assert.assertTrue(t.containsWord("test"));
        Assert.assertEquals(1, iws.getSenses().lastIndexOf(s));

        iws.getSenses().remove(t);
        iws.setDictionary(null);
        t.setDictionary(null);
        iws.getSenses().add(0, t);
        Assert.assertEquals(2, iws.getSenses().size());
    }

    @Test
    public void testSynsetAddAllIndexed() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN);
        s.setGloss("test gloss");
        Synset t = new Synset(dictionary, POS.NOUN);
        t.setGloss("car gloss");
        Synset tt = new Synset(dictionary, POS.NOUN);
        tt.setGloss("another car gloss");
        IndexWord iws = new IndexWord(dictionary, "test", POS.NOUN, s);

        iws.getSenses().addAll(0, Arrays.asList(t, tt));
        Assert.assertEquals(3, iws.getSenses().size());
    }

    @Test
    public void testSynsetAddAll() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN);
        s.setGloss("test gloss");
        Synset t = new Synset(dictionary, POS.NOUN);
        t.setGloss("car gloss");
        Synset tt = new Synset(dictionary, POS.NOUN);
        tt.setGloss("another car gloss");
        IndexWord iws = new IndexWord(dictionary, "test", POS.NOUN, s);

        iws.getSenses().addAll(Arrays.asList(t, tt));
        Assert.assertEquals(3, iws.getSenses().size());
    }

    @Test
    public void testSynsetAddAllNull() throws JWNLException {
        Synset s = new Synset(null, POS.NOUN, 1);
        s.setGloss("test gloss");
        Synset t = new Synset(null, POS.NOUN, 2);
        t.setGloss("car gloss");
        Synset tt = new Synset(null, POS.NOUN, 3);
        tt.setGloss("another car gloss");
        IndexWord iws = new IndexWord(null, "test", POS.NOUN, s);

        iws.getSenses().addAll(Arrays.asList(t, tt));
        Assert.assertEquals(3, iws.getSenses().size());
    }

    @Test
    public void testSynsetAddAllIndexedNull() throws JWNLException {
        Synset s = new Synset(null, POS.NOUN, 1);
        s.setGloss("test gloss");
        Synset t = new Synset(null, POS.NOUN, 2);
        t.setGloss("car gloss");
        Synset tt = new Synset(null, POS.NOUN, 3);
        tt.setGloss("another car gloss");
        IndexWord iws = new IndexWord(null, "test", POS.NOUN, s);

        iws.getSenses().addAll(0, Arrays.asList(t, tt));
        Assert.assertEquals(3, iws.getSenses().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSynsetAddAlien() throws JWNLException {
        Synset s = new Synset(mapDictionary, POS.NOUN);
        new IndexWord(dictionary, "test", POS.NOUN, s);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSynsetAddNull() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN);
        IndexWord iw = new IndexWord(dictionary, "test", POS.NOUN, s);
        iw.getSenses().add(null);
    }

    @Test
    public void testSynsetListIterator() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN);
        IndexWord iw = new IndexWord(dictionary, "test", POS.NOUN, s);
        Assert.assertNotNull(iw.getSenses().listIterator());
        Assert.assertTrue(iw.getSenses().listIterator().hasNext());
    }

    @Test
    public void testSynsetListIteratorIndexed() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN);
        IndexWord iw = new IndexWord(dictionary, "test", POS.NOUN, s);
        Assert.assertNotNull(iw.getSenses().listIterator(0));
        Assert.assertTrue(iw.getSenses().listIterator(0).hasNext());
        Assert.assertEquals(s, iw.getSenses().listIterator(0).next());
    }

    @Test
    public void testSynsetSubList() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN);
        IndexWord iw = new IndexWord(dictionary, "test", POS.NOUN, s);
        iw.getSenses().add(new Synset(dictionary, POS.NOUN));
        Assert.assertNotNull(iw.getSenses().subList(0, 1));
        Assert.assertEquals(s, iw.getSenses().subList(0, 1).get(0));
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
    public void testSynsetRemoveNull() throws JWNLException {
        Synset s = new Synset(null, POS.NOUN);
        IndexWord iw = new IndexWord(null, "test", POS.NOUN, s);
        iw.getSenses().remove(0);
        Assert.assertEquals(0, iw.getSenses().size());
    }

    @Test
    public void testSynsetRemoveObjectNull() throws JWNLException {
        Synset s = new Synset(null, POS.NOUN);
        IndexWord iw = new IndexWord(null, "test", POS.NOUN, s);
        iw.getSenses().remove(s);
        Assert.assertEquals(0, iw.getSenses().size());
    }

    @Test
    public void testSynsetRemoveAll() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN);
        IndexWord iw = new IndexWord(dictionary, "test", POS.NOUN, s);
        iw.getSenses().add(new Synset(dictionary, POS.NOUN));
        Assert.assertNotNull(iw.getSenses().removeAll(Arrays.asList(s)));
        Assert.assertFalse(iw.getSenses().contains(s));
    }

    @Test
    public void testSynsetRemoveAllNull() throws JWNLException {
        Synset s = new Synset(null, POS.NOUN, 1);
        IndexWord iw = new IndexWord(null, "test", POS.NOUN, s);
        iw.getSenses().add(new Synset(null, POS.NOUN, 2));
        Assert.assertNotNull(iw.getSenses().removeAll(Arrays.asList(s)));
        Assert.assertFalse(iw.getSenses().contains(s));
    }

    @Test
    public void testSynsetContainsAll() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN);
        IndexWord iw = new IndexWord(dictionary, "test", POS.NOUN, s);
        Synset ss = new Synset(dictionary, POS.NOUN);
        iw.getSenses().add(ss);
        Assert.assertTrue(iw.getSenses().containsAll(Arrays.asList(s, ss)));
    }

    @Test
    public void testSynsetContainsAllNull() throws JWNLException {
        Synset s = new Synset(null, POS.NOUN, 1);
        IndexWord iw = new IndexWord(null, "test", POS.NOUN, s);
        Synset ss = new Synset(null, POS.NOUN, 1);
        iw.getSenses().add(ss);
        Assert.assertTrue(iw.getSenses().containsAll(Arrays.asList(s, ss)));
    }

    @Test
    public void testSynsetRemoveAllEditable() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN);
        IndexWord iw = new IndexWord(dictionary, "test", POS.NOUN, s);
        iw.getSenses().add(new Synset(dictionary, POS.NOUN));
        Assert.assertTrue(iw.getSenses().removeAll(Arrays.asList(s)));
        Assert.assertFalse(iw.getSenses().contains(s));
        Assert.assertNull(dictionary.getIndexWord(POS.NOUN, "test"));
    }

    @Test
    public void testSynsetRetainAll() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        s.setGloss("synset 1");
        IndexWord iw = new IndexWord(dictionary, "test", POS.NOUN, s);
        Synset ss = new Synset(dictionary, POS.NOUN, 2);
        ss.setGloss("synset 2");
        iw.getSenses().add(ss);
        Assert.assertTrue(iw.getSenses().retainAll(Arrays.asList(s)));
        Assert.assertTrue(iw.getSenses().contains(s));
        Assert.assertEquals(1, iw.getSenses().size());

        Assert.assertTrue(iw.getSenses().retainAll(Arrays.asList()));
        Assert.assertNull(dictionary.getIndexWord(POS.NOUN, "test"));
    }

    @Test
    public void testSynsetRetainAllNull() throws JWNLException {
        Synset s = new Synset(null, POS.NOUN, 1);
        s.setGloss("synset 1");
        IndexWord iw = new IndexWord(null, "test", POS.NOUN, s);
        Synset ss = new Synset(null, POS.NOUN, 2);
        ss.setGloss("synset 2");
        iw.getSenses().add(ss);
        Assert.assertTrue(iw.getSenses().retainAll(Arrays.asList(s)));
        Assert.assertTrue(iw.getSenses().contains(s));
        Assert.assertEquals(1, iw.getSenses().size());
    }

    @Test
    public void testSynsetRetainAllRemove() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN);
        IndexWord iw = new IndexWord(dictionary, "test", POS.NOUN, s);
        Assert.assertNotNull(iw.getSenses().retainAll(Arrays.asList()));
        Assert.assertNull(dictionary.getIndexWord(POS.NOUN, "test"));
    }

    @Test
    public void testSynsetClear() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN);
        IndexWord iw = new IndexWord(dictionary, "test", POS.NOUN, s);
        iw.getSenses().clear();
        Assert.assertEquals(0, iw.getSenses().size());

        Assert.assertFalse(dictionary.getIndexWordIterator(POS.NOUN).hasNext());
    }

    @Test
    public void testSynsetClearNull() throws JWNLException {
        Synset s = new Synset(null, POS.NOUN, 1);
        IndexWord iw = new IndexWord(null, "test", POS.NOUN, s);
        iw.getSenses().clear();
        Assert.assertEquals(0, iw.getSenses().size());
    }

    @Test
    public void testSynsetSortSenses() throws JWNLException {
        AdjectiveSynset s = new AdjectiveSynset(dictionary);
        Adjective w = new Adjective(dictionary, s, "test", AdjectivePosition.NONE);
        w.setUseCount(1);
        s.getWords().add(w);

        AdjectiveSynset ss = new AdjectiveSynset(dictionary);
        Adjective ww = new Adjective(dictionary, ss, "test", AdjectivePosition.ATTRIBUTIVE);
        ww.setUseCount(10);
        ss.getWords().add(ww);

        AdjectiveSynset sss = new AdjectiveSynset(dictionary);
        Adjective www = new Adjective(dictionary, sss, "test", AdjectivePosition.NONE);
        www.setUseCount(0);
        sss.getWords().add(www);

        AdjectiveSynset ssss = new AdjectiveSynset(dictionary);
        Adjective wwww = new Adjective(dictionary, ssss, "test", AdjectivePosition.NONE);
        wwww.setUseCount(0);
        ssss.getWords().add(wwww);

        IndexWord iw = new IndexWord(dictionary, "test", POS.ADJECTIVE, s);
        iw.getSenses().add(ss);
        iw.getSenses().add(sss);
        iw.getSenses().add(ssss);
        Assert.assertEquals(2, iw.sortSenses());
    }

    @Test
    public void testSynsetListStream() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN, 123);
        dictionary.addSynset(s);
        s.setGloss("test gloss");
        IndexWord iws = new IndexWord(dictionary, "test", POS.NOUN, new long[]{123});

        Assert.assertEquals(s, iws.getSenses().stream().findFirst().orElse(null));
    }

    @Test
    public void testHashCode() throws JWNLException {
        IndexWord i = new IndexWord(dictionary, "test", POS.NOUN);
        IndexWord ii = new IndexWord(dictionary, "rest", POS.NOUN);

        Assert.assertNotEquals(i.hashCode(), ii.hashCode());
    }
}