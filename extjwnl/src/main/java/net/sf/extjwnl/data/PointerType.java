package net.sf.extjwnl.data;

import net.sf.extjwnl.util.ResourceBundleSet;

import java.util.*;

/**
 * Instances of this class enumerate the possible WordNet pointer types,
 * and are used to label <code>PointerType</code>s. Each <code>PointerType</code>
 * carries additional information: a human-readable label, an optional reflexive
 * type that labels links pointing the opposite direction, an encoding of
 * parts-of-speech that it applies to, and a short string that represents it in
 * the dictionary files.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
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

    ANTONYM("!", "antonym", PointerTypeFlags.N | PointerTypeFlags.V | PointerTypeFlags.ADJ | PointerTypeFlags.ADV),
    HYPERNYM("@", "hypernym", PointerTypeFlags.N | PointerTypeFlags.V),
    HYPONYM("~", "hyponym", PointerTypeFlags.N | PointerTypeFlags.V),
    ENTAILMENT("*", "entailment", PointerTypeFlags.V),
    SIMILAR_TO("&", "similar", PointerTypeFlags.ADJ),
    MEMBER_HOLONYM("#m", "member holonym", PointerTypeFlags.N),
    SUBSTANCE_HOLONYM("#s", "substance holonym", PointerTypeFlags.N),
    PART_HOLONYM("#p", "part holonym", PointerTypeFlags.N),
    MEMBER_MERONYM("%m", "member meronym", PointerTypeFlags.N),
    SUBSTANCE_MERONYM("%s", "substance meronym", PointerTypeFlags.N),
    PART_MERONYM("%p", "part meronym", PointerTypeFlags.N),
    //    "#",			/* 12 MERONYM (noun) */
