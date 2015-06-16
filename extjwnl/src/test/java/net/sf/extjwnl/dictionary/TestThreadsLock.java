package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.Exc;
import net.sf.extjwnl.data.POS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tests thread locking for this case:
 * https://sourceforge.net/projects/extjwnl/forums/forum/1333399/topic/4772095/index/page/1
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class TestThreadsLock extends MultiThreadedTestCase {

    private static final int threadCount = 5;

    /**
     * Basic constructor - called by the test runners.
     *
     * @param name test name
     */
    public TestThreadsLock(String name) {
        super(name);
    }

    protected class LookupThread extends TestCaseRunnable {

        private List<String> list;
        private Dictionary d;

        public LookupThread(Dictionary d, List<String> list) {
            this.d = d;
            this.list = list;
        }

        @Override
        public void runTestCase() throws JWNLException {
            for (String word : list) {
                if (!isInterrupted()) {
                    d.getMorphologicalProcessor().lookupBaseForm(POS.NOUN, word);
                } else {
                    break;
                }
            }
        }
    }

    public void testThreadedLookupFile() throws IOException, JWNLException {
        Dictionary d = Dictionary.getInstance(
                TestThreadsDictionary.class.getResourceAsStream("/test_file_properties.xml"));

        List<String> list = new ArrayList<String>();
        Iterator<Exc> exceptions = d.getExceptionIterator(POS.NOUN);
        while (exceptions.hasNext()) {
            list.add(exceptions.next().getLemma());
        }

        TestCaseRunnable[] runnables = new TestCaseRunnable[threadCount];
        for (int i = 0; i < threadCount; i++) {
            runnables[i] = new LookupThread(d, list);
        }

        runTestCaseRunnables(runnables);
    }

    public void testThreadedLookupResource() throws IOException, JWNLException {
        Dictionary d = Dictionary.getDefaultResourceInstance();

        List<String> list = new ArrayList<String>();
        Iterator<Exc> exceptions = d.getExceptionIterator(POS.NOUN);
        while (exceptions.hasNext()) {
            list.add(exceptions.next().getLemma());
        }

        TestCaseRunnable[] runnables = new TestCaseRunnable[threadCount];
        for (int i = 0; i < threadCount; i++) {
            runnables[i] = new LookupThread(d, list);
        }

        runTestCaseRunnables(runnables);
    }
}