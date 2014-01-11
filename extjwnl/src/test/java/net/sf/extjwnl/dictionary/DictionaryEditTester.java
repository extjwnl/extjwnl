package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import org.junit.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Tests dictionary editing.
 *
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
@Ignore
public abstract class DictionaryEditTester {

    private final static String entityGloss = "that which is perceived or known or inferred to have its own distinct existence (living or nonliving)";
    private final static String entityLemma = "entity";
    private final static String physical_entityGloss = "an entity that has physical existence";
    private final static String physical_entityLemma = "physìcal entìty";//ì to test encoding
    private final static String abstractionGloss = "a general concept formed by extracting common features from specific examples";
    private final static String[] abstractionWords = {"abstraction", "abstract entity"};

    private final static String[] exception1 = {"alto-relievos", "alto-relievo", "alto-rilievo"};
    private final static String[] exception2 = {"aìdes-de-camp", "aìde-de-camp"};//ì to test encoding
    private final static String[] exception3 = {"altocumuli", "altocumulus"};//to test sorting

    private Dictionary dictionary;
    private Dictionary mapDictionary;

    protected abstract InputStream getProperties();

    @Before
    public void setUp() throws IOException, JWNLException {
        // clean up files left previously
        dictionary = Dictionary.getInstance(getProperties());
        dictionary.close();
        dictionary.delete();

        dictionary = Dictionary.getInstance(getProperties());
        mapDictionary = Dictionary.getResourceInstance("/net/sf/extjwnl/dictionary/mem_properties.xml");
    }

    @After
    public void tearDown() throws IOException, JWNLException {
        dictionary.close();
        dictionary.delete();
    }

    @Test(expected = JWNLException.class)
    public void testSaveReadOnly() throws JWNLException {
        dictionary.save();
    }

    @Test(expected = JWNLException.class)
    public void testAddElementReadOnly() throws JWNLException {
        dictionary.addElement(null);
    }

    @Test(expected = JWNLException.class)
    public void testAddElementAlien() throws JWNLException {
        dictionary.addElement(new Synset(dictionary, POS.NOUN));
    }

    @Test
    public void testAddElementExc() throws JWNLException {
        dictionary.edit();
        final Exc exc = new Exc(dictionary, POS.NOUN, "test", Arrays.asList("tests"));
        dictionary.addElement(exc);

        List<Exc> excs = new ArrayList<Exc>(1);
        Iterator<Exc> i = dictionary.getExceptionIterator(POS.NOUN);
        while (i.hasNext()) {
            excs.add(i.next());
        }
        Assert.assertEquals(1, excs.size());
        Assert.assertEquals(exc, excs.get(0));
    }

    @Test
    public void testAddElementIndexWord() throws JWNLException {
        dictionary.edit();
        final Synset s = new Synset(dictionary, POS.NOUN);
        final IndexWord iw = new IndexWord(dictionary, "test", POS.NOUN, s);
        dictionary.addElement(iw);

        List<IndexWord> indexWords = new ArrayList<IndexWord>(1);
        Iterator<IndexWord> i = dictionary.getIndexWordIterator(POS.NOUN);
        while (i.hasNext()) {
            indexWords.add(i.next());
        }
        Assert.assertEquals(1, indexWords.size());
        Assert.assertEquals(iw, indexWords.get(0));

        List<Synset> synsets = new ArrayList<Synset>(1);
        Iterator<Synset> ii = dictionary.getSynsetIterator(POS.NOUN);
        while (ii.hasNext()) {
            synsets.add(ii.next());
        }
        Assert.assertEquals(1, synsets.size());
        Assert.assertEquals(s, synsets.get(0));
    }

    @Test(expected = JWNLException.class)
    public void testRemoveReadOnly() throws JWNLException {
        dictionary.removeElement(null);
    }

    @Test(expected = JWNLException.class)
    public void testCreateExceptionReadOnly() throws JWNLException {
        dictionary.createException(null, null, null);
    }

    @Test(expected = JWNLException.class)
    public void testAddExceptionReadOnly() throws JWNLException {
        dictionary.addException(null);
    }

    @Test(expected = JWNLException.class)
    public void testAddExceptionAlien() throws JWNLException {
        dictionary.addException(new Exc(dictionary, POS.NOUN, "test", Arrays.asList("tests")));
    }

    @Test(expected = JWNLException.class)
    public void testRemoveExceptionReadOnly() throws JWNLException {
        dictionary.removeException(new Exc(dictionary, POS.NOUN, "test", Arrays.asList("tests")));
    }

