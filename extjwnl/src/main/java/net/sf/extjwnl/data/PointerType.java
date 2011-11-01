package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNL;

import java.util.*;

/**
 * Instances of this class enumerate the possible WordNet pointer types,
 * and are used to label <code>PointerType</code>s. Each <code>PointerType</code>
 * carries additional information: a human-readable label, an optional reflexive
 * type that labels links pointing the opposite direction, an encoding of
 * parts-of-speech that it applies to, and a short string that represents it in
 * the dictionary files.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public enum PointerType {

//    from globals.c
//    "!",			/* 1 ANTPTR (all) */
//    "@",			/* 2 HYPERPTR (noun, verb) */
//    "~",			/* 3 HYPOPTR (noun, verb) */
//    "*",			/* 4 ENTAILPTR (verb) */
//    "&",			/* 5 SIMPTR (adjective) */
//    "#m",			/* 6 ISMEMBERPTR (noun) */
//    "#s",			/* 7 ISSTUFFPTR (noun) */
//    "#p",			/* 8 ISPARTPTR (noun) */
//    "%m",			/* 9 HASMEMBERPTR (noun) */
//    "%s",			/* 10 HASSTUFFPTR (noun) */
//    "%p",			/* 11 HASPARTPTR (noun) */
//    "#",			/* 12 MERONYM (noun) */
//    "%",			/* 13 HOLONYM (noun) */
//    ">",			/* 14 CAUSETO (verb) */
//    "<",			/* 15 PPLPTR (adj) */
//    "^",			/* 16 SEEALSOPTR (adjective, verb) */
//    "\\",			/* 17 PERTAINSTO (adjective, noun, adverb) */
//    "=",			/* 18 ATTRIBUTE (adjective, noun) */
//    "$",			/* 19 VERBGROUP (verb) */
//    "+",			/* 20 DERIVATION */
//    ";",			/* 21 CLASSIFICATION (all) */
//    "-",			/* 22 CLASS (all) */
//    "",				/* 23 SYNS */
//    "",				/* 24 FREQ */
//    "",				/* 25 FRAMES */
//    "",				/* 26 COORDS */
//    "",				/* 27 RELATIVES */
//    "",				/* 28 HMERONYM */
//    "",				/* 29 HHOLONYM */
//    "",				/* 30 WNGREP */
//    "",				/* 31 OVERVIEW */
//    ";c",			/* 32 classification CATEGORY */
//    ";u",			/* 33 classification USAGE */
//    ";r",			/* 34 classificaiton REGIONAL */
//    "-c",			/* 35 class CATEGORY */
//    "-u",			/* 36 class USAGE */
//    "-r",			/* 37 class REGIONAL */
//    "@i", 			/* 38 INSTANCE (noun) */
//    "~i",			/* 39 INSTANCES (noun) */

    ANTONYM("!", "ANTONYM", PointerTypeFlags.N | PointerTypeFlags.V | PointerTypeFlags.ADJ | PointerTypeFlags.ADV | PointerTypeFlags.LEXICAL),
    HYPERNYM("@", "HYPERNYM", PointerTypeFlags.N | PointerTypeFlags.V),
    HYPONYM("~", "HYPONYM", PointerTypeFlags.N | PointerTypeFlags.V),
    ENTAILMENT("*", "ENTAILMENT", PointerTypeFlags.V),
    SIMILAR_TO("&", "SIMILAR", PointerTypeFlags.ADJ),
    MEMBER_HOLONYM("#m", "MEMBER_HOLONYM", PointerTypeFlags.N),
    SUBSTANCE_HOLONYM("#s", "SUBSTANCE_HOLONYM", PointerTypeFlags.N),
    PART_HOLONYM("#p", "PART_HOLONYM", PointerTypeFlags.N),
    MEMBER_MERONYM("%m", "MEMBER_MERONYM", PointerTypeFlags.N),
    SUBSTANCE_MERONYM("%s", "SUBSTANCE_MERONYM", PointerTypeFlags.N),
    PART_MERONYM("%p", "PART_MERONYM", PointerTypeFlags.N),
    //    "#",			/* 12 MERONYM (noun) */
