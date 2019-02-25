package net.sf.extjwnl.dictionary;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.dictionary.morph.Util;
import net.sf.extjwnl.util.ResourceBundleSet;
import net.sf.extjwnl.util.factory.NameValueParam;
import net.sf.extjwnl.util.factory.Param;
import net.sf.extjwnl.util.factory.ParamList;
import net.sf.extjwnl.util.factory.ValueParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Abstract representation of a WordNet dictionary.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class Dictionary {

    private static final Logger log = LoggerFactory.getLogger(Dictionary.class);

    private volatile Random rand = null;

    // messages for static methods
    private static final String STATIC_MESSAGES = "net.sf.extjwnl.dictionary.messages_static";
    private static final ResourceBundleSet staticMessages = new ResourceBundleSet(STATIC_MESSAGES);

    // messages for dictionary
    private static final String MESSAGES = "net.sf.extjwnl.dictionary.messages";
    private final ResourceBundleSet messages = new ResourceBundleSet(MESSAGES);

    /**
     * Parameter name: class of the morphological processor to use.
     */
    public static final String MORPHOLOGICAL_PROCESSOR = "morphological_processor";

    /**
     * Parameter name: whether to add symmetric pointers automatically, default true.
     */
    public static final String EDIT_MANAGE_SYMMETRIC_POINTERS = "edit_manage_symmetric_pointers";
    private boolean editManageSymmetricPointers = true;

    /**
     * Parameter name: whether to check for alien pointers (pointing nowhere, or to another dictionary), default true.
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
     * Parameter name: whether to check and fix lexicographer ids, default true.
     */
    public static final String CHECK_LEX_IDS_KEY = "check_lex_ids";
    private boolean checkLexIds = true;

    // temporary variable, used for loading from maps
    private static Dictionary restore;

    protected final Map<String, Param> params;

    private final Version version;

    private final MorphologicalProcessor morph;

    private volatile boolean editable;

    private static final String DEFAULT_FILE_DICTIONARY_PATH = "./data/wn30";
    private static final String DEFAULT_MAP_DICTIONARY_PATH = "./data/map";
    private static final String DEFAULT_DB_DICTIONARY_PATH = "jdbc:mysql://localhost/jwnl?user=root";

    /**
     * Default name of the configuration file for resource instance creation.
     */
    public static final String DEFAULT_RESOURCE_CONFIG_PATH = "/extjwnl_resource_properties.xml";

    private static final Comparator<Word> wordLexIdComparator = Comparator.comparingInt(Word::getLexId);

    private final String[] verbFrames;

    /**
     * The class of DictionaryElementFactory to use.
     */
    public static final String DICTIONARY_ELEMENT_FACTORY = "dictionary_element_factory";
    protected final DictionaryElementFactory elementFactory;

    /**
     * Represents a version of WordNet.
     */
    public final class Version {
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

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof Version)
                    && publisher.equals(((Version) obj).publisher)
                    && number == ((Version) obj).number
                    && locale.equals(((Version) obj).locale);
        }

        @Override
        public String toString() {
            return messages.resolveMessage("JWNL_TOSTRING_002", new Object[]{publisher, number, locale});
        }

        @Override
        public int hashCode() {
            return publisher.hashCode() ^ (int) (number * 100);
        }
    }

    /**
     * Parses a properties file and creates a dictionary.
     *
     * @param properties the properties file stream
     * @return dictionary
     * @throws JWNLException various JWNL exceptions, depending on where this fails
     */
    public static Dictionary getInstance(InputStream properties) throws JWNLException {
        if (null == properties) {
            throw new IllegalArgumentException();
        }
        return getInstance(new InputSource(properties));
    }

    /**
     * Parses properties and creates a dictionary.
     *
     * @param properties input source with properties
     * @return dictionary
     * @throws JWNLException various JWNL exceptions, depending on where this fails
     */
    @SuppressWarnings("unchecked")
    public static Dictionary getInstance(InputSource properties) throws JWNLException {
        // parse the properties file
        Document doc;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            doc = docBuilder.parse(properties);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new JWNLException(staticMessages.resolveMessage("DICTIONARY_ERROR_PARSING_PROPERTIES"), e);
        }

        org.w3c.dom.Element root = doc.getDocumentElement();

        // parse dictionary
        NodeList dictionaryNodeList = root.getElementsByTagName(DICTIONARY_TAG);
        if (dictionaryNodeList.getLength() == 0) {
            throw new JWNLException(staticMessages.resolveMessage("DICTIONARY_PROPERTIES_MUST_SPECIFY_DICTIONARY"));
        }
        Node dictionaryNode = dictionaryNodeList.item(0);
        String dictionaryClassName = getAttribute(dictionaryNode, CLASS_ATTRIBUTE);
        Dictionary dictionary;
        try {
            Class clazz = Class.forName(dictionaryClassName);
            Constructor c = clazz.getConstructor(Document.class);
            dictionary = (Dictionary) c.newInstance(doc);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new JWNLException(staticMessages.resolveMessage("DICTIONARY_UNABLE_TO_CREATE_INSTANCE", new Object[]{dictionaryClassName, Util.getRootCause(e)}), e);
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
            String properties = getResourceProperties("file_properties.xml");
            properties = properties.replace(DEFAULT_FILE_DICTIONARY_PATH, dictionaryPath);
            return getInstance(new InputSource(new StringReader(properties)));
        } catch (IOException e) {
            throw new JWNLException(staticMessages.resolveMessage("DICTIONARY_IO_ERROR_OPENING_PROPERTIES"), e);
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
            String properties = getResourceProperties("map_properties.xml");
            properties = properties.replace(DEFAULT_MAP_DICTIONARY_PATH, dictionaryPath);
            return getInstance(new InputSource(new StringReader(properties)));
        } catch (IOException e) {
            throw new JWNLException(staticMessages.resolveMessage("DICTIONARY_IO_ERROR_OPENING_PROPERTIES"), e);
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
            String properties = getResourceProperties("database_properties.xml");
            properties = properties.replace(DEFAULT_DB_DICTIONARY_PATH, dbURL);
            return getInstance(new InputSource(new StringReader(properties)));
        } catch (IOException e) {
            throw new JWNLException(staticMessages.resolveMessage("DICTIONARY_IO_ERROR_OPENING_PROPERTIES"), e);
        }
    }

    /**
     * Returns Dictionary instance configured from classpath by default.
     *
     * @return Dictionary instance configured from classpath by default
     * @throws JWNLException JWNLException
     */
    public static Dictionary getDefaultResourceInstance() throws JWNLException {
        return getResourceInstance(DEFAULT_RESOURCE_CONFIG_PATH);
    }

    /**
     * Returns Dictionary instance configured from classpath.
     *
     * @param propertiesPath path to properties, for example "/net/sf/extjwnl/data/wordnet/wn31/res_properties.xml"
     * @return Dictionary instance configured from classpath
     * @throws JWNLException JWNLException
     */
    public static Dictionary getResourceInstance(String propertiesPath) throws JWNLException {
        InputStream properties = Dictionary.class.getResourceAsStream(propertiesPath);
        return getInstance(properties);
    }

    public synchronized static void setRestoreDictionary(Dictionary dictionary) {
        restore = dictionary;
    }

    public static Dictionary getRestoreDictionary() {
        return restore;
    }

    protected Dictionary(Document doc) throws JWNLException {
        org.w3c.dom.Element root = doc.getDocumentElement();

        // set messages locale
        messages.setLocale(getLocale(getAttribute(root, LANGUAGE_ATTRIBUTE), getAttribute(root, COUNTRY_ATTRIBUTE)));

        // add additional resources
        NodeList resourceNodes = root.getElementsByTagName(RESOURCE_TAG);
        for (int i = 0; i < resourceNodes.getLength(); i++) {
            String resource = getAttribute(resourceNodes.item(i), CLASS_ATTRIBUTE);
            if (resource != null) {
                messages.addResource(resource);
            }
        }

        // parse version information
        NodeList versionNodes = root.getElementsByTagName(VERSION_TAG);
        if (versionNodes.getLength() == 0) {
            throw new JWNLException(messages.resolveMessage("EXC_PROPERTIES_FILE_MUST_SPECIFY_VERSION"));
        }
        Node version = versionNodes.item(0);

        String number = getAttribute(version, NUMBER_ATTRIBUTE);
        this.version = new Version(
                getAttribute(version, PUBLISHER_ATTRIBUTE),
                (number == null) ? 0.0 : Double.parseDouble(number),
                getLocale(getAttribute(version, LANGUAGE_ATTRIBUTE), getAttribute(version, COUNTRY_ATTRIBUTE)));

        // parse dictionary
        NodeList dictionaryNodeList = root.getElementsByTagName(DICTIONARY_TAG);
        Node dictionaryNode = dictionaryNodeList.item(0);

        params = new HashMap<>();
        for (Param p : getParams(this, dictionaryNode.getChildNodes())) {
            params.put(p.getName(), p);
        }

        if (!params.containsKey(DICTIONARY_ELEMENT_FACTORY)) {
            throw new IllegalArgumentException(messages.resolveMessage("DICTIONARY_EXCEPTION_001", DICTIONARY_ELEMENT_FACTORY));
        }
        elementFactory = (DictionaryElementFactory) (params.get(DICTIONARY_ELEMENT_FACTORY)).create();

        if (params.containsKey(CHECK_LEX_IDS_KEY)) {
            checkLexIds = Boolean.parseBoolean(params.get(CHECK_LEX_IDS_KEY).getValue());
        }

        Param param = params.get(MORPHOLOGICAL_PROCESSOR);
        morph = (param == null) ? null : (MorphologicalProcessor) param.create();

        if (params.containsKey(EDIT_MANAGE_SYMMETRIC_POINTERS)) {
            editManageSymmetricPointers = Boolean.parseBoolean(params.get(EDIT_MANAGE_SYMMETRIC_POINTERS).getValue());
        }

        if (params.containsKey(EDIT_CHECK_ALIEN_POINTERS)) {
            editCheckAlienPointers = Boolean.parseBoolean(params.get(EDIT_CHECK_ALIEN_POINTERS).getValue());
        }

        // initialize verb frames
        int framesCount = Integer.parseInt(messages.resolveMessage("NUMBER_OF_VERB_FRAMES"));
        verbFrames = new String[framesCount];
        for (int i = 1; i <= framesCount; i++) {
            verbFrames[i - 1] = messages.resolveMessage("VERB_FRAME_" + i);
        }
    }

    /**
     * Returns an Iterator over all the IndexWords of part-of-speech <var>pos</var>.
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
     * Looks up a word. The search is case-independent,
     * and phrases are separated by spaces ("look up", not "look_up").
     * Note: this method does not subject <var>lemma</var> to any
     * morphological processing. If you want this, use {@link #lookupIndexWord(POS, String)}.
     *
     * @param pos   The part-of-speech
     * @param lemma The orthographic representation of the word
     * @return An IndexWord representing the word, or <code>null</code> if
     * no such entry exists
     * @throws JWNLException JWNLException
     */
    public abstract IndexWord getIndexWord(POS pos, String lemma) throws JWNLException;

    /**
     * Returns a random index word of a specified <var>pos</var>.
     *
     * @param pos part of speech
     * @return a random index word of a specified <var>pos</var>
     * @throws JWNLException JWNLException
     */
    public abstract IndexWord getRandomIndexWord(POS pos) throws JWNLException;

    /**
     * Returns an iterator over all the synsets of part-of-speech <var>pos</var>.
     *
     * @param pos The part-of-speech.
     * @return An iterator over <code>Synset</code>s.
     * @throws JWNLException JWNLException
     */
    public abstract Iterator<Synset> getSynsetIterator(POS pos) throws JWNLException;

    /**
     * Returns the <code>Synset</code> at offset <var>offset</var>.
     *
     * @param pos    The part-of-speech file to look in
     * @param offset The offset of the synset in the file
     * @return A synset containing the parsed line from the database
     * @throws JWNLException JWNLException
     */
    public abstract Synset getSynsetAt(POS pos, long offset) throws JWNLException;

    /**
     * Returns an iterator over all the exceptions.
     *
     * @param pos the part-of-speech
     * @return an iterator over <code>Exc</code>s
     * @throws JWNLException JWNLException
     */
    public abstract Iterator<Exc> getExceptionIterator(POS pos) throws JWNLException;

    /**
     * Looks up <var>derivation</var> in the exceptions file of part-of-speech <var>
     * pos</var> and return an Exc object containing the results.
     *
     * @param pos        the exception file to look in
     * @param derivation the word to look up
     * @return the Exc object
     * @throws JWNLException JWNLException
     */
    public abstract Exc getException(POS pos, String derivation) throws JWNLException;

    /**
     * Shuts down the dictionary, freeing resources.
     *
     * @throws JWNLException JWNLException
     */
    public abstract void close() throws JWNLException;

    public ResourceBundleSet getMessages() {
        return messages;
    }

    public MorphologicalProcessor getMorphologicalProcessor() {
        return morph;
    }

    /**
     * Returns a word by specified <var>senseKey</var> or null if not found.
     *
     * @param senseKey sense key
     * @return a word by specified <var>senseKey</var> or null if not found
     * @throws JWNLException JWNLException
     */
    public Word getWordBySenseKey(String senseKey) throws JWNLException {
        if (senseKey == null || senseKey.isEmpty()) {
            return null;
        }

        final int percentIndex = senseKey.indexOf('%');
        if (percentIndex == -1) {
            return null;
        }

        final int colonIndex = senseKey.indexOf(':', percentIndex);
        if (colonIndex == -1) {
            return null;
        }

        final String ssType = senseKey.substring(percentIndex + 1, colonIndex);
        final int ssTypeId;
        try {
            ssTypeId = Integer.parseInt(ssType);
        } catch (NumberFormatException e) {
            return null;
        }

        final POS pos = POS.getPOSForId(ssTypeId);
        if (pos == null) {
            return null;
        }

        final String lemma = senseKey.substring(0, percentIndex).replace('_', ' ');
        final IndexWord iw = getIndexWord(pos, lemma);
        if (iw == null) {
            return null;
        }

        Word result = null;
        searchB:
        for (final Synset synset : iw.getSenses()) {
            for (final Word word : synset.getWords()) {
                if (senseKey.equals(word.getSenseKey())) {
                    result = word;
                    break searchB;
                }
            }
        }
        return result;
    }

    /**
     * Looks up a word <var>lemma</var>. First tries a normal lookup. If that doesn't work,
     * tries looking up the stemmed form of the lemma.
     *
     * @param pos   the part-of-speech of the word to look up
     * @param lemma the lemma to look up
     * @return the IndexWord found by the lookup procedure, or null
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
     * Returns the current dictionary version.
     *
     * @return current dictionary version
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
        checkEditable();
        if (log.isInfoEnabled()) {
            log.info(messages.resolveMessage("DICTIONARY_INFO_014"));
        }
        if (checkLexIds) {
            //fixing word lex ids
            for (POS pos : POS.getAllPOS()) {
                if (log.isDebugEnabled()) {
                    log.debug(messages.resolveMessage("DICTIONARY_INFO_015", pos.getLabel()));
                }
                Iterator<IndexWord> ii = getIndexWordIterator(pos);
                while (ii.hasNext()) {
                    IndexWord iw = ii.next();
                    //lex ids should be unique within lex file name
                    //lex file name -> list of words
                    Map<Long, List<Word>> words = new HashMap<>();
                    for (Synset sense : iw.getSenses()) {
                        for (Word word : sense.getWords()) {
                            if (word.getLemma().equalsIgnoreCase(iw.getLemma())) {
                                List<Word> list = words.computeIfAbsent(sense.getLexFileNum(), k -> new ArrayList<>());
                                list.add(word);
                            }
                        }
                    }

                    for (Map.Entry<Long, List<Word>> entry : words.entrySet()) {
                        List<Word> list = entry.getValue();
                        list.sort(wordLexIdComparator);
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
                if (log.isDebugEnabled()) {
                    log.debug(messages.resolveMessage("DICTIONARY_INFO_016", pos.getLabel()));
                }
            }
        }
    }

    /**
     * Deletes dictionary files.
     *
     * @return true if deleted
     * @throws JWNLException JWNLException
     */
    public synchronized boolean delete() throws JWNLException {
        return false;
    }

    /**
     * Adds dictionary element to the dictionary.
     *
     * @param element element to add
     * @throws JWNLException JWNLException
     */
    public void addElement(DictionaryElement element) throws JWNLException {
        checkEditable();
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
        checkEditable();
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
        checkEditable();
        return elementFactory.createException(pos, lemma, exceptions);
    }

    /**
     * Adds exception to the dictionary.
     *
     * @param exc exception to add
     * @throws JWNLException JWNLException
     */
    public void addException(Exc exc) throws JWNLException {
        checkEditable();
        exc.setDictionary(this);
    }

    /**
     * Removes the <var>exc</var> from the dictionary.
     *
     * @param exc exc to be removed
     * @throws JWNLException JWNLException
     */
    public void removeException(Exc exc) throws JWNLException {
        checkEditable();
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
        checkEditable();
        return elementFactory.createSynset(pos);
    }

    /**
     * Adds synset to the dictionary.
     *
     * @param synset synset to add
     * @throws JWNLException JWNLException
     */
    public void addSynset(Synset synset) throws JWNLException {
        checkEditable();
        synset.setDictionary(this);
    }

    /**
     * Removes <var>synset</var> from the dictionary.
     *
     * @param synset synset to remove
     * @throws JWNLException JWNLException
     */
    public synchronized void removeSynset(Synset synset) throws JWNLException {
        checkEditable();

        // take care of pointers
        // this will delete symmetric ones
        // asymmetric ones will be checked by the synset on gets and removed
        synset.getPointers().clear();

        synset.setDictionary(null);

        // take care of index words
        List<Word> copy = new ArrayList<>(synset.getWords());
        for (Word word : copy) {
            IndexWord indexWord = getIndexWord(synset.getPOS(), word.getLemma());
            if (null != indexWord) {
                indexWord.getSenses().remove(synset);
            }
        }
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
        checkEditable();
        return elementFactory.createIndexWord(pos, lemma, synset);
    }

    /**
     * Adds index word to the dictionary.
     *
     * @param indexWord index word to add
     * @throws JWNLException JWNLException
     */
    public void addIndexWord(IndexWord indexWord) throws JWNLException {
        checkEditable();
        indexWord.setDictionary(this);
    }

    /**
     * Removes <var>indexWord</var> from the dictionary.
     *
     * @param indexWord index word to remove
     * @throws JWNLException JWNLException
     */
    public synchronized void removeIndexWord(IndexWord indexWord) throws JWNLException {
        checkEditable();

        indexWord.setDictionary(null);

        // take care of words in synsets
        List<Synset> copy = new ArrayList<>(indexWord.getSenses());
        for (Synset synset : copy) {
            List<Word> wordsCopy = new ArrayList<>(synset.getWords());
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
     * Returns the frames at the indexes encoded in <var>l</var>.
     * Verb Frames are encoded within <code>Word</code>s as a long. Each bit represents
     * the frame at its corresponding index. If the bit is set, that verb
     * frame is valid for the word.
     *
     * @param bits frame flags
     * @return the frames at the indexes encoded in <var>l</var>
     */
    public String[] getFrames(BitSet bits) {
        return getFrames(bits, verbFrames);
    }

    /**
     * Returns the frames at the indexes encoded in <var>l</var>.
     * Verb Frames are encoded within <code>Word</code>s as a long. Each bit represents
     * the frame at its corresponding index. If the bit is set, that verb
     * frame is valid for the word.
     *
     * @param bits       frame flags
     * @param verbFrames frames
     * @return the frames at the indexes encoded in <var>l</var>
     */
    public static String[] getFrames(BitSet bits, String[] verbFrames) {
        int[] indices = getVerbFrameIndices(bits);
        String[] frames = new String[indices.length];
        for (int i = 0; i < indices.length; i++) {
            frames[i] = verbFrames[indices[i] - 1];
        }
        return frames;
    }

    /**
     * Returns the verb frame indices for a synset. This is the collection
     * of f_num values for a synset definition. In the case of a synset, this
     * is only the values that are true for all words with the synset. In other
     * words, only the sentence frames that belong to all words.
     *
     * @param bits the bit set
     * @return an integer collection
     */
    public static int[] getVerbFrameIndices(BitSet bits) {
        int[] indices = new int[bits.cardinality()];
        int index = 0;
        for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
            indices[index++] = i;
        }

        return indices;
    }

    public Random getRandom() {
        Random localRef = rand;
        if (localRef == null) {
            synchronized (this) {
                localRef = rand;
                if (localRef == null) {
                    rand = localRef = new Random(new Date().getTime());
                }
            }
        }
        return localRef;
    }

    public void setRandom(Random rand) {
        this.rand = rand;
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

    /**
     * Checks whether dictionary is editable and throws if not;
     *
     * @throws JWNLException JWNLException
     */
    protected void checkEditable() throws JWNLException {
        if (!isEditable()) {
            throw new JWNLException(messages.resolveMessage("DICTIONARY_EXCEPTION_029"));
        }
    }

    /**
     * Loads all targets in load all pointers and all synsets in all index words.
     *
     * @throws JWNLException JWNLException
     */
    protected void resolveAllPointers() throws JWNLException {
        for (POS pos : POS.getAllPOS()) {
            resolvePointers(pos);
        }
    }

    protected void resolvePointers(POS pos) throws JWNLException {
        if (log.isDebugEnabled()) {
            log.debug(getMessages().resolveMessage("DICTIONARY_INFO_013", pos.getLabel()));
        }

        {
            Iterator<Synset> si = getSynsetIterator(pos);
            while (si.hasNext()) {
                Synset s = si.next();
                for (Pointer p : s.getPointers()) {
                    // resolve pointers
                    p.getTarget();
                }
            }
        }

        {
            Iterator<IndexWord> ii = getIndexWordIterator(pos);
            while (ii.hasNext()) {
                IndexWord iw = ii.next();
                // load synsets
                iw.getSenses().iterator();
            }
        }
    }

    private List<Param> getParams(Dictionary dictionary, NodeList list) throws JWNLException {
        List<Param> params = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(PARAM_TAG)) {
                String name = getAttribute(n, NAME_ATTRIBUTE);
                String value = getAttribute(n, VALUE_ATTRIBUTE);
                if (name == null && value == null) {
                    throw new JWNLException(messages.resolveMessage("JWNL_EXCEPTION_008"));
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
            return new Locale(language);
        } else {
            return new Locale(language, country);
        }
    }

    private static String getResourceProperties(String resourceName) throws IOException {
        try (final InputStream inputStream = Dictionary.class.getResourceAsStream(resourceName)) {
            try (final BufferedReader fileCheck = new BufferedReader(new InputStreamReader(inputStream))) {
                final StringBuilder fileText = new StringBuilder();
                String line;
                while (null != (line = fileCheck.readLine())) {
                    fileText.append(line).append("\n");
                }
                return fileText.toString();
            }
        }
    }
}