    @Test(expected = JWNLException.class)
    public void testCreateSynsetReadOnly() throws JWNLException {
        dictionary.createSynset(null);
    }

    @Test
    public void testCreateVerbSynset() throws JWNLException {
        dictionary.edit();
        Assert.assertTrue(dictionary.createSynset(POS.VERB) instanceof VerbSynset);
    }

    @Test
    public void testCreateAdjectiveSynset() throws JWNLException {
        dictionary.edit();
        Assert.assertTrue(dictionary.createSynset(POS.ADJECTIVE) instanceof AdjectiveSynset);
    }

    @Test
    public void testCreateNounSynset() throws JWNLException {
        dictionary.edit();
        Assert.assertNotNull(dictionary.createSynset(POS.NOUN));
    }

    @Test(expected = JWNLException.class)
    public void testAddSynsetReadOnly() throws JWNLException {
        dictionary.addSynset(null);
    }

    @Test
    public void testAddSynsetAlien() throws JWNLException {
        dictionary.edit();
        mapDictionary.edit();
        Synset synset = new Synset(mapDictionary, POS.NOUN, 100);
        dictionary.addSynset(synset);
        Assert.assertEquals(dictionary, synset.getDictionary());
        Assert.assertEquals(synset, dictionary.getSynsetAt(POS.NOUN, 100));
        Assert.assertNull(mapDictionary.getSynsetAt(POS.NOUN, 100));
    }

    @Test(expected = JWNLException.class)
    public void testRemoveSynsetReadOnly() throws JWNLException {
        dictionary.removeSynset(new Synset(dictionary, POS.NOUN));
    }

    @Test(expected = JWNLException.class)
    public void testCreateIndexWordReadOnly() throws JWNLException {
        dictionary.createIndexWord(null, null, null);
    }

    @Test(expected = JWNLException.class)
    public void testAddIndexWordReadOnly() throws JWNLException {
        dictionary.addIndexWord(null);
    }

    @Test(expected = JWNLException.class)
    public void testAddIndexWordAlien() throws JWNLException {
        dictionary.addIndexWord(new IndexWord(mapDictionary, "test", POS.NOUN, new Synset(mapDictionary, POS.NOUN)));
    }

    @Test(expected = JWNLException.class)
    public void testRemoveIndexWordReadOnly() throws JWNLException {
        dictionary.removeIndexWord(new IndexWord(dictionary, "test", POS.NOUN, new Synset(dictionary, POS.NOUN)));
    }

    @Test
    public void testEmptyDictionary() throws IOException, JWNLException {
        for (POS pos : POS.getAllPOS()) {
            int synsetCount = 0;
            Iterator<Synset> si = dictionary.getSynsetIterator(pos);
            while (si.hasNext()) {
                synsetCount++;
            }
            Assert.assertEquals(0, synsetCount);

            int iwCount = 0;
            Iterator<IndexWord> ii = dictionary.getIndexWordIterator(pos);
            while (ii.hasNext()) {
                iwCount++;
            }
            Assert.assertEquals(0, iwCount);

            int excCount = 0;
            Iterator<Exc> ei = dictionary.getExceptionIterator(pos);
            while (ei.hasNext()) {
                excCount++;
            }
            Assert.assertEquals(0, excCount);
        }
    }

    @Test
    public void testExceptionsCreate() throws JWNLException, FileNotFoundException {
        dictionary.edit();

        createExceptions(dictionary);
        checkThreeExceptions(dictionary);
    }

    @Test
    public void testExceptionsRecreate() throws JWNLException, IOException {
        dictionary.edit();

        createExceptions(dictionary);
        saveAndReloadDictionary();
        checkThreeExceptions(dictionary);
    }

    @Test
    public void testExceptionsSetNull() throws JWNLException {
        dictionary.edit();

        createExceptions(dictionary);
        checkThreeExceptions(dictionary);

        Exc e3 = dictionary.getException(POS.NOUN, exception3[0]);
        e3.setDictionary(null);
        Assert.assertNull(e3.getDictionary());
        testTwoExceptions(dictionary);
        Assert.assertNull(dictionary.getException(POS.NOUN, exception3[0]));
    }

    @Test
    public void testExceptionsSetNullRecreate() throws JWNLException, FileNotFoundException {
        dictionary.edit();

        createExceptions(dictionary);
        checkThreeExceptions(dictionary);

        Exc e3 = dictionary.getException(POS.NOUN, exception3[0]);
        e3.setDictionary(null);
        Assert.assertNull(e3.getDictionary());

        saveAndReloadDictionary();
        testTwoExceptions(dictionary);
        Assert.assertNull(dictionary.getException(POS.NOUN, exception3[0]));
    }

