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
        TestAdjSynset.class,
        TestExc.class,
        TestIndexWord.class,
        TestIndexWordSet.class,
        TestRelationshipList.class,
        TestSummary.class,
        TestSynset.class,
        TestVerbSynset.class,
        TestWord.class,

        TestEditFileBackedDictionary.class,
        TestEditMapBackedDictionary.class,
        TestGetDatabaseBackedInstance.class,
        TestGetFileBackedInstance.class,
        TestReadDatabaseBackedDictionary.class,
        TestReadFileBackedDictionary.class,
        TestReadFileChannelBackedDictionary.class,
//        TestReadMapBackedDictionary.class,
        TestThreads.class,
        TestThreadsDictionary.class,
        TestThreadsLock.class
})
public class JWNLSuite {

    public static void main(String[] args) {
        Class[] classes = new Class[]{
                TestAdjSynset.class,
                TestExc.class,
                TestIndexWord.class,
                TestIndexWordSet.class,
                TestRelationshipList.class,
                TestSummary.class,
                TestSynset.class,
                TestVerbSynset.class,
                TestWord.class,

                TestEditFileBackedDictionary.class,
                TestEditMapBackedDictionary.class,
                TestGetDatabaseBackedInstance.class,
                TestGetFileBackedInstance.class,
                TestReadDatabaseBackedDictionary.class,
                TestReadFileBackedDictionary.class,
                TestReadFileChannelBackedDictionary.class,
//        TestReadMapBackedDictionary.class,
                TestThreads.class,
                TestThreadsDictionary.class,
                TestThreadsLock.class
        };
        String[] names = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            names[i] = classes[i].getName();
        }
        org.junit.runner.JUnitCore.main(names);
    }
}