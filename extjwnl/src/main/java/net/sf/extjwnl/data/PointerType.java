package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.util.Resolvable;

import java.io.Serializable;
import java.util.*;

/**
 * Instances of this class enumerate the possible WordNet pointer types,
 * and are used to label <code>PointerType</code>s. Each <code>PointerType</code>
 * carries additional information: a human-readable label, an optional reflexive
 * type that labels links pointing the opposite direction, an encoding of
 * parts-of-speech that it applies to, and a short string that represents it in
 * the dictionary files.
 *
 * @author John Didion <jdidion@users.sourceforge.net>
 * @author Aliaksandr Autayeu <avtaev@gmail.com>
 */
public class PointerType implements Serializable {

    private static final long serialVersionUID = 1L;

    // Flags for tagging a pointer type with the POS types it apples to.
    private static final int N = 1;
    private static final int V = 2;
    private static final int ADJ = 4;
    private static final int ADV = 8;
    private static final int LEXICAL = 16;

    private static final String ANTONYM_KEY = "!";
    private static final String HYPERNYM_KEY = "@";
    private static final String HYPONYM_KEY = "~";
    private static final String ATTRIBUTE_KEY = "=";
    private static final String ALSO_SEE_KEY = "^";
    private static final String ENTAILMENT_KEY = "*";
    private static final String ENTAILED_BY_KEY = "?";
    private static final String CAUSE_KEY = ">";
    private static final String VERB_GROUP_KEY = "$";
    private static final String MEMBER_HOLONYM_KEY = "#m";
    private static final String SUBSTANCE_HOLONYM_KEY = "#s";
    private static final String PART_HOLONYM_KEY = "#p";
    private static final String MEMBER_MERONYM_KEY = "%m";
    private static final String SUBSTANCE_MERONYM_KEY = "%s";
    private static final String PART_MERONYM_KEY = "%p";
    private static final String SIMILAR_KEY = "&";
    private static final String PARTICIPLE_OF_KEY = "<";
    private static final String DERIVED_KEY = "\\";
    private static final String PERTAINYM_KEY = "\\";
    private static final String NOMINALIZATION_KEY = "+";
    private static final String CATEGORY_DOMAIN_KEY = ";c";
    private static final String CATEGORY_MEMBER_KEY = "-c";
    private static final String REGION_DOMAIN_KEY = ";r";
    private static final String REGION_MEMBER_KEY = "-r";
    private static final String USAGE_DOMAIN_KEY = ";u";
    private static final String USAGE_MEMBER_KEY = "-u";
    private static final String INSTANCE_HYPERNYM_KEY = "@i";
    private static final String INSTANCES_HYPONYM_KEY = "~i";

    // All categories
    public static final PointerType ANTONYM = new PointerType("ANTONYM", ANTONYM_KEY, N | V | ADJ | ADV | LEXICAL);
    public static final PointerType CATEGORY = new PointerType("CATEGORY_DOMAIN", CATEGORY_DOMAIN_KEY, N | V | ADJ | ADV | LEXICAL);
    public static final PointerType REGION = new PointerType("REGION_DOMAIN", REGION_DOMAIN_KEY, N | V | ADJ | ADV | LEXICAL);
    public static final PointerType USAGE = new PointerType("USAGE_DOMAIN", USAGE_DOMAIN_KEY, N | V | ADJ | ADV | LEXICAL);

    // Nouns and Verbs
    public static final PointerType HYPERNYM = new PointerType("HYPERNYM", HYPERNYM_KEY, N | V);
    public static final PointerType HYPONYM = new PointerType("HYPONYM", HYPONYM_KEY, N | V);
    public static final PointerType NOMINALIZATION = new PointerType("NOMINALIZATION", NOMINALIZATION_KEY, N | V);

    public static final PointerType INSTANCE_HYPERNYM = new PointerType("INSTANCE_HYPERNYM", INSTANCE_HYPERNYM_KEY, N | V);
    public static final PointerType INSTANCES_HYPONYM = new PointerType("INSTANCES_HYPONYM", INSTANCES_HYPONYM_KEY, N | V);

    // Nouns and Adjectives
    public static final PointerType ATTRIBUTE = new PointerType("ATTRIBUTE", ATTRIBUTE_KEY, N | ADJ);
    public static final PointerType SEE_ALSO = new PointerType("ALSO_SEE", ALSO_SEE_KEY, N | V | ADJ | LEXICAL);