    @Test
    public void testExceptionsRemove() throws JWNLException {
        dictionary.edit();

        createExceptions(dictionary);
        checkThreeExceptions(dictionary);

        Exc e3 = dictionary.getException(POS.NOUN, exception3[0]);
        dictionary.removeException(e3);

        Assert.assertNull(e3.getDictionary());
        testTwoExceptions(dictionary);
        e3 = dictionary.getException(POS.NOUN, exception3[0]);
        Assert.assertNull(e3);
    }

    @Test
    public void testExceptionsRemoveRecreate() throws JWNLException, IOException {
        dictionary.edit();

        createExceptions(dictionary);
        checkThreeExceptions(dictionary);

        Exc e3 = dictionary.getException(POS.NOUN, exception3[0]);
        dictionary.removeException(e3);
        Assert.assertNull(e3.getDictionary());
        testTwoExceptions(dictionary);
        e3 = dictionary.getException(POS.NOUN, exception3[0]);
        Assert.assertNull(e3);

        saveAndReloadDictionary();

        testTwoExceptions(dictionary);
        e3 = dictionary.getException(POS.NOUN, exception3[0]);
        Assert.assertNull(e3);
    }

    private void saveAndReloadDictionary() throws JWNLException, FileNotFoundException {
        if (!(dictionary instanceof MemoryDictionary)) {
            dictionary.save();
            dictionary.close();
            dictionary = Dictionary.getInstance(getProperties());
        }
    }

    private void createExceptions(Dictionary dictionary) throws JWNLException {
        dictionary.createException(POS.NOUN, exception1[0], Arrays.asList(exception1[1], exception1[2]));
        dictionary.createException(POS.NOUN, exception2[0], Arrays.asList(exception2[1]));
        dictionary.createException(POS.NOUN, exception3[0], Arrays.asList(exception3[1]));
    }

    private void checkThreeExceptions(Dictionary dictionary) throws JWNLException {
        Exc e1 = dictionary.getException(POS.NOUN, exception1[0]);
        Exc e2 = dictionary.getException(POS.NOUN, exception2[0]);
        Exc e3 = dictionary.getException(POS.NOUN, exception3[0]);

        Assert.assertNotNull(e1);
        Assert.assertEquals(exception1[0], e1.getLemma());
        Assert.assertEquals(2, e1.getExceptions().size());
        Assert.assertEquals(exception1[1], e1.getExceptions().get(0));
        Assert.assertEquals(exception1[2], e1.getExceptions().get(1));
        Assert.assertNotNull(e2);
        Assert.assertNotNull(e3);

        List<Exc> exceptions = new ArrayList<Exc>(3);
        Iterator<Exc> ei = dictionary.getExceptionIterator(POS.NOUN);
        while (ei.hasNext()) {
            exceptions.add(ei.next());
        }
        Assert.assertEquals(3, exceptions.size());
        Assert.assertTrue(exceptions.contains(e1));
        Assert.assertTrue(exceptions.contains(e2));
        Assert.assertTrue(exceptions.contains(e3));
    }

    private void testTwoExceptions(Dictionary dictionary) throws JWNLException {
        Exc e1 = dictionary.getException(POS.NOUN, exception1[0]);
        Exc e2 = dictionary.getException(POS.NOUN, exception2[0]);

        Assert.assertNotNull(e1);
        Assert.assertEquals(exception1[0], e1.getLemma());
        Assert.assertEquals(2, e1.getExceptions().size());
        Assert.assertEquals(exception1[1], e1.getExceptions().get(0));
        Assert.assertEquals(exception1[2], e1.getExceptions().get(1));
        Assert.assertNotNull(e2);

        List<Exc> exceptions = new ArrayList<Exc>(2);
        Iterator<Exc> ei = dictionary.getExceptionIterator(POS.NOUN);
        while (ei.hasNext()) {
            exceptions.add(ei.next());
        }
        Assert.assertEquals(2, exceptions.size());
        Assert.assertTrue(exceptions.contains(e1));
        Assert.assertTrue(exceptions.contains(e2));
    }

