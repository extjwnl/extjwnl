package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.util.factory.NameValueParam;
import net.sf.extjwnl.util.factory.Param;
import net.sf.extjwnl.util.factory.ParamList;
import net.sf.extjwnl.util.factory.ValueParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Abstract representation of a WordNet dictionary.
 * See the architecture documentation for information on subclassing Dictionary.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class Dictionary {

    static {
        JWNL.initialize();
    }

    private static final Log log = LogFactory.getLog(Dictionary.class);

    /**
     * Morphological processor class install parameter. The value should be the
     * class of MorphologicalProcessor to use.
     */
    public static final String MORPH = "morphological_processor";

    /**
     * Whether to add symmetric pointers automatically, default true.
     */
    public static final String EDIT_MANAGE_SYMMETRIC_POINTERS = "edit_manage_symmetric_pointers";
    private boolean editManageSymmetricPointers = true;

    /**
     * Whether to check for alien pointers (pointing nowhere, or to another dictionary), default true.
     */
    public static final String EDIT_CHECK_ALIEN_POINTERS = "edit_check_alien_pointers";
    private boolean editCheckAlienPointers = true;


    // tag names
    private static final String VERSION_TAG = "version";
    private static final String DICTIONARY_TAG = "dictionary";
    private static final String PARAM_TAG = "param";
    private static final String RESOURCE_TAG = "resource";

    // attribute names
    private static final String LANGUAGE_ATTRIBUTE = "language";
    private static final String COUNTRY_ATTRIBUTE = "country";
    private static final String CLASS_ATTRIBUTE = "class";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String VALUE_ATTRIBUTE = "value";
    private static final String PUBLISHER_ATTRIBUTE = "publisher";
    private static final String NUMBER_ATTRIBUTE = "number";

    /**
     * Whether to check and fix lexicographer ids, default true.
     */
    public static final String CHECK_LEX_IDS_KEY = "check_lex_ids";
    private boolean checkLexIds = true;

    /**
     * The singleton instance of the dictionary to be used throughout the system.
     */
    private static Dictionary dictionary = null;

    // temporary variable, used for loading from maps
    private static Dictionary restore;

    protected final Map<String, Param> params;

    private final Version version;

    private final MorphologicalProcessor morph;

    private volatile boolean editable;

    private static final String DEFAULT_FILE_DICTIONARY_PATH = "./data/wn30";
    private static final String DEFAULT_MAP_DICTIONARY_PATH = "./data/map";
    private static final String DEFAULT_DB_DICTIONARY_PATH = "jdbc:mysql://localhost/jwnl?user=root";

    private static final Comparator<Word> wordLexIdComparator = new Comparator<Word>() {
        @Override
        public int compare(Word o1, Word o2) {
            return o1.getLexId() - o2.getLexId();
        }
    };

    // stores max offset for each POS
    protected final Map<POS, Long> maxOffset = new EnumMap<POS, Long>(POS.class);

    /**
     * Represents a version of WordNet.
     */
    public static final class Version {
        private static final String UNSPECIFIED = "unspecified";

        private final String publisher;
        private final double number;
        private final Locale locale;

        public Version(String publisher, double number, Locale locale) {
            if (publisher == null) {
                publisher = UNSPECIFIED;
            }
            this.publisher = publisher;
            this.number = number;
            this.locale = locale;
        }

        public String getPublisher() {
            return publisher;
        }

        public double getNumber() {
            return number;
        }

        public Locale getLocale() {
            return locale;
        }

        public boolean equals(Object obj) {
            return (obj instanceof Version)
                    && publisher.equals(((Version) obj).publisher)
                    && number == ((Version) obj).number
                    && locale.equals(((Version) obj).locale);
        }

        public String toString() {
            return JWNL.resolveMessage("JWNL_TOSTRING_002", new Object[]{publisher, number, locale});
        }

        public int hashCode() {
            return publisher.hashCode() ^ (int) (number * 100);
        }
    }

    public static Dictionary getInstance() {
        return dictionary;
    }

    /**
     * Parses a properties file and creates a dictionary.
     *
     * @param properties the properties file stream
     * @return dictionary
     * @throws JWNLException various JWNL exceptions, depending on where this fails
     */
    public static Dictionary getInstance(InputStream properties) throws JWNLException {
        try {
            // find the properties file
            if (properties == null || properties.available() <= 0) {
                throw new JWNLException("JWNL_EXCEPTION_001");
            }
        } catch (IOException e) {
            throw new JWNLException("JWNL_EXCEPTION_001", e);
        }

        Dictionary result = getInstance(new InputSource(properties));

        // do this in a separate try/catch since parse can also throw an IOException
        try {
            properties.close();
        } catch (IOException e) {
            //hush it
        }

        return result;
    }

    /**
     * Parses properties and creates a dictionary.
     *
     * @param properties input source with properties
     * @return dictionary
     * @throws JWNLException various JWNL exceptions, depending on where this fails
     */
    public static Dictionary getInstance(InputSource properties) throws JWNLException {
        // parse the properties file
        Document doc;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            doc = docBuilder.parse(properties);
        } catch (Exception e) {
            throw new JWNLException("JWNL_EXCEPTION_002", e);
        }

        org.w3c.dom.Element root = doc.getDocumentElement();

        // parse dictionary
        NodeList dictionaryNodeList = root.getElementsByTagName(DICTIONARY_TAG);
        if (dictionaryNodeList.getLength() == 0) {
            throw new JWNLException("JWNL_EXCEPTION_005");
        }
        Node dictionaryNode = dictionaryNodeList.item(0);
        String dictionaryClassName = getAttribute(dictionaryNode, CLASS_ATTRIBUTE);
        Dictionary dictionary;
        try {
            Class clazz = Class.forName(dictionaryClassName);
            Constructor c = clazz.getConstructor(Document.class);
            dictionary = (Dictionary) c.newInstance(doc);
        } catch (ClassNotFoundException e) {
            throw new JWNLException("UTILS_EXCEPTION_005", dictionaryClassName, e);
        } catch (NoSuchMethodException e) {
            throw new JWNLException("UTILS_EXCEPTION_005", dictionaryClassName, e);
        } catch (InstantiationException e) {
            throw new JWNLException("UTILS_EXCEPTION_005", dictionaryClassName, e);
        } catch (IllegalAccessException e) {
            throw new JWNLException("UTILS_EXCEPTION_005", dictionaryClassName, e);
        } catch (InvocationTargetException e) {
            throw new JWNLException("UTILS_EXCEPTION_005", dictionaryClassName, e);
        }

        return dictionary;
    }

    /**
     * Returns FileBackedDictionary instance with default configuration.
     *
     * @param dictionaryPath dictionary path
     * @return FileBackedDictionary instance with default configuration
     * @throws JWNLException JWNLException
     */
    public static Dictionary getFileBackedInstance(String dictionaryPath) throws JWNLException {
        try {
            String properties = getTextFromStream(JWNL.class.getResourceAsStream("file_properties.xml"));
            properties = properties.replace(DEFAULT_FILE_DICTIONARY_PATH, dictionaryPath);
            return getInstance(new InputSource(new StringReader(properties)));
        } catch (IOException e) {
            throw new JWNLException("IO error opening properties file", e);
        }
    }

    /**
     * Returns MapBackedDictionary instance with default configuration.
     *
     * @param dictionaryPath dictionary path
     * @return MapBackedDictionary instance with default configuration
     * @throws JWNLException JWNLException
     */
    public static Dictionary getMapBackedInstance(String dictionaryPath) throws JWNLException {
        try {
            String properties = getTextFromStream(JWNL.class.getResourceAsStream("map_properties.xml"));
            properties = properties.replace(DEFAULT_MAP_DICTIONARY_PATH, dictionaryPath);
            return getInstance(new InputSource(new StringReader(properties)));
        } catch (IOException e) {
            throw new JWNLException("IO error opening properties file", e);
        }
    }

    /**
     * Returns DatabaseBackedDictionary instance with default configuration.
     *
     * @param dbURL database url
     * @return DatabaseBackedDictionary instance with default configuration
     * @throws JWNLException JWNLException
     */
    public static Dictionary getDatabaseBackedInstance(String dbURL) throws JWNLException {
        try {
            String properties = getTextFromStream(JWNL.class.getResourceAsStream("database_properties.xml"));
            properties = properties.replace(DEFAULT_DB_DICTIONARY_PATH, dbURL);
            return getInstance(new InputSource(new StringReader(properties)));
        } catch (IOException e) {
            throw new JWNLException("IO error opening properties file", e);
        }
    }

    public static Dictionary setInstance(Dictionary dictionary) {
        if (log.isInfoEnabled()) {
            log.info(JWNL.resolveMessage("DICTIONARY_INFO_002", dictionary));
        }
        synchronized (Dictionary.class) {
            Dictionary.dictionary = dictionary;
        }
        return dictionary;
    }

    public synchronized static void uninstall() {
        if (dictionary != null) {
            dictionary.close();
            dictionary = null;
        }
    }

    public synchronized static void setRestoreDictionary(Dictionary dictionary) {
        restore = dictionary;
    }

    public static Dictionary getRestoreDictionary() {
        return restore;
    }

    protected Dictionary(Document doc) throws JWNLException {
        org.w3c.dom.Element root = doc.getDocumentElement();

        // add additional resources
        NodeList resourceNodes = root.getElementsByTagName(RESOURCE_TAG);
        for (int i = 0; i < resourceNodes.getLength(); i++) {
            String resource = getAttribute(resourceNodes.item(i), CLASS_ATTRIBUTE);
            if (resource != null) {
                JWNL.getResourceBundleSet().addResource(resource);
            }
        }

        // parse version information
        NodeList versionNodes = root.getElementsByTagName(VERSION_TAG);
        if (versionNodes.getLength() == 0) {
            throw new JWNLException("JWNL_EXCEPTION_003");
        }
        Node version = versionNodes.item(0);

        String number = getAttribute(version, NUMBER_ATTRIBUTE);
        this.version = new Version(
                getAttribute(version, PUBLISHER_ATTRIBUTE),
                (number == null) ? 0.0 : Double.parseDouble(number),
                getLocale(getAttribute(version, LANGUAGE_ATTRIBUTE), getAttribute(version, COUNTRY_ATTRIBUTE)));

        // parse dictionary
        NodeList dictionaryNodeList = root.getElementsByTagName(DICTIONARY_TAG);
        if (dictionaryNodeList.getLength() == 0) {
            throw new JWNLException("JWNL_EXCEPTION_005");
        }
        Node dictionaryNode = dictionaryNodeList.item(0);

        params = new HashMap<String, Param>();
        for (Param p : getParams(this, dictionaryNode.getChildNodes())) {
            params.put(p.getName(), p);
        }

        if (params.containsKey(CHECK_LEX_IDS_KEY)) {
            checkLexIds = Boolean.parseBoolean(params.get(CHECK_LEX_IDS_KEY).getValue());
        }

        Param param = params.get(MORPH);
        morph = (param == null) ? null : (MorphologicalProcessor) param.create();

        if (params.containsKey(EDIT_MANAGE_SYMMETRIC_POINTERS)) {
            editManageSymmetricPointers = Boolean.parseBoolean(params.get(EDIT_MANAGE_SYMMETRIC_POINTERS).getValue());
        }

        if (params.containsKey(EDIT_CHECK_ALIEN_POINTERS)) {
            editCheckAlienPointers = Boolean.parseBoolean(params.get(EDIT_CHECK_ALIEN_POINTERS).getValue());
        }
    }

    /**
     * Returns an Iterator over all the IndexWords of part-of-speech <var>pos</var> in the database.
     *
     * @param pos The part-of-speech
     * @return iterator over <code>IndexWord</code>s
     * @throws JWNLException JWNLException
     */
    public abstract Iterator<IndexWord> getIndexWordIterator(POS pos) throws JWNLException;

    /**
     * Returns an Iterator over all the IndexWords of part-of-speech <var>pos</var>
     * whose lemmas contain <var>substring</var> as a substring.
     *
     * @param pos       The part-of-speech.
     * @param substring substring
     * @return An iterator over <code>IndexWord</code>s.
     * @throws JWNLException JWNLException
     */
    public abstract Iterator<IndexWord> getIndexWordIterator(POS pos, String substring) throws JWNLException;

    /**
     * Looks up a word in the database. The search is case-independent,
     * and phrases are separated by spaces ("look up", not "look_up").
     * Note: this method does not subject <var>lemma</var> to any
     * morphological processing. If you want this, use {@link #lookupIndexWord(POS, String)}.
     *
     * @param pos   The part-of-speech.
     * @param lemma The orthographic representation of the word.
     * @return An IndexWord representing the word, or <code>null</code> if
     *         no such entry exists.
     * @throws JWNLException JWNLException
     */
    public abstract IndexWord getIndexWord(POS pos, String lemma) throws JWNLException;

    public abstract IndexWord getRandomIndexWord(POS pos) throws JWNLException;

    /**
     * Returns a word by specified <var>senseKey</var> or null if not found.
     *
     * @param senseKey sense key
     * @return a word by specified <var>senseKey</var> or null if not found
     * @throws JWNLException JWNLException
     */
    public Word getWordBySenseKey(String senseKey) throws JWNLException {
        int percentIndex = senseKey.indexOf('%');
        String lemma = senseKey.substring(0, percentIndex).replace('_', ' ');
        String ssType = senseKey.substring(percentIndex + 1, senseKey.indexOf(':', percentIndex));
        POS pos = POS.getPOSForId(Integer.parseInt(ssType));
        Word result = null;
        if (null != pos) {
            IndexWord iw = getIndexWord(pos, lemma);
            if (null != iw) {
                searchB:
                for (Synset synset : iw.getSenses()) {
                    for (Word word : synset.getWords()) {
                        if (senseKey.equals(word.getSenseKey())) {
                            result = word;
                            break searchB;
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns an Iterator over all the Synsets of part-of-speech <var>pos</var>
     * in the database.
     *
     * @param pos The part-of-speech.
     * @return An iterator over <code>Synset</code>s.
     * @throws JWNLException JWNLException
     */
    public abstract Iterator<Synset> getSynsetIterator(POS pos) throws JWNLException;

    /**
     * Returns the <code>Synset</code> at offset <var>offset</var> from the database.
     *
     * @param pos    The part-of-speech file to look in
     * @param offset The offset of the synset in the file
     * @return A synset containing the parsed line from the database
     * @throws JWNLException JWNLException
     */
    public abstract Synset getSynsetAt(POS pos, long offset) throws JWNLException;

    /**
     * Returns an Iterator over all the Exceptions in the database.
     *
     * @param pos the part-of-speech
     * @return Iterator An iterator over <code>Exc</code>s
     * @throws JWNLException JWNLException
     */
    public abstract Iterator<Exc> getExceptionIterator(POS pos) throws JWNLException;

    /**
     * Looks up <var>derivation</var> in the exceptions file of part-of-speech <var>
     * pos</var> and return an Exc object containing the results.
     *
     * @param pos        the exception file to look in
     * @param derivation the word to look up
     * @return Exc the Exc object
     * @throws JWNLException JWNLException
     */
    public abstract Exc getException(POS pos, String derivation) throws JWNLException;

    /**
     * Shuts down the dictionary
     */
    public abstract void close();

    public MorphologicalProcessor getMorphologicalProcessor() {
        return morph;
    }

    /**
     * Looks up a word <var>lemma</var>. First tries a normal lookup. If that doesn't work,
     * tries looking up the stemmed form of the lemma.
     *
     * @param pos   the part-of-speech of the word to look up
     * @param lemma the lemma to look up
     * @return IndexWord the IndexWord found by the lookup procedure, or null
     *         if an IndexWord is not found
     * @throws JWNLException JWNLException
     */
    public IndexWord lookupIndexWord(POS pos, String lemma) throws JWNLException {
        lemma = prepareQueryString(lemma);
        IndexWord word = getIndexWord(pos, lemma);
        if (word == null && getMorphologicalProcessor() != null) {
            word = getMorphologicalProcessor().lookupBaseForm(pos, lemma);
        }
        return word;
    }

    /**
     * Returns a set of <code>IndexWord</code>s, with each element in the set
     * corresponding to a part-of-speech of <var>word</var>.
     *
     * @param lemma the word for which to lookup senses
     * @return An array of IndexWords, each of which is a sense of <var>word</var>
     * @throws JWNLException JWNLException
     */
    public IndexWordSet lookupAllIndexWords(String lemma) throws JWNLException {
        lemma = prepareQueryString(lemma);
        IndexWordSet set = new IndexWordSet(lemma);
        for (POS pos : POS.getAllPOS()) {
            IndexWord current = lookupIndexWord(pos, lemma);
            if (current != null) {
                set.add(current);
            }
        }
        return set;
    }

    /**
     * Returns the current WordNet version.
     *
     * @return current WordNet version
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Returns whether the dictionary is editable.
     *
     * @return whether the dictionary is editable
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Puts the dictionary into edit mode.
     *
     * @throws JWNLException JWNLException
     */
    public synchronized void edit() throws JWNLException {
        if (!editable) {
            editable = true;
        }
    }

    /**
     * Saves the dictionary.
     *
     * @throws JWNLException JWNLException
     */
    public synchronized void save() throws JWNLException {
        if (!isEditable()) {
            throw new JWNLException("DICTIONARY_EXCEPTION_029");
        }
        if (log.isInfoEnabled()) {
            log.info(JWNL.resolveMessage("PRINCETON_INFO_022"));
        }
        if (checkLexIds) {
            //fixing word lex ids
            for (POS pos : POS.getAllPOS()) {
                if (log.isInfoEnabled()) {
                    log.info(JWNL.resolveMessage("PRINCETON_INFO_015", pos.getLabel()));
                }
                Iterator<IndexWord> ii = getIndexWordIterator(pos);
                while (ii.hasNext()) {
                    IndexWord iw = ii.next();
                    //lex ids should be unique within lex file name
                    //lex file name -> list of words
                    Map<Long, List<Word>> words = new HashMap<Long, List<Word>>();
                    for (Synset sense : iw.getSenses()) {
                        for (Word word : sense.getWords()) {
                            if (word.getLemma().equalsIgnoreCase(iw.getLemma())) {
                                List<Word> list = words.get(sense.getLexFileNum());
                                if (null == list) {
                                    list = new ArrayList<Word>();
                                    words.put(sense.getLexFileNum(), list);
                                }
                                list.add(word);
                            }
                        }
                    }

                    for (Map.Entry<Long, List<Word>> entry : words.entrySet()) {
                        List<Word> list = entry.getValue();
                        Collections.sort(list, wordLexIdComparator);
                        int maxId = -1;
                        for (Word word : list) {
                            if (maxId < word.getLexId()) {
                                maxId = word.getLexId();
                            }
                        }
                        for (Word word : list) {
                            if (-1 == word.getLexId()) {
                                maxId++;
                                word.setLexId(maxId);
                            }
                        }
                    }
                }
                if (log.isInfoEnabled()) {
                    log.info(JWNL.resolveMessage("PRINCETON_INFO_016", pos.getLabel()));
                }
            }
        }
    }

    public synchronized void delete() throws JWNLException {
        //nop
    }

    /**
     * Adds dictionary element to the dictionary.
     *
     * @param element element to add
     * @throws JWNLException JWNLException
     */
    public void addElement(DictionaryElement element) throws JWNLException {
        if (!isEditable()) {
            throw new JWNLException("DICTIONARY_EXCEPTION_029");
        }
        if (this != element.getDictionary()) {
            throw new JWNLException("DICTIONARY_EXCEPTION_040");
        }
        if (element instanceof Exc) {
            addException((Exc) element);
        } else if (element instanceof IndexWord) {
            addIndexWord((IndexWord) element);
        } else if (element instanceof Synset) {
            addSynset((Synset) element);
        }
    }

    /**
     * Removes the dictionary <var>element</var> from the dictionary.
     *
     * @param element element to be removed
     * @throws JWNLException JWNLException
     */
    public void removeElement(DictionaryElement element) throws JWNLException {
        if (!isEditable()) {
            throw new JWNLException("DICTIONARY_EXCEPTION_029");
        }
        if (element instanceof Exc) {
            removeException((Exc) element);
        } else if (element instanceof IndexWord) {
            removeIndexWord((IndexWord) element);
        } else if (element instanceof Synset) {
            removeSynset((Synset) element);
        }
    }

    /**
     * Creates an exception in the dictionary.
     *
     * @param pos        exception part of speech
     * @param lemma      exception lemma
     * @param exceptions list of base forms
     * @return exception object
     * @throws JWNLException JWNLException
     */
    public Exc createException(POS pos, String lemma, List<String> exceptions) throws JWNLException {
        if (!isEditable()) {
            throw new JWNLException("DICTIONARY_EXCEPTION_029");
        }
        return new Exc(this, pos, lemma, exceptions);
    }

    /**
     * Adds exception to the dictionary.
     *
     * @param exc exception to add
     * @throws JWNLException JWNLException
     */
    public void addException(Exc exc) throws JWNLException {
        if (!isEditable()) {
            throw new JWNLException("DICTIONARY_EXCEPTION_029");
        }
        if (this != exc.getDictionary()) {
            throw new JWNLException("DICTIONARY_EXCEPTION_040");
        }
        exc.setDictionary(this);
    }

    /**
     * Removes the <var>exc</var> from the dictionary.
     *
     * @param exc exc to be removed
     * @throws JWNLException JWNLException
     */
    public void removeException(Exc exc) throws JWNLException {
        if (!isEditable()) {
            throw new JWNLException("DICTIONARY_EXCEPTION_029");
        }
        exc.setDictionary(null);
    }

    /**
     * Creates synset of the specified part of speech.
     *
     * @param pos part of speech
     * @return synset object
     * @throws JWNLException JWNLException
     */
    public Synset createSynset(POS pos) throws JWNLException {
        if (!isEditable()) {
            throw new JWNLException("DICTIONARY_EXCEPTION_029");
        }
        if (POS.VERB == pos) {
            return new VerbSynset(this, pos, createNewOffset(pos));
        } else if (POS.ADJECTIVE == pos) {
            return new AdjectiveSynset(this, pos, createNewOffset(pos));
        } else {
            return new Synset(this, pos, createNewOffset(pos));
        }
    }

    /**
     * Adds synset to the dictionary.
     *
     * @param synset synset to add
     * @throws JWNLException JWNLException
     */
    public void addSynset(Synset synset) throws JWNLException {
        if (!isEditable()) {
            throw new JWNLException("DICTIONARY_EXCEPTION_029");
        }
        if (this != synset.getDictionary()) {
            throw new JWNLException("DICTIONARY_EXCEPTION_040");
        }
        synset.setDictionary(this);
    }

    /**
     * Removes <var>synset</var> from the dictionary.
     *
     * @param synset synset to remove
     * @throws JWNLException JWNLException
     */
    public synchronized void removeSynset(Synset synset) throws JWNLException {
        if (!isEditable()) {
            throw new JWNLException("DICTIONARY_EXCEPTION_029");
        }
        synset.setDictionary(null);

        //take care of index words
        List<Word> copy = new ArrayList<Word>(synset.getWords());
        for (Word word : copy) {
            IndexWord indexWord = getIndexWord(synset.getPOS(), word.getLemma());
            if (null != indexWord) {
                indexWord.getSenses().remove(synset);
            }
        }

        //take care of pointers
        //this will delete symmetric ones
        synset.getPointers().clear();
        //asymmetric ones will be checked by the synset on gets
    }

    /**
     * Creates index word.
     *
     * @param pos    part of speech
     * @param lemma  lemma
     * @param synset synset
     * @return index word object
     * @throws JWNLException JWNLException
     */
    public IndexWord createIndexWord(POS pos, String lemma, Synset synset) throws JWNLException {
        if (!isEditable()) {
            throw new JWNLException("DICTIONARY_EXCEPTION_029");
        }
        return new IndexWord(this, lemma, pos, synset);
    }

    /**
     * Adds index word to the dictionary.
     *
     * @param indexWord index word to add
     * @throws JWNLException JWNLException
     */
    public void addIndexWord(IndexWord indexWord) throws JWNLException {
        if (!isEditable()) {
            throw new JWNLException("DICTIONARY_EXCEPTION_029");
        }
        if (this != indexWord.getDictionary()) {
            throw new JWNLException("DICTIONARY_EXCEPTION_040");
        }
        indexWord.setDictionary(this);
    }

    /**
     * Removes <var>indexWord</var> from the dictionary.
     *
     * @param indexWord index word to remove
     * @throws JWNLException JWNLException
     */
    public synchronized void removeIndexWord(IndexWord indexWord) throws JWNLException {
        if (!isEditable()) {
            throw new JWNLException("DICTIONARY_EXCEPTION_029");
        }

        indexWord.setDictionary(null);

        //take care of words in synsets
        List<Synset> copy = new ArrayList<Synset>(indexWord.getSenses());
        for (Synset synset : copy) {
            List<Word> wordsCopy = new ArrayList<Word>(synset.getWords());
            for (Word word : wordsCopy) {
                if (word.getLemma().equalsIgnoreCase(indexWord.getLemma())) {
                    synset.getWords().remove(word);
                    break;
                }
            }
        }
    }

    public boolean getManageSymmetricPointers() {
        return editManageSymmetricPointers;
    }

    public boolean getCheckAlienPointers() {
        return editCheckAlienPointers;
    }

    /**
     * Prepares the lemma for being used in a lookup operation.
     * Specifically, this method trims whitespace and converts the lemma
     * to lower case.
     *
     * @param lemma the lemma to be prepared
     * @return String the prepared lemma
     */
    protected static String prepareQueryString(String lemma) {
        return lemma.trim().toLowerCase();
    }

    private static List<Param> getParams(Dictionary dictionary, NodeList list) throws JWNLException {
        List<Param> params = new ArrayList<Param>();
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(PARAM_TAG)) {
                String name = getAttribute(n, NAME_ATTRIBUTE);
                String value = getAttribute(n, VALUE_ATTRIBUTE);
                if (name == null && value == null) {
                    throw new JWNLException("JWNL_EXCEPTION_008");
                } else {
                    Param param;
                    if (value == null) {
                        param = new ParamList(name.toLowerCase(), getParams(dictionary, n.getChildNodes()));
                    } else if (name == null) {
                        param = new ValueParam(dictionary, value, getParams(dictionary, n.getChildNodes()));
                    } else {
                        param = new NameValueParam(dictionary, name.toLowerCase(), value, getParams(dictionary, n.getChildNodes()));
                    }
                    params.add(param);
                }
            }
        }
        return params;
    }

    private static String getAttribute(Node node, String attributeName) {
        NamedNodeMap map = node.getAttributes();
        if (map != null) {
            Node n = map.getNamedItem(attributeName);
            if (n != null) {
                return n.getNodeValue();
            }
        }
        return null;
    }

    private static Locale getLocale(String language, String country) {
        if (language == null) {
            return Locale.getDefault();
        } else if (country == null) {
            return new Locale(language, "");
        } else {
            return new Locale(language, country);
        }
    }

    private synchronized long createNewOffset(POS pos) {
        long result = maxOffset.get(pos) + 1;
        maxOffset.put(pos, result);
        return result;
    }

    private static String getTextFromStream(InputStream inputStream) throws IOException {
        BufferedReader fileCheck;
        fileCheck = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder fileText = new StringBuilder();
        String line;
        while (null != (line = fileCheck.readLine())) {
            fileText.append(line).append("\n");
        }
        try {
            fileCheck.close();
        } catch (IOException e) {
            // doesn't matter.
        }
        return fileText.toString();
    }
}