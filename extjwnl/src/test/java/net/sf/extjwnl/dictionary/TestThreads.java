package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Assert;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Attempt to reproduce http://sourceforge.net/tracker/?func=detail&aid=3202925&group_id=33824&atid=409470
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 * @author wangyeee
 */
public class TestThreads extends MultiThreadedTestCase {

    private static final Logger log = LoggerFactory.getLogger(TestThreads.class);

    private static Dictionary dictionary;

    protected final String[] list = {"tank", "cooler", "pile", "storm", "perfect", "crown", "computer science",
            "failure", "pleasure", "black", "Great Pyramid", "dictionary", "throw", "exception",
            "boredom", "file", "index", "list", "apple", "orange", "pear", "find", "treasure", "memory", "good",
            "claw", "feet", "cold", "green", "glee"};

    protected final String[] notlist = {"ttank", "ccooler", "ppile", "sstorm", "pperfect", "ccrown", "ccomputer sscience",
            "ffailure", "ppleasure", "bblack", "GGreat PPyramid", "ddictionary", "tthrow", "eexception",
            "bboredom", "ffile", "iindex", "llist", "aapple", "oorange", "ppear", "ffind", "ttreasure", "mmemory", "ggood",
            "cclaw", "ffeet", "ccold", "ggreen", "gglee"};

    public TestThreads(String s) {
        super(s);
    }

    public void testThreadedLookupAllIndexWords() throws FileNotFoundException, JWNLException {
        dictionary = Dictionary.getInstance(TestThreads.class.getResourceAsStream("/test_file_properties.xml"));

        List<String> words = new ArrayList<String>(Arrays.asList(list));
        List<String> notwords = new ArrayList<String>(Arrays.asList(notlist));

        TestCaseRunnable t0 = new Lookup(words, true);
        TestCaseRunnable t1 = new Lookup(words, true);
        TestCaseRunnable t2 = new Lookup(notwords, false);

        runTestCaseRunnables(new TestCaseRunnable[]{t0, t1, t2});
    }

    private class Lookup extends TestCaseRunnable {

        private List<String> words;
        private boolean test;

        public Lookup(List<String> words, boolean test) {
            this.words = words;
            this.test = test;
        }

        @Override
        public void runTestCase() throws JWNLException {
            //uncomment this to solve the problem,
            //but I think there's a better way to solve it.
//            synchronized (dictionary) {
            for (String word : words) {
                if (!isInterrupted()) {
                    //throws an Exception or just stop at here
                    log.debug("lookup: " + word);
                    IndexWord iws = dictionary.lookupIndexWord(POS.NOUN, word);
                    log.debug("finished: " + word);
                    Assert.assertEquals("Can't find: " + word, null != iws, test);
                } else {
                    break;
                }
            }
//            }
        }
    }
}