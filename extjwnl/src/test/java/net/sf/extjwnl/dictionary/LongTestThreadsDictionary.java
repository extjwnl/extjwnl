package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import org.junit.Ignore;

import java.io.IOException;

/**
 * Tests thread locking for this case:
 * https://sourceforge.net/projects/extjwnl/forums/forum/1333399/topic/4772095/index/page/1
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
@Ignore
public class LongTestThreadsDictionary extends MultiThreadedTestCase {

    private static final int threadCount = 5;
    private static final int runCount = 3;

    /**
     * Basic constructor - called by the test runners.
     *
     * @param s s
     */
    public LongTestThreadsDictionary(String s) {
        super(s);
    }

    protected class TestThread extends TestCaseRunnable {

        private Dictionary d;

        public TestThread(Dictionary d) {
            this.d = d;
        }

        @Override
        public void runTestCase() throws JWNLException, CloneNotSupportedException {
            DictionaryReadTester dt = new DictionaryReadTester(d);
            for (int i = 0; i < runCount; i++) {
                dt.runAllTests();
            }
        }
    }

    public void testThreaded() throws IOException, JWNLException {
        Dictionary d = Dictionary.getInstance(
                LongTestThreadsDictionary.class.getResourceAsStream("/test_file_properties.xml"));

        TestCaseRunnable[] runnables = new TestCaseRunnable[threadCount];
        for (int i = 0; i < threadCount; i++) {
            runnables[i] = new TestThread(d);
        }

        runTestCaseRunnables(runnables);
    }
}