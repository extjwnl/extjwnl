package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests thread locking for this case:
 * https://sourceforge.net/projects/extjwnl/forums/forum/1333399/topic/4772095/index/page/1
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestThreadsLock extends MultiThreadedTestCase {

    private static final Log log = LogFactory.getLog(TestThreadsLock.class);

    private final String properties = "./src/main/resources/net/sf/extjwnl/file_properties.xml";
    private final String wordlist = "./data/wn30/noun.exc";
    private static Dictionary d;

    /**
     * Basic constructor - called by the test runners.
     *
     * @param s s
     */
    public TestThreadsLock(String s) {
        super(s);
    }

    public static final String TEST_ALL_TEST_TYPE = "UNIT";

    protected class LookupThread extends TestCaseRunnable {

        private String name;
        private List<String> list;

        public LookupThread(String name, List<String> list) {
            this.name = name;
            this.list = list;
        }

        @Override
        public void runTestCase() {
            try {
                for (String word : list) {
                    log.info(name + " querying for " + word);
                    d.getMorphologicalProcessor().lookupBaseForm(POS.NOUN, word);
                    log.info(name + " finished querying for " + word);
                }
            } catch (JWNLException e) {
                e.printStackTrace();
            }
        }
    }

    public void testThreadedLookup() throws IOException, JWNLException {
        JWNL.initialize(new FileInputStream(properties));
        d = Dictionary.getInstance();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(wordlist)));
        List<String> list = new ArrayList<String>();
        String line = reader.readLine();
        while (line != null) {
            list.add(line);
            line = reader.readLine();
        }

        TestCaseRunnable t1 = new LookupThread("t1", list);
        TestCaseRunnable t2 = new LookupThread("t2", list);

        runTestCaseRunnables(new TestCaseRunnable[]{t1, t2});
    }
}