    @Test
    public void testCreateSynset() throws JWNLException {
        dictionary.edit();

        Synset synEntity = dictionary.createSynset(POS.NOUN);
        Assert.assertNotNull(synEntity);
        Assert.assertEquals(POS.NOUN, synEntity.getPOS());
        Assert.assertTrue(-1 < synEntity.getOffset());
        Assert.assertEquals(0, synEntity.getPointers().size());
        Assert.assertEquals(0, synEntity.getWords().size());

        ArrayList<Synset> synsets = new ArrayList<Synset>();
        Iterator<Synset> si = dictionary.getSynsetIterator(POS.NOUN);
        while (si.hasNext()) {
            synsets.add(si.next());
        }
        Assert.assertEquals(1, synsets.size());
        Assert.assertTrue(synsets.contains(synEntity));

        synEntity.setGloss(entityGloss);
        Assert.assertEquals(entityGloss, synEntity.getGloss());

        int iwCount = 0;
        Iterator<IndexWord> ii = dictionary.getIndexWordIterator(POS.NOUN);
        while (ii.hasNext()) {
            iwCount++;
        }
        Assert.assertEquals(0, iwCount);
    }

    @Test
    public void testCreateSynsetRecreate() throws JWNLException, FileNotFoundException {
        dictionary.edit();

        Synset synEntity = dictionary.createSynset(POS.NOUN);
        synEntity.setGloss(entityGloss);

        saveAndReloadDictionary();

        ArrayList<Synset> synsets = new ArrayList<Synset>();
        Iterator<Synset> si = dictionary.getSynsetIterator(POS.NOUN);
        while (si.hasNext()) {
            synsets.add(si.next());
        }
        Assert.assertEquals(1, synsets.size());
        Assert.assertTrue(synsets.contains(synEntity));

        synEntity = synsets.get(0);

        Assert.assertNotNull(synEntity);
        Assert.assertEquals(POS.NOUN, synEntity.getPOS());
        Assert.assertTrue(-1 < synEntity.getOffset());
        Assert.assertEquals(0, synEntity.getPointers().size());
        Assert.assertEquals(0, synEntity.getWords().size());
        Assert.assertEquals(entityGloss, synEntity.getGloss());

        int iwCount = 0;
        Iterator<IndexWord> ii = dictionary.getIndexWordIterator(POS.NOUN);
        while (ii.hasNext()) {
            iwCount++;
        }
        Assert.assertEquals(0, iwCount);
    }

    @Test
    public void testCreateWord() throws JWNLException {
        dictionary.edit();
        createEntityWord(dictionary);
        checkEntityWord(dictionary);
    }

    @Test
    public void testCreateWordRecreate() throws JWNLException, FileNotFoundException {
        dictionary.edit();
        createEntityWord(dictionary);
        saveAndReloadDictionary();
        checkEntityWord(dictionary);
    }

    private void createEntityWord(Dictionary dictionary) throws JWNLException {
        Synset synEntity = dictionary.createSynset(POS.NOUN);
        synEntity.setGloss(entityGloss);
        synEntity.getWords().add(new Word(dictionary, synEntity, 1, entityLemma));
    }

    private void checkEntityWord(Dictionary dictionary) throws JWNLException {
        IndexWord iwEntity = dictionary.getIndexWord(POS.NOUN, entityLemma);
        Assert.assertNotNull(iwEntity);
        Assert.assertFalse(iwEntity.getSenses().isEmpty());
        Assert.assertEquals(1, iwEntity.getSenses().size());
        Assert.assertEquals(entityLemma, iwEntity.getLemma());
        Assert.assertEquals(POS.NOUN, iwEntity.getPOS());
        Assert.assertNotNull(iwEntity.getSynsetOffsets());
        Assert.assertEquals(1, iwEntity.getSynsetOffsets().length);

        ArrayList<IndexWord> indexWords = new ArrayList<IndexWord>();
        Iterator<IndexWord> ii = dictionary.getIndexWordIterator(POS.NOUN);
        while (ii.hasNext()) {
            indexWords.add(ii.next());
        }
        Assert.assertTrue(indexWords.contains(iwEntity));

        Synset synEntity = iwEntity.getSenses().get(0);
        Assert.assertEquals(1, synEntity.getWords().size());
        Assert.assertNotNull(synEntity.getWords().get(0));
        Assert.assertEquals(entityLemma, synEntity.getWords().get(0).getLemma());
        Assert.assertEquals(1, synEntity.getWords().get(0).getIndex());
        Assert.assertEquals(POS.NOUN, synEntity.getWords().get(0).getPOS());
        Assert.assertEquals(synEntity, synEntity.getWords().get(0).getSynset());
    }