    // Nouns
    public static final PointerType MEMBER_HOLONYM = new PointerType("MEMBER_HOLONYM", MEMBER_HOLONYM_KEY, N);
    public static final PointerType SUBSTANCE_HOLONYM = new PointerType("SUBSTANCE_HOLONYM", SUBSTANCE_HOLONYM_KEY, N);
    public static final PointerType PART_HOLONYM = new PointerType("PART_HOLONYM", PART_HOLONYM_KEY, N);
    public static final PointerType MEMBER_MERONYM = new PointerType("MEMBER_MERONYM", MEMBER_MERONYM_KEY, N);
    public static final PointerType SUBSTANCE_MERONYM = new PointerType("SUBSTANCE_MERONYM", SUBSTANCE_MERONYM_KEY, N);
    public static final PointerType PART_MERONYM = new PointerType("PART_MERONYM", PART_MERONYM_KEY, N);
    public static final PointerType CATEGORY_MEMBER = new PointerType("CATEGORY_MEMBER", CATEGORY_MEMBER_KEY, N);
    public static final PointerType REGION_MEMBER = new PointerType("REGION_MEMBER", REGION_MEMBER_KEY, N);
    public static final PointerType USAGE_MEMBER = new PointerType("USAGE_MEMBER", USAGE_MEMBER_KEY, N);

    // Verbs
    public static final PointerType ENTAILMENT = new PointerType("ENTAILMENT", ENTAILMENT_KEY, V);
    public static final PointerType ENTAILED_BY = new PointerType("ENTAILED_BY", ENTAILED_BY_KEY, V);
    public static final PointerType CAUSE = new PointerType("CAUSE", CAUSE_KEY, V);
    public static final PointerType VERB_GROUP = new PointerType("VERB_GROUP", VERB_GROUP_KEY, V);

    // Adjectives
    public static final PointerType SIMILAR_TO = new PointerType("SIMILAR", SIMILAR_KEY, ADJ);
    public static final PointerType PARTICIPLE_OF = new PointerType("PARTICIPLE_OF", PARTICIPLE_OF_KEY, ADJ | LEXICAL);
    public static final PointerType PERTAINYM = new PointerType("PERTAINYM", PERTAINYM_KEY, ADJ | LEXICAL);

    // Adverbs
    public static final PointerType DERIVED = new PointerType("DERIVED", DERIVED_KEY, ADV);

    /**
     * A list of all <code>PointerType</code>s.
     */
    private static final List<PointerType> ALL_TYPES = Collections.unmodifiableList(Arrays.asList(
            ANTONYM, HYPERNYM, HYPONYM, ATTRIBUTE, SEE_ALSO, ENTAILMENT, ENTAILED_BY, CAUSE, VERB_GROUP,
            MEMBER_MERONYM, SUBSTANCE_MERONYM, PART_MERONYM, MEMBER_HOLONYM, SUBSTANCE_HOLONYM, PART_HOLONYM,
            SIMILAR_TO, PARTICIPLE_OF, DERIVED, NOMINALIZATION, CATEGORY, REGION, USAGE, CATEGORY_MEMBER,
            REGION_MEMBER, USAGE_MEMBER, INSTANCE_HYPERNYM, INSTANCES_HYPONYM
    ));

    private static final Map<POS, Integer> POS_TO_MASK_MAP = new HashMap<POS, Integer>();
    private static final Map<String, PointerType> KEY_TO_POINTER_TYPE_MAP = new HashMap<String, PointerType>();

    private static boolean initialized = false;

    public static void initialize() {
        if (!initialized) {
            POS_TO_MASK_MAP.put(POS.NOUN, N);
            POS_TO_MASK_MAP.put(POS.VERB, V);
            POS_TO_MASK_MAP.put(POS.ADJECTIVE, ADJ);
            POS_TO_MASK_MAP.put(POS.ADVERB, ADV);

            for (PointerType pt : ALL_TYPES) {
                KEY_TO_POINTER_TYPE_MAP.put(pt.getKey(), pt);
            }

            initialized = true;
        }
    }