//    "%",			/* 13 HOLONYM (noun) */
    CAUSE(">", "CAUSE", PointerTypeFlags.V),
    PARTICIPLE_OF("<", "PARTICIPLE_OF", PointerTypeFlags.ADJ | PointerTypeFlags.LEXICAL),
    SEE_ALSO("^", "ALSO_SEE", PointerTypeFlags.N | PointerTypeFlags.V | PointerTypeFlags.ADJ | PointerTypeFlags.LEXICAL),
    PERTAINYM("\\", "PERTAINYM", PointerTypeFlags.ADJ | PointerTypeFlags.ADV | PointerTypeFlags.LEXICAL),
    ATTRIBUTE("=", "ATTRIBUTE", PointerTypeFlags.N | PointerTypeFlags.ADJ),
    VERB_GROUP("$", "VERB_GROUP", PointerTypeFlags.V),
    NOMINALIZATION("+", "NOMINALIZATION", PointerTypeFlags.N | PointerTypeFlags.V),
    DOMAIN_ALL(";", "DOMAIN_ALL", PointerTypeFlags.N | PointerTypeFlags.V | PointerTypeFlags.ADJ | PointerTypeFlags.ADV | PointerTypeFlags.LEXICAL),
    MEMBER_ALL("-", "MEMBER_ALL", PointerTypeFlags.N),
    CATEGORY(";c", "CATEGORY_DOMAIN", PointerTypeFlags.N | PointerTypeFlags.V | PointerTypeFlags.ADJ | PointerTypeFlags.ADV | PointerTypeFlags.LEXICAL),
    USAGE(";u", "USAGE_DOMAIN", PointerTypeFlags.N | PointerTypeFlags.V | PointerTypeFlags.ADJ | PointerTypeFlags.ADV | PointerTypeFlags.LEXICAL),
    REGION(";r", "REGION_DOMAIN", PointerTypeFlags.N | PointerTypeFlags.V | PointerTypeFlags.ADJ | PointerTypeFlags.ADV | PointerTypeFlags.LEXICAL),
    CATEGORY_MEMBER("-c", "CATEGORY_MEMBER", PointerTypeFlags.N),
    USAGE_MEMBER("-u", "USAGE_MEMBER", PointerTypeFlags.N),
    REGION_MEMBER("-r", "REGION_MEMBER", PointerTypeFlags.N),
    INSTANCE_HYPERNYM("@i", "INSTANCE_HYPERNYM", PointerTypeFlags.N | PointerTypeFlags.V),
    INSTANCES_HYPONYM("~i", "INSTANCES_HYPONYM", PointerTypeFlags.N | PointerTypeFlags.V);

    private static final Map<POS, Integer> POS_TO_MASK_MAP = new EnumMap<POS, Integer>(POS.class);

    static {
        POS_TO_MASK_MAP.put(POS.NOUN, PointerTypeFlags.N);
        POS_TO_MASK_MAP.put(POS.VERB, PointerTypeFlags.V);
        POS_TO_MASK_MAP.put(POS.ADJECTIVE, PointerTypeFlags.ADJ);
        POS_TO_MASK_MAP.put(POS.ADVERB, PointerTypeFlags.ADV);
    }

    /**
     * A list of all <code>PointerType</code>s.
     */
    private static final List<PointerType> ALL_TYPES = Collections.unmodifiableList(Arrays.asList(
            ANTONYM, HYPERNYM, HYPONYM, ENTAILMENT, SIMILAR_TO, MEMBER_HOLONYM, SUBSTANCE_HOLONYM,
            PART_HOLONYM, MEMBER_MERONYM, SUBSTANCE_MERONYM, PART_MERONYM,
            CAUSE, PARTICIPLE_OF, SEE_ALSO, PERTAINYM, ATTRIBUTE, VERB_GROUP, NOMINALIZATION,
            CATEGORY, USAGE, REGION, CATEGORY_MEMBER, USAGE_MEMBER, REGION_MEMBER,
            INSTANCE_HYPERNYM, INSTANCES_HYPONYM
    ));

    static {
        setSymmetric(ANTONYM, ANTONYM);
        setSymmetric(HYPERNYM, HYPONYM);
        setSymmetric(MEMBER_MERONYM, MEMBER_HOLONYM);
        setSymmetric(SUBSTANCE_MERONYM, SUBSTANCE_HOLONYM);
        setSymmetric(PART_MERONYM, PART_HOLONYM);
        setSymmetric(SIMILAR_TO, SIMILAR_TO);
        setSymmetric(ATTRIBUTE, ATTRIBUTE);
        setSymmetric(VERB_GROUP, VERB_GROUP);
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
     * Return the <code>PointerType</code> whose key matches <var>key</var> and applies to <var>pos</var>.
     *
     * @param key pointer type key
     * @return the <code>PointerType</code> whose key matches <var>key</var>
     */
    public static PointerType getPointerTypeForKey(String key) {
        if (ANTONYM.getKey().equals(key)) {
            return ANTONYM;
        }
        if (HYPERNYM.getKey().equals(key)) {
            return HYPERNYM;
        }
        if (HYPONYM.getKey().equals(key)) {
            return HYPONYM;
        }
        if (ENTAILMENT.getKey().equals(key)) {
            return ENTAILMENT;
        }
        if (SIMILAR_TO.getKey().equals(key)) {
            return SIMILAR_TO;
        }
        if (MEMBER_HOLONYM.getKey().equals(key)) {
            return MEMBER_HOLONYM;
        }
        if (SUBSTANCE_HOLONYM.getKey().equals(key)) {
            return SUBSTANCE_HOLONYM;
        }
        if (PART_HOLONYM.getKey().equals(key)) {
            return PART_HOLONYM;
        }
        if (MEMBER_MERONYM.getKey().equals(key)) {
            return MEMBER_MERONYM;
        }
        if (SUBSTANCE_MERONYM.getKey().equals(key)) {
            return SUBSTANCE_MERONYM;
        }
        if (PART_MERONYM.getKey().equals(key)) {
            return PART_MERONYM;
        }
        if (CAUSE.getKey().equals(key)) {
            return CAUSE;
        }
        if (PARTICIPLE_OF.getKey().equals(key)) {
            return PARTICIPLE_OF;
        }
        if (SEE_ALSO.getKey().equals(key)) {
            return SEE_ALSO;
        }
        if (PERTAINYM.getKey().equals(key)) {
            return PERTAINYM;
        }
        if (ATTRIBUTE.getKey().equals(key)) {
            return ATTRIBUTE;
        }
        if (VERB_GROUP.getKey().equals(key)) {
            return VERB_GROUP;
        }
        if (NOMINALIZATION.getKey().equals(key)) {
            return NOMINALIZATION;
        }
        if (DOMAIN_ALL.getKey().equals(key)) {
            return DOMAIN_ALL;
        }
        if (MEMBER_ALL.getKey().equals(key)) {
            return MEMBER_ALL;
        }
        if (CATEGORY.getKey().equals(key)) {
            return CATEGORY;
        }
        if (USAGE.getKey().equals(key)) {
            return USAGE;
        }
        if (REGION.getKey().equals(key)) {
            return REGION;
        }
        if (CATEGORY_MEMBER.getKey().equals(key)) {
            return CATEGORY_MEMBER;
        }
        if (USAGE_MEMBER.getKey().equals(key)) {
            return USAGE_MEMBER;
        }
        if (REGION_MEMBER.getKey().equals(key)) {
            return REGION_MEMBER;
        }
        if (INSTANCE_HYPERNYM.getKey().equals(key)) {
            return INSTANCE_HYPERNYM;
        }
        if (INSTANCES_HYPONYM.getKey().equals(key)) {
            return INSTANCES_HYPONYM;
        }
        return null;
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

    private final transient String label;
    private final transient int flags;
    private final transient String key;
    /**
     * The PointerType that is the revers of this PointerType
     */
    private transient PointerType symmetricType;

    private PointerType(String key, String label, int flags) {
        JWNL.initialize();
        this.key = key;
        this.label = JWNL.resolveMessage(label);
        this.flags = flags;
    }

    public String toString() {
        return JWNL.resolveMessage("DATA_TOSTRING_011", new Object[]{getLabel(), getKey(), getFlagsAsString()});
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
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

    public boolean isLexical() {
        return (flags & PointerTypeFlags.LEXICAL) != 0;
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

    public int getFlags() {
        return flags;
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

    private transient String flagStringCache = null;
    private String getFlagsAsString() {
        if (flagStringCache == null) {
            String str = "";
            if ((flags & PointerTypeFlags.N) != 0) {
                str += JWNL.resolveMessage("NOUN") + ", ";
            }
            if ((flags & PointerTypeFlags.V) != 0) {
                str += JWNL.resolveMessage("VERB") + ", ";
            }
            if ((flags & PointerTypeFlags.ADJ) != 0) {
                str += JWNL.resolveMessage("ADJECTIVE") + ", ";
            }
            if ((flags & PointerTypeFlags.ADV) != 0) {
                str += JWNL.resolveMessage("ADVERB") + ", ";
            }
            if ((flags & PointerTypeFlags.LEXICAL) != 0) {
                str += JWNL.resolveMessage("LEXICAL") + ", ";
            }
            flagStringCache = str.substring(0, str.length() - 2);
        }
        return flagStringCache;
    }
}