    private void createPEntityWord(Dictionary dictionary) throws JWNLException {
        Synset synPEntity = dictionary.createSynset(POS.NOUN);
        synPEntity.setGloss(physical_entityGloss);
        synPEntity.getWords().add(new Word(dictionary, synPEntity, 1, physical_entityLemma));
    }

    private void checkPEntityWord(Dictionary dictionary) throws JWNLException {
        IndexWord iwpEntity = dictionary.getIndexWord(POS.NOUN, physical_entityLemma);
        Assert.assertNotNull(iwpEntity);
        Assert.assertEquals(1, iwpEntity.getSenses().size());
        Assert.assertEquals(physical_entityLemma, iwpEntity.getLemma());
        Assert.assertEquals(POS.NOUN, iwpEntity.getPOS());
        Assert.assertNotNull(iwpEntity.getSynsetOffsets());
        Assert.assertEquals(1, iwpEntity.getSynsetOffsets().length);

        ArrayList<IndexWord> indexWords = new ArrayList<IndexWord>();
        Iterator<IndexWord> ii = dictionary.getIndexWordIterator(POS.NOUN);
        while (ii.hasNext()) {
            indexWords.add(ii.next());
        }
        Assert.assertTrue(indexWords.contains(iwpEntity));

        Synset synPEntity = iwpEntity.getSenses().get(0);
        Assert.assertEquals(1, synPEntity.getWords().size());
        Assert.assertNotNull(synPEntity.getWords().get(0));
        Assert.assertEquals(physical_entityLemma, synPEntity.getWords().get(0).getLemma());
        Assert.assertEquals(1, synPEntity.getWords().get(0).getIndex());
        Assert.assertEquals(POS.NOUN, synPEntity.getWords().get(0).getPOS());
        Assert.assertEquals(synPEntity, synPEntity.getWords().get(0).getSynset());
    }

    @Test
    public void testCreatePointer() throws JWNLException {
        dictionary.edit();

        createEntityWord(dictionary);
        createPEntityWord(dictionary);
        createEntityPEntityPointer(dictionary);

        checkEntityWord(dictionary);
        checkPEntityWord(dictionary);
        checkEntityPEntityPointer(dictionary);
        checkIterators(dictionary);
    }

    @Test
    public void testCreatePointerRecreate() throws JWNLException, FileNotFoundException {
        dictionary.edit();

        createEntityWord(dictionary);
        createPEntityWord(dictionary);
        createEntityPEntityPointer(dictionary);

        saveAndReloadDictionary();

        checkEntityWord(dictionary);
        checkPEntityWord(dictionary);
        checkEntityPEntityPointer(dictionary);
        checkIterators(dictionary);
    }

    private void createEntityPEntityPointer(Dictionary dictionary) throws JWNLException {
        IndexWord iwEntity = dictionary.getIndexWord(POS.NOUN, entityLemma);
        Synset synEntity = iwEntity.getSenses().get(0);
        IndexWord iwPEntity = dictionary.getIndexWord(POS.NOUN, physical_entityLemma);
        Synset synPEntity = iwPEntity.getSenses().get(0);

        Assert.assertEquals(0, synEntity.getPointers().size());
        Assert.assertEquals(0, synPEntity.getPointers().size());

        synPEntity.getPointers().add(new Pointer(PointerType.HYPERNYM, synPEntity, synEntity));
    }

    private void checkEntityPEntityPointer(Dictionary dictionary) throws JWNLException {
        IndexWord iwEntity = dictionary.getIndexWord(POS.NOUN, entityLemma);
        Synset synEntity = iwEntity.getSenses().get(0);
        IndexWord iwPEntity = dictionary.getIndexWord(POS.NOUN, physical_entityLemma);
        Synset synPEntity = iwPEntity.getSenses().get(0);

        //direct pointer
        Assert.assertEquals(1, synPEntity.getPointers().size());
        Assert.assertNotNull(synPEntity.getPointers().get(0));
        Assert.assertEquals(PointerType.HYPERNYM, synPEntity.getPointers().get(0).getType());
        Assert.assertEquals(synPEntity, synPEntity.getPointers().get(0).getSource());
        Assert.assertEquals(synEntity, synPEntity.getPointers().get(0).getTarget());

        //reverse pointer
        Assert.assertEquals(1, synEntity.getPointers().size());
        Assert.assertNotNull(synEntity.getPointers().get(0));
        Assert.assertEquals(PointerType.HYPONYM, synEntity.getPointers().get(0).getType());
        Assert.assertEquals(synEntity, synEntity.getPointers().get(0).getSource());
        Assert.assertEquals(synPEntity, synEntity.getPointers().get(0).getTarget());
    }

