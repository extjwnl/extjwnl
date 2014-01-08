package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.Exc;
import net.sf.extjwnl.data.POS;
import org.junit.Ignore;
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
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
@Ignore
public class LongTestThreadsLock extends MultiThreadedTestCase {

    private static final Logger log = LoggerFactory.getLogger(LongTestThreadsLock.class);

    private static final int threadCount = 5;

    /**
     * Basic constructor - called by the test runners.
     *
     * @param name test name
     */
    public LongTestThreadsLock(String name) {
        super(name);
    }

    protected class LookupThread extends TestCaseRunnable {

        private String name;
        private List<String> list;
        private Dictionary d;

        public LookupThread(Dictionary d, String name, List<String> list) {
            this.d = d;
            this.name = name;
            this.list = list;
        }

        @Override
        public void runTestCase() throws JWNLException {
            for (String word : list) {
                if (!isInterrupted()) {
                    log.info(name + " querying for " + word);
                    d.getMorphologicalProcessor().lookupBaseForm(POS.NOUN, word);
                    log.info(name + " finished querying for " + word);
                } else {
                    break;
                }
            }
        }
    }

    public void testThreadedLookup() throws IOException, JWNLException {
        Dictionary d = Dictionary.getInstance(
                LongTestThreadsDictionary.class.getResourceAsStream("/test_file_properties.xml"));

        List<String> list = new ArrayList<String>();
        Iterator<Exc> exceptions = d.getExceptionIterator(POS.NOUN);
        while (exceptions.hasNext()) {
            list.add(exceptions.next().getLemma());
        }

        TestCaseRunnable[] runnables = new TestCaseRunnable[threadCount];
        for (int i = 0; i < threadCount; i++) {
            runnables[i] = new LookupThread(d, "t" + (i + 1), list);
        }

        runTestCaseRunnables(runnables);
    }
}