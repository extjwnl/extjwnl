package net.sf.extjwnl;

import net.sf.extjwnl.data.*;
import net.sf.extjwnl.dictionary.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * All tests.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestWord.class,
        TestSynset.class,
        TestExc.class,
        TestSummary.class,
        TestReadFileBackedDictionary.class,
//        TestReadMapBackedDictionary.class,
        TestReadDatabaseBackedDictionary.class,
        TestEditFileBackedDictionary.class,
        TestGetFileBackedInstance.class,
        TestEditMapBackedDictionary.class,
        TestThreads.class,
        TestThreadsLock.class,
        TestThreadsDictionary.class
})
public class JWNLSuite {

    public static void main(String[] args) {
        Class[] classes = new Class[]{
                TestWord.class,
                TestSynset.class,
                TestExc.class,
                TestSummary.class,
                TestReadFileBackedDictionary.class,
//                TestReadMapBackedDictionary.class,
                TestReadDatabaseBackedDictionary.class,
                TestEditFileBackedDictionary.class,
                TestEditMapBackedDictionary.class,
                TestThreads.class,
                TestThreadsLock.class,
                TestThreadsDictionary.class
        };
        String[] names = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            names[i] = classes[i].getName();
        }
        org.junit.runner.JUnitCore.main(names);
    }
}