    @Test
    public void testIndexWordRemove() throws IOException, JWNLException {
        dictionary.edit();

        createEntityWord(dictionary);
        createPEntityWord(dictionary);
        createEntityPEntityPointer(dictionary);
        createAbstractionWords(dictionary);
        testAbstractionWords(dictionary);

        IndexWord iwAbstraction = dictionary.getIndexWord(POS.NOUN, abstractionWords[1]);
        dictionary.removeIndexWord(iwAbstraction);
        iwAbstraction = dictionary.getIndexWord(POS.NOUN, abstractionWords[1]);
        Assert.assertNull(iwAbstraction);

        iwAbstraction = dictionary.getIndexWord(POS.NOUN, abstractionWords[0]);
        Synset synAbstraction = iwAbstraction.getSenses().get(0);
        Assert.assertEquals(1, synAbstraction.getWords().size());
    }

    @Test
    public void testIndexWordRemoveRecreate() throws IOException, JWNLException {
        dictionary.edit();

        createEntityWord(dictionary);
        createPEntityWord(dictionary);
        createEntityPEntityPointer(dictionary);
        createAbstractionWords(dictionary);
        testAbstractionWords(dictionary);

        IndexWord iwAbstraction = dictionary.getIndexWord(POS.NOUN, abstractionWords[1]);
        dictionary.removeIndexWord(iwAbstraction);

        saveAndReloadDictionary();

        iwAbstraction = dictionary.getIndexWord(POS.NOUN, abstractionWords[1]);
        Assert.assertNull(iwAbstraction);
        iwAbstraction = dictionary.getIndexWord(POS.NOUN, abstractionWords[0]);
        Synset synAbstraction = iwAbstraction.getSenses().get(0);
        Assert.assertEquals(1, synAbstraction.getWords().size());
    }

    @Test
    public void testUseCount() throws IOException, JWNLException {
        dictionary.edit();

        createEntityWord(dictionary);
        createPEntityWord(dictionary);
        createEntityPEntityPointer(dictionary);
        createAbstractionWords(dictionary);
        saveAndReloadDictionary();
        testAbstractionWords(dictionary);
    }

    @Test
    public void testIndexWordSubstringIterator() throws IOException, JWNLException {
        dictionary.edit();

        createEntityWord(dictionary);
        createPEntityWord(dictionary);
        createEntityPEntityPointer(dictionary);
        createAbstractionWords(dictionary);

        Iterator<IndexWord> i = dictionary.getIndexWordIterator(POS.NOUN, "abstract");
        List<IndexWord> indexWords = new ArrayList<IndexWord>(2);
        List<String> lemmas = new ArrayList<String>(2);
        while (i.hasNext()) {
            indexWords.add(i.next());
            lemmas.add(indexWords.get(indexWords.size() - 1).getLemma());
        }
        Assert.assertEquals(2, indexWords.size());
        Assert.assertTrue(lemmas.contains(abstractionWords[0]));
        Assert.assertTrue(lemmas.contains(abstractionWords[1]));
    }

    @Test
    public void testIndexWordSubstringIteratorRecreate() throws IOException, JWNLException {
        dictionary.edit();

        createEntityWord(dictionary);
        createPEntityWord(dictionary);
        createEntityPEntityPointer(dictionary);
        createAbstractionWords(dictionary);

        saveAndReloadDictionary();

        Iterator<IndexWord> i = dictionary.getIndexWordIterator(POS.NOUN, "abstract");
        List<IndexWord> indexWords = new ArrayList<IndexWord>(2);
        List<String> lemmas = new ArrayList<String>(2);
        while (i.hasNext()) {
            indexWords.add(i.next());
            lemmas.add(indexWords.get(indexWords.size() - 1).getLemma());
        }
        Assert.assertEquals(2, indexWords.size());
        Assert.assertTrue(lemmas.contains(abstractionWords[0]));
        Assert.assertTrue(lemmas.contains(abstractionWords[1]));
    }


    private void createAbstractionWords(Dictionary dictionary) throws JWNLException {
        IndexWord iwEntity = dictionary.getIndexWord(POS.NOUN, entityLemma);
        Synset synEntity = iwEntity.getSenses().get(0);

        Synset synAbstraction = dictionary.createSynset(POS.NOUN);
        synAbstraction.setGloss(abstractionGloss);
        synAbstraction.getPointers().add(new Pointer(PointerType.HYPERNYM, synAbstraction, synEntity));

        for (int i = 0; i < abstractionWords.length; i++) {
            Word word = new Word(dictionary, synAbstraction, i + 1, abstractionWords[i]);
            word.setUseCount(i + 1);
            synAbstraction.getWords().add(word);
        }
    }