//    "%",			/* 13 HOLONYM (noun) */
    CAUSE(">", "cause", PointerTypeFlags.V),
    PARTICIPLE_OF("<", "participle of", PointerTypeFlags.ADJ),
    SEE_ALSO("^", "also see", PointerTypeFlags.N | PointerTypeFlags.V | PointerTypeFlags.ADJ),
    PERTAINYM("\\", "pertainym", PointerTypeFlags.ADJ | PointerTypeFlags.ADV),
    ATTRIBUTE("=", "attribute", PointerTypeFlags.N | PointerTypeFlags.ADJ),
    VERB_GROUP("$", "verb group", PointerTypeFlags.V),
    DERIVATION("+", "derivation", PointerTypeFlags.N | PointerTypeFlags.V | PointerTypeFlags.ADJ | PointerTypeFlags.ADV),
    DOMAIN_ALL(";", "domain", PointerTypeFlags.N | PointerTypeFlags.V | PointerTypeFlags.ADJ | PointerTypeFlags.ADV),
    MEMBER_ALL("-", "member", PointerTypeFlags.N),
    CATEGORY(";c", "category domain", PointerTypeFlags.N | PointerTypeFlags.V | PointerTypeFlags.ADJ | PointerTypeFlags.ADV),
    USAGE(";u", "usage domain", PointerTypeFlags.N | PointerTypeFlags.V | PointerTypeFlags.ADJ | PointerTypeFlags.ADV),
    REGION(";r", "region domain", PointerTypeFlags.N | PointerTypeFlags.V | PointerTypeFlags.ADJ | PointerTypeFlags.ADV),
    CATEGORY_MEMBER("-c", "member of category domain", PointerTypeFlags.N),
    USAGE_MEMBER("-u", "member of usage domain", PointerTypeFlags.N),
    REGION_MEMBER("-r", "member of region domain", PointerTypeFlags.N),
    INSTANCE_HYPERNYM("@i", "instance hypernym", PointerTypeFlags.N | PointerTypeFlags.V),
    INSTANCES_HYPONYM("~i", "instances hyponym", PointerTypeFlags.N | PointerTypeFlags.V);

    private static final Map<POS, Integer> POS_TO_MASK_MAP = new EnumMap<>(POS.class);

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
            CAUSE, PARTICIPLE_OF, SEE_ALSO, PERTAINYM, ATTRIBUTE, VERB_GROUP, DERIVATION,
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
        setSymmetric(DERIVATION, DERIVATION);
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
        if (DERIVATION.getKey().equals(key)) {
            return DERIVATION;
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

    /**
     * Return the <code>PointerType</code> whose key matches <var>key</var> and applies to <var>pos</var>.
     *
     * @param key pointer type key
     * @return the <code>PointerType</code> whose key matches <var>key</var>
     */
    public static PointerType getPointerTypeForKey(CharSequence key) {
        if (0 == key.length()) {
            return null;
        } else {
            if (key.charAt(0) == ANTONYM.getKey().charAt(0)) {
                return ANTONYM;
            }
            if (key.charAt(0) == HYPERNYM.getKey().charAt(0)) {
                if (1 == key.length()) {
                    return HYPERNYM;
                } else if (2 == key.length() && key.charAt(1) == INSTANCE_HYPERNYM.getKey().charAt(1)) {
                    return INSTANCE_HYPERNYM;
                } else {
                    return null;
                }
            }
            if (key.charAt(0) == HYPONYM.getKey().charAt(0)) {
                if (1 == key.length()) {
                    return HYPONYM;
                } else if (2 == key.length() && key.charAt(1) == INSTANCES_HYPONYM.getKey().charAt(1)) {
                    return INSTANCES_HYPONYM;
                } else {
                    return null;
                }
            }
            if (key.charAt(0) == ENTAILMENT.getKey().charAt(0)) {
                return ENTAILMENT;
            }
            if (key.charAt(0) == SIMILAR_TO.getKey().charAt(0)) {
                return SIMILAR_TO;
            }
            if (key.charAt(0) == CAUSE.getKey().charAt(0)) {
                return CAUSE;
            }
            if (key.charAt(0) == PARTICIPLE_OF.getKey().charAt(0)) {
                return PARTICIPLE_OF;
            }
            if (key.charAt(0) == SEE_ALSO.getKey().charAt(0)) {
                return SEE_ALSO;
            }
            if (key.charAt(0) == PERTAINYM.getKey().charAt(0)) {
                return PERTAINYM;
            }
            if (key.charAt(0) == ATTRIBUTE.getKey().charAt(0)) {
                return ATTRIBUTE;
            }
            if (key.charAt(0) == VERB_GROUP.getKey().charAt(0)) {
                return VERB_GROUP;
            }
            if (key.charAt(0) == DERIVATION.getKey().charAt(0)) {
                return DERIVATION;
            }
            if (key.charAt(0) == DOMAIN_ALL.getKey().charAt(0)) {
                if (1 == key.length()) {
                    return DOMAIN_ALL;
                } else if (2 == key.length()) {
                    if (key.charAt(1) == CATEGORY.getKey().charAt(1)) {
                        return CATEGORY;
                    }
                    if (key.charAt(1) == USAGE.getKey().charAt(1)) {
                        return USAGE;
                    }
                    if (key.charAt(1) == REGION.getKey().charAt(1)) {
                        return REGION;
                    }
                } else {
                    return null;
                }

            }
            if (key.charAt(0) == MEMBER_ALL.getKey().charAt(0)) {
                if (1 == key.length()) {
                    return MEMBER_ALL;
                } else if (2 == key.length()) {
                    if (key.charAt(1) == CATEGORY_MEMBER.getKey().charAt(1)) {
                        return CATEGORY_MEMBER;
                    }
                    if (key.charAt(1) == USAGE_MEMBER.getKey().charAt(1)) {
                        return USAGE_MEMBER;
                    }
                    if (key.charAt(1) == REGION_MEMBER.getKey().charAt(1)) {
                        return REGION_MEMBER;
                    }
                } else {
                    return null;
                }
            }
            if (key.charAt(0) == MEMBER_HOLONYM.getKey().charAt(0)) {
                if (2 == key.length()) {
                    if (key.charAt(1) == MEMBER_HOLONYM.getKey().charAt(1)) {
                        return MEMBER_HOLONYM;
                    }
                    if (key.charAt(1) == SUBSTANCE_HOLONYM.getKey().charAt(1)) {
                        return SUBSTANCE_HOLONYM;
                    }
                    if (key.charAt(1) == PART_HOLONYM.getKey().charAt(1)) {
                        return PART_HOLONYM;
                    }
                } else {
                    return null;
                }
            }
            if (key.charAt(0) == MEMBER_MERONYM.getKey().charAt(0)) {
                if (2 == key.length()) {
                    if (key.charAt(1) == MEMBER_MERONYM.getKey().charAt(1)) {
                        return MEMBER_MERONYM;
                    }
                    if (key.charAt(1) == SUBSTANCE_MERONYM.getKey().charAt(1)) {
                        return SUBSTANCE_MERONYM;
                    }
                    if (key.charAt(1) == PART_MERONYM.getKey().charAt(1)) {
                        return PART_MERONYM;
                    }
                } else {
                    return null;
                }
            }
            return null;
        }
    }

    public static List<PointerType> getAllPointerTypes() {
        return ALL_TYPES;
    }

    public static List<PointerType> getAllPointerTypesForPOS(POS pos) {
        List<PointerType> types = new ArrayList<>();
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

    PointerType(String key, String label, int flags) {
        this.key = key;
        this.label = label;
        this.flags = flags;
    }

    public String toString() {
        return ResourceBundleSet.insertParams("[PointerType: [Label: {0}] [Key: {1}] Applies To: {2}]",
                new Object[]{getLabel(), getKey(), getFlagsAsString()});
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
                str += "noun" + ", ";
            }
            if ((flags & PointerTypeFlags.V) != 0) {
                str += "verb" + ", ";
            }
            if ((flags & PointerTypeFlags.ADJ) != 0) {
                str += "adjective" + ", ";
            }
            if ((flags & PointerTypeFlags.ADV) != 0) {
                str += "adverb" + ", ";
            }
            flagStringCache = str.substring(0, str.length() - 2);
        }
        return flagStringCache;
    }
}