    static {
        setSymmetric(ANTONYM, ANTONYM);
        setSymmetric(HYPERNYM, HYPONYM);
        setSymmetric(MEMBER_MERONYM, MEMBER_HOLONYM);
        setSymmetric(SUBSTANCE_MERONYM, SUBSTANCE_HOLONYM);
        setSymmetric(PART_MERONYM, PART_HOLONYM);
        setSymmetric(SIMILAR_TO, SIMILAR_TO);
        setSymmetric(ATTRIBUTE, ATTRIBUTE);
        setSymmetric(VERB_GROUP, VERB_GROUP);
        setSymmetric(ENTAILMENT, ENTAILED_BY);
        setSymmetric(CATEGORY, CATEGORY_MEMBER);
        setSymmetric(REGION, REGION_MEMBER);
        setSymmetric(USAGE, USAGE_MEMBER);
        setSymmetric(NOMINALIZATION, NOMINALIZATION);
        setSymmetric(INSTANCE_HYPERNYM, INSTANCES_HYPONYM);
    }

    /**
     * Returns true if <var>type</var> is a symmetric pointer type (it is its own symmetric type).
     *
     * @param type pointer type
     * @return if <var>type</var> is a symmetric pointer type
     */
    public static boolean isSymmetric(PointerType type) {
        return type.symmetricTo(type);
    }

    /**
     * Return the <code>PointerType</code> whose key matches <var>key</var>.
     *
     * @param key pointer type key
     * @return the <code>PointerType</code> whose key matches <var>key</var>
     */
    public static PointerType getPointerTypeForKey(String key) {
        return KEY_TO_POINTER_TYPE_MAP.get(key);
    }

    public static List<PointerType> getAllPointerTypes() {
        return ALL_TYPES;
    }

    public static List<PointerType> getAllPointerTypesForPOS(POS pos) {
        List<PointerType> types = new ArrayList<PointerType>();
        for (PointerType pt : ALL_TYPES) {
            if (pt.appliesTo(pos)) {
                types.add(pt);
            }
        }
        return Collections.unmodifiableList(types);
    }

    /**
     * Sets <var>a</var> as <var>b</var>'s symmetric type, and vice versa.
     *
     * @param a pointer type
     * @param b pointer type
     */
    private static void setSymmetric(PointerType a, PointerType b) {
        a.symmetricType = b;
        b.symmetricType = a;
    }

    private static int getPOSMask(POS pos) {
        return POS_TO_MASK_MAP.get(pos);
    }

    private Resolvable label;
    private String key;
    private int flags;
    /**
     * The PointerType that is the revers of this PointerType
     */
    private PointerType symmetricType;

    private PointerType(String label, String key, int flags) {
        this.label = new Resolvable(label);
        this.key = key;
        this.flags = flags;
    }

    public String toString() {
        return JWNL.resolveMessage("DATA_TOSTRING_011", new Object[]{getLabel(), getKey(), getFlagsAsString()});
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label.toString();
    }

    /**
     * Returns whether or not this PointerType can be associated with <var>pos</var>.
     *
     * @param pos part of speech
     * @return true if this PointerType can be associated with <var>pos</var>
     */
    public boolean appliesTo(POS pos) {
        return (flags & getPOSMask(pos)) != 0;
    }

    public boolean isSymmetric() {
        return symmetricTo(this);
    }

    /**
     * Returns true if <var>type</var> is symmetric to this pointer type.
     *
     * @param type pointer type
     * @return true if <var>type</var> is symmetric to this pointer type
     */
    public boolean symmetricTo(PointerType type) {
        return getSymmetricType() != null && getSymmetricType().equals(type);
    }

    /**
     * Returns the pointer type that is symmetric to this type.
     *
     * @return the pointer type that is symmetric to this type
     */
    public PointerType getSymmetricType() {
        return symmetricType;
    }

    public int hashCode() {
        return getLabel().hashCode();
    }

    private String flagStringCache = null;

    private String getFlagsAsString() {
        if (flagStringCache == null) {
            String str = "";
            if ((flags & N) != 0) {
                str += JWNL.resolveMessage("NOUN") + ", ";
            }
            if ((flags & V) != 0) {
                str += JWNL.resolveMessage("VERB") + ", ";
            }
            if ((flags & ADJ) != 0) {
                str += JWNL.resolveMessage("ADJECTIVE") + ", ";
            }
            if ((flags & ADV) != 0) {
                str += JWNL.resolveMessage("ADVERB") + ", ";
            }
            if ((flags & LEXICAL) != 0) {
                str += JWNL.resolveMessage("LEXICAL") + ", ";
            }
            flagStringCache = str.substring(0, str.length() - 2);
        }
        return flagStringCache;
    }
}