    private void testAbstractionWords(Dictionary dictionary) throws JWNLException {
        IndexWord iwEntity = dictionary.getIndexWord(POS.NOUN, entityLemma);
        Synset synEntity = iwEntity.getSenses().get(0);

        IndexWord iwAbstraction = dictionary.getIndexWord(POS.NOUN, abstractionWords[0]);
        Synset synAbstraction = iwAbstraction.getSenses().get(0);
        Assert.assertNotNull(synAbstraction);
        Assert.assertEquals(2, synEntity.getPointers().size());
        Assert.assertEquals(2, synAbstraction.getWords().size());

        IndexWord[] iwAbstractions = new IndexWord[2];
        for (int i = 0; i < abstractionWords.length; i++) {
            iwAbstractions[i] = dictionary.getIndexWord(POS.NOUN, abstractionWords[i]);
            Assert.assertNotNull(iwAbstractions[i]);
            Assert.assertEquals(1, iwAbstractions[i].getSenses().size());
            Assert.assertEquals(synAbstraction, iwAbstractions[i].getSenses().get(0));
            Assert.assertEquals(abstractionWords[i], iwAbstractions[i].getLemma());
            Assert.assertEquals(POS.NOUN, iwAbstractions[i].getPOS());
            Assert.assertNotNull(iwAbstractions[i].getSynsetOffsets());
            Assert.assertEquals(1, iwAbstractions[i].getSynsetOffsets().length);
            Assert.assertEquals(synAbstraction.getOffset(), iwAbstractions[i].getSynsetOffsets()[0]);
        }
        for (int i = 0; i < abstractionWords.length; i++) {
            Word word = synAbstraction.getWords().get(synAbstraction.indexOfWord(abstractionWords[i]));
            Assert.assertEquals(i + 1, word.getUseCount());
        }
    }

    @Test
    public void testSynsetRemove() throws IOException, JWNLException {
        dictionary.edit();

        createEntityWord(dictionary);
        createPEntityWord(dictionary);
        createEntityPEntityPointer(dictionary);
        checkIterators(dictionary);
        createAbstractionWords(dictionary);

        IndexWord iwAbstraction = dictionary.getIndexWord(POS.NOUN, abstractionWords[0]);
        Synset synAbstraction = iwAbstraction.getSenses().get(0);
        dictionary.removeSynset(synAbstraction);
        testSynsetIterator(dictionary);

        IndexWord iwEntity = dictionary.getIndexWord(POS.NOUN, entityLemma);
        Synset synEntity = iwEntity.getSenses().get(0);
        Assert.assertEquals(1, synEntity.getPointers().size());

        IndexWord[] iwAbstractions = new IndexWord[2];
        for (int i = 0; i < abstractionWords.length; i++) {
            iwAbstractions[i] = dictionary.getIndexWord(POS.NOUN, abstractionWords[i]);
            Assert.assertNull(iwAbstractions[i]);
        }
        testIndexWordIterator(dictionary);
    }

    @Test
    public void testSynsetRemoveRecreate() throws IOException, JWNLException {
        dictionary.edit();

        createEntityWord(dictionary);
        createPEntityWord(dictionary);
        createEntityPEntityPointer(dictionary);
        checkIterators(dictionary);
        createAbstractionWords(dictionary);

        saveAndReloadDictionary();

        IndexWord iwAbstraction = dictionary.getIndexWord(POS.NOUN, abstractionWords[0]);
        Synset synAbstraction = iwAbstraction.getSenses().get(0);
        dictionary.edit();
        dictionary.removeSynset(synAbstraction);

        saveAndReloadDictionary();

        testSynsetIterator(dictionary);

        IndexWord iwEntity = dictionary.getIndexWord(POS.NOUN, entityLemma);
        Synset synEntity = iwEntity.getSenses().get(0);
        Assert.assertEquals(1, synEntity.getPointers().size());

        IndexWord[] iwAbstractions = new IndexWord[2];
        for (int i = 0; i < abstractionWords.length; i++) {
            iwAbstractions[i] = dictionary.getIndexWord(POS.NOUN, abstractionWords[i]);
            Assert.assertNull(iwAbstractions[i]);
        }
        testIndexWordIterator(dictionary);
    }

