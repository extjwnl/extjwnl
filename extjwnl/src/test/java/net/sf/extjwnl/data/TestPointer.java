package net.sf.extjwnl.data;

import org.junit.Assert;
import net.sf.extjwnl.JWNLException;
import org.junit.Test;

/**
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestPointer extends BaseData {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor1NullSource() {
        new Pointer(null, PointerType.ANTONYM, POS.NOUN, 1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor1NullPointerType() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN);
        new Pointer(s, null, POS.NOUN, 1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor1NullPOS() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN);
        new Pointer(s, PointerType.ANTONYM, null, 1, 0);
    }

    @Test
    public void testConstructor1() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 1, 0);
        Assert.assertNotNull(p);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor2NullPointerType() throws JWNLException {
        new Pointer(null, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor2NullSource() throws JWNLException {
        new Pointer(PointerType.ANTONYM, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor2NullTarget() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN);
        new Pointer(PointerType.ANTONYM, s, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor2Alien() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN);
        Synset t = new Synset(mapDictionary, POS.NOUN);
        new Pointer(PointerType.ANTONYM, s, t);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor2Alien2() throws JWNLException {
        Synset s = new Synset(null, POS.NOUN);
        Synset t = new Synset(mapDictionary, POS.NOUN);
        new Pointer(PointerType.ANTONYM, s, t);
    }

    @Test
    public void testConstructor2Semantic() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Synset t = new Synset(dictionary, POS.ADJECTIVE, 2);
        Pointer p = new Pointer(PointerType.ANTONYM, s, t);
        Assert.assertNotNull(p);
        Assert.assertEquals(s, p.getSource());
        Assert.assertEquals(t, p.getTarget());
        Assert.assertEquals(t, p.getTargetSynset());
        Assert.assertEquals(POS.ADJECTIVE, p.getTargetPOS());
        Assert.assertEquals(2, p.getTargetOffset());
        Assert.assertEquals(PointerType.ANTONYM, p.getType());
        Assert.assertFalse(p.isLexical());
        Assert.assertTrue(p.isSemantic());
    }

    @Test
    public void testConstructor2Lexical() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Word sw = new Word(dictionary, s, "test");
        s.getWords().add(sw);

        Synset t = new Synset(dictionary, POS.ADJECTIVE, 2);
        Word tw = new Word(dictionary, t, "rest");
        t.getWords().add(tw);

        Pointer p = new Pointer(PointerType.ANTONYM, sw, tw);
        Assert.assertNotNull(p);
        Assert.assertEquals(sw, p.getSource());
        Assert.assertEquals(tw, p.getTarget());
        Assert.assertEquals(t, p.getTargetSynset());
        Assert.assertEquals(POS.ADJECTIVE, p.getTargetPOS());
        Assert.assertEquals(2, p.getTargetOffset());
        Assert.assertEquals(PointerType.ANTONYM, p.getType());
        Assert.assertTrue(p.isLexical());
        Assert.assertFalse(p.isSemantic());
    }

    @Test
    public void testToString() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Synset t = new Synset(dictionary, POS.ADJECTIVE, 2);
        Pointer p = new Pointer(PointerType.ANTONYM, s, t);
        Assert.assertNotNull(p.toString());
    }

    @Test
    public void testSetTarget() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Word sw = new Word(dictionary, s, "test");
        s.getWords().add(sw);

        Synset t = new Synset(dictionary, POS.ADJECTIVE, 2);
        Word tw = new Word(dictionary, t, "rest");
        t.getWords().add(tw);

        Pointer p = new Pointer(PointerType.ANTONYM, sw, tw);
        p.setTarget(t);

        Assert.assertEquals(t, p.getTarget());
        Assert.assertEquals(t, p.getTargetSynset());
        Assert.assertFalse(p.isLexical());
        Assert.assertFalse(p.isSemantic());
    }

    @Test
    public void testGetTargetNull() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 3, 0);

        Assert.assertNull(p.getTarget());
    }

    @Test
    public void testGetTargetSynsetNull() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 3, 0);

        Assert.assertNull(p.getTargetSynset());
    }

    @Test
    public void testGetTarget() throws JWNLException {
        dictionary.edit();
        Synset s = dictionary.createSynset(POS.NOUN);
        Synset t = dictionary.createSynset(POS.NOUN);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, t.getOffset(), 0);

        Assert.assertEquals(t, p.getTarget());
    }

    @Test
    public void testGetTargetWord() throws JWNLException {
        dictionary.edit();
        Synset s = dictionary.createSynset(POS.NOUN);
        Synset t = dictionary.createSynset(POS.NOUN);
        Word tw = new Word(dictionary, t, "test");
        t.getWords().add(tw);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, t.getOffset(), 1);

        Assert.assertEquals(tw, p.getTarget());
        Assert.assertEquals(t, p.getTargetSynset());
    }

    @Test
    public void testGetTargetOffset() throws JWNLException {
        Synset s = new Synset(null, POS.NOUN, 1);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 2, 0);

        Assert.assertEquals(2, p.getTargetOffset());
    }

    @Test
    public void testGetTargetOffsetEditable() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Synset t = new Synset(dictionary, POS.NOUN, 2);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 2, 0);

        Assert.assertEquals(2, p.getTargetOffset());
    }

    @Test
    public void testGetTargetIndexWord() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Synset t = new Synset(dictionary, POS.NOUN, 2);
        Word tw = new Word(dictionary, t, "test");
        t.getWords().add(tw);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 2, 1);

        Assert.assertEquals(1, p.getTargetIndex());
    }

    @Test
    public void testGetTargetIndex() throws JWNLException {
        Synset s = new Synset(null, POS.NOUN, 1);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 2, 0);

        Assert.assertEquals(0, p.getTargetIndex());
    }

    @Test
    public void testGetTargetIndexEdit() throws JWNLException {
        dictionary.edit();
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Synset t = new Synset(dictionary, POS.NOUN, 2);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 2, 0);

        Assert.assertEquals(0, p.getTargetIndex());
    }

    @Test
    public void testGetTargetPOS() throws JWNLException {
        Synset s = new Synset(null, POS.NOUN, 1);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 2, 0);

        Assert.assertEquals(POS.NOUN, p.getTargetPOS());
    }

    @Test
    public void testGetTargetPOSEdit() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Synset t = new Synset(dictionary, POS.NOUN, 2);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 2, 0);

        Assert.assertEquals(POS.NOUN, p.getTargetPOS());
    }

    @Test
    public void testHashCode() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Synset t = new Synset(dictionary, POS.NOUN, 2);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 10, 0);
        Pointer pp = new Pointer(t, PointerType.ANTONYM, POS.NOUN, 20, 0);

        Assert.assertNotEquals(p.hashCode(), pp.hashCode());
    }

    @Test
    public void testEqualsType() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Synset t = new Synset(dictionary, POS.NOUN, 2);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 10, 0);

        Assert.assertFalse(p.equals(t));
    }

    @Test
    public void testEqualsSource() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Synset t = new Synset(dictionary, POS.NOUN, 2);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 10, 0);
        Pointer pp = new Pointer(t, PointerType.ANTONYM, POS.NOUN, 20, 0);

        Assert.assertFalse(p.equals(pp));
    }

    @Test
    public void testEqualsTargetIdx() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Synset t = new Synset(dictionary, POS.NOUN, 2);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 10, 0);
        Pointer pp = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 10, 0);

        Assert.assertTrue(p.equals(pp));
    }

    @Test
    public void testEqualsTargetIdxFalse() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 10, 0);
        Pointer pp = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 20, 0);

        Assert.assertFalse(p.equals(pp));
    }

    @Test
    public void testEqualsTargetIdxIdxFalse() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 10, 0);
        Pointer pp = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 10, 1);

        Assert.assertFalse(p.equals(pp));
    }

    @Test
    public void testEqualsTargetIdxPOSFalse() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 10, 0);
        Pointer pp = new Pointer(s, PointerType.ANTONYM, POS.ADJECTIVE, 10, 0);

        Assert.assertFalse(p.equals(pp));
    }

    @Test
    public void testEqualsTargetIdxSelf() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Pointer p = new Pointer(s, PointerType.ANTONYM, POS.NOUN, 10, 0);

        Assert.assertTrue(p.equals(p));
    }

    @Test
    public void testEqualsTargetFalse() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Synset t = new Synset(dictionary, POS.NOUN, 2);
        Synset tt = new Synset(dictionary, POS.NOUN, 3);
        Pointer p = new Pointer(PointerType.ANTONYM, s, t);
        Pointer pp = new Pointer(PointerType.ANTONYM, s, tt);

        Assert.assertFalse(p.equals(pp));
    }

    @Test
    public void testEqualsTargetTrue() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Synset t = new Synset(dictionary, POS.NOUN, 2);
        Pointer p = new Pointer(PointerType.ANTONYM, s, t);
        Pointer pp = new Pointer(PointerType.ANTONYM, s, t);

        Assert.assertTrue(p.equals(pp));
    }

    @Test
    public void testIsSymmetricTrue() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Synset t = new Synset(dictionary, POS.NOUN, 2);
        Pointer p = new Pointer(PointerType.ANTONYM, s, t);
        Pointer pp = new Pointer(PointerType.ANTONYM, t, s);

        Assert.assertTrue(p.isSymmetricTo(pp));
        Assert.assertTrue(pp.isSymmetricTo(p));
    }

    @Test
    public void testIsSymmetricFalse() throws JWNLException {
        Synset s = new Synset(dictionary, POS.NOUN, 1);
        Synset t = new Synset(dictionary, POS.NOUN, 2);
        Pointer p = new Pointer(PointerType.ANTONYM, s, t);
        Pointer pp = new Pointer(PointerType.ANTONYM, s, t);

        Assert.assertFalse(p.isSymmetricTo(pp));
        Assert.assertFalse(pp.isSymmetricTo(p));
    }
}
