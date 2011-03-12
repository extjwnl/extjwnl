package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWordSet;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Attempt to reproduce http://sourceforge.net/tracker/?func=detail&aid=3202925&group_id=33824&atid=409470
 *
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 * @author wangyeee
 */
public class TestThreads {

    protected final String properties = "./src/main/config/file_properties.xml";
    protected final String[] list = {"tank", "cooler", "pile", "storm", "perfect", "crown", "computer science",
            "failure", "pleasure", "black", "Great Pyramid", "dictionary", "throw", "exception", "initialize",
            "boredom", "file", "index", "list", "apple", "orange", "pear", "find", "treasure", "memory", "good",
            "reproduce", "claw", "feet", "cold", "green", "glee"};

    @Test
    public void TestThreadedLookupAllIndexWords() throws FileNotFoundException, JWNLException {
        JWNL.initialize(new FileInputStream(properties));

        List<String> words0 = new ArrayList<String>(Arrays.asList(list));
        List<String> words1 = new ArrayList<String>(Arrays.asList(list));

        Thread t0 = new Lookup(words0);
        Thread t1 = new Lookup(words1);
        //I start 2 threads looking up words in wordnet
        t0.start();
        t1.start();

    }

    private class Lookup extends Thread {

        private List<String> words;

        public Lookup(List<String> words) {
            this.words = words;
        }

        @Override
        public void run() {
            Dictionary dictionary = Dictionary.getInstance();
            //uncomment this to solve the problem,
            //but I think there's a better way to solve it.
//            synchronized (dictionary) {
            for (String word : words) {
                try {
                    //throws an Exception or just stop at here
                    IndexWordSet iws = dictionary.lookupAllIndexWords(word);
                    Assert.assertNotNull(iws);
                    Assert.assertTrue(0 < iws.size());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//            }
        }
    }
}
