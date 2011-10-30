package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Attempt to reproduce http://sourceforge.net/tracker/?func=detail&aid=3202925&group_id=33824&atid=409470
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 * @author wangyeee
 */
public class TestThreads extends MultiThreadedTestCase {

    private static final Log log = LogFactory.getLog(TestThreads.class);

    protected final String properties = "./src/main/resources/net/sf/extjwnl/file_properties.xml";
    protected final String[] list = {"tank", "cooler", "pile", "storm", "perfect", "crown", "computer science",
            "failure", "pleasure", "black", "Great Pyramid", "dictionary", "throw", "exception",
            "boredom", "file", "index", "list", "apple", "orange", "pear", "find", "treasure", "memory", "good",
            "reproduce", "claw", "feet", "cold", "green", "glee"};

    public TestThreads(String s) {
        super(s);
    }

    public void testThreadedLookupAllIndexWords() throws FileNotFoundException, JWNLException {
        JWNL.initialize(new FileInputStream(properties));

        List<String> words = new ArrayList<String>(Arrays.asList(list));

        TestCaseRunnable t0 = new Lookup(words);
        TestCaseRunnable t1 = new Lookup(words);

        runTestCaseRunnables(new TestCaseRunnable[]{t0, t1});
    }

    private class Lookup extends TestCaseRunnable {

        private List<String> words;

        public Lookup(List<String> words) {
            this.words = words;
        }

        @Override
        public void runTestCase() {
            Dictionary dictionary = Dictionary.getInstance();
            //uncomment this to solve the problem,
            //but I think there's a better way to solve it.
//            synchronized (dictionary) {
            for (String word : words) {
                try {
                    //throws an Exception or just stop at here
                    log.debug("lookup: " + word);
                    IndexWord iws = dictionary.lookupIndexWord(POS.NOUN, word);
                    Assert.assertNotNull(iws);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
//            }
        }
    }
}