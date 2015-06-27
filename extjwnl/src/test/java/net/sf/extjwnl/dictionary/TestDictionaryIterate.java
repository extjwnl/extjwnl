package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Ignore
public abstract class TestDictionaryIterate {

    protected static final Random r = new Random();
    protected static Dictionary s_d;
    protected Dictionary d;

    @Before
    public void setUp() {
        d = s_d;
    }

    @Test
    public void iterateAll() throws JWNLException {
        long start = System.currentTimeMillis();

        for (POS pos : POS.getAllPOS()) {
            Iterator<Exc> ie = d.getExceptionIterator(pos);
            while (ie.hasNext()) {
                ie.next();
            }

            Iterator<IndexWord> ii = d.getIndexWordIterator(pos);
            while (ii.hasNext()) {
                ii.next();
            }

            Iterator<Synset> si = d.getSynsetIterator(pos);
            while (ii.hasNext()) {
                si.next();
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("Iteration (ms):\t" + (end - start));
    }

    @Test
    public void randomWalk() throws JWNLException {
        long start = System.currentTimeMillis();

        POS pos = POS.getPOSForId(r.nextInt(4) + 1);
        Synset s = d.getSynsetIterator(pos).next();

        for (int i = 0; i < 10000; i++) {
            List<Pointer> ptrs = s.getPointers();
            PointerTarget p = ptrs.get(r.nextInt(ptrs.size())).getTarget();
            s = p.getSynset();
        }

        for (int i = 0; i < 10000; i++) {
            IndexWord w = d.getRandomIndexWord(pos);
        }

        long end = System.currentTimeMillis();
        System.out.println("Walk (ms):\t" + (end - start));
    }
}