    private void checkIterators(Dictionary dictionary) throws JWNLException {
        testSynsetIterator(dictionary);
        testIndexWordIterator(dictionary);
    }

    private void testSynsetIterator(Dictionary dictionary) throws JWNLException {
        List<Synset> synsets = new ArrayList<Synset>();
        Iterator<Synset> si = dictionary.getSynsetIterator(POS.NOUN);
        while (si.hasNext()) {
            synsets.add(si.next());
        }
        Assert.assertEquals(2, synsets.size());
        IndexWord iwEntity = dictionary.getIndexWord(POS.NOUN, entityLemma);
        Synset synEntity = iwEntity.getSenses().get(0);
        Assert.assertTrue(synsets.contains(synEntity));

        IndexWord iwpEntity = dictionary.getIndexWord(POS.NOUN, physical_entityLemma);
        Synset synPEntity = iwpEntity.getSenses().get(0);
        Assert.assertTrue(synsets.contains(synPEntity));
    }

    private void testIndexWordIterator(Dictionary dictionary) throws JWNLException {
        List<IndexWord> indexWords = new ArrayList<IndexWord>();
        Iterator<IndexWord> ii = dictionary.getIndexWordIterator(POS.NOUN);
        while (ii.hasNext()) {
            indexWords.add(ii.next());
        }
        Assert.assertEquals(2, indexWords.size());

        IndexWord iwEntity = dictionary.getIndexWord(POS.NOUN, entityLemma);
        Assert.assertTrue(indexWords.contains(iwEntity));

        IndexWord iwpEntity = dictionary.getIndexWord(POS.NOUN, physical_entityLemma);
        Assert.assertTrue(indexWords.contains(iwpEntity));
    }

    @Test
    public void testWordRemove() throws IOException, JWNLException {
        dictionary.edit();

        createEntityWord(dictionary);
        createPEntityWord(dictionary);
        createEntityPEntityPointer(dictionary);
        createAbstractionWords(dictionary);
        testAbstractionWords(dictionary);

        IndexWord iwAbstraction = dictionary.getIndexWord(POS.NOUN, abstractionWords[0]);
        Synset synAbstraction = iwAbstraction.getSenses().get(0);
        Assert.assertEquals(2, synAbstraction.getWords().size());

        synAbstraction.getWords().remove(0);
        Assert.assertEquals(1, synAbstraction.getWords().size());

        iwAbstraction = dictionary.getIndexWord(POS.NOUN, abstractionWords[0]);
        Assert.assertNull(iwAbstraction);
    }

    @Test
    public void testWordRemoveRecreate() throws IOException, JWNLException {
        dictionary.edit();

        createEntityWord(dictionary);
        createPEntityWord(dictionary);
        createEntityPEntityPointer(dictionary);
        createAbstractionWords(dictionary);
        testAbstractionWords(dictionary);

        IndexWord iwAbstraction = dictionary.getIndexWord(POS.NOUN, abstractionWords[0]);
        Synset synAbstraction = iwAbstraction.getSenses().get(0);
        Assert.assertEquals(2, synAbstraction.getWords().size());

        synAbstraction.getWords().remove(0);
        Assert.assertEquals(1, synAbstraction.getWords().size());

        saveAndReloadDictionary();

        iwAbstraction = dictionary.getIndexWord(POS.NOUN, abstractionWords[0]);
        Assert.assertNull(iwAbstraction);

        IndexWord iwAbstractEntity = dictionary.getIndexWord(POS.NOUN, abstractionWords[1]);
        Assert.assertNotNull(iwAbstractEntity);
        Assert.assertEquals(1, iwAbstractEntity.getSenses().size());
        Assert.assertTrue(iwAbstractEntity.getSenses().contains(synAbstraction));
    }

    @Test
    public void testSynsetOffsetsIncrease() throws JWNLException, FileNotFoundException {
        dictionary.edit();

        Synset s1 = dictionary.createSynset(POS.NOUN);
        s1.setGloss("first gloss");
        s1.getWords().add(new Word(dictionary, s1, 1, "first"));
        long o1 = s1.getOffset();

        saveAndReloadDictionary();
        dictionary.edit();

        Synset s2 = dictionary.createSynset(POS.NOUN);
        s2.getWords().add(new Word(dictionary, s2, 1, "second"));
        s2.setGloss("second gloss");
        long o2 = s2.getOffset();

        Assert.assertTrue(o1 < o2);
    }
}