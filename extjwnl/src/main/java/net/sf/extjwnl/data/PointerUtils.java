package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNL;
import net.sf.extjwnl.data.list.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * This class contains static methods for performing various pointer operations. A pointer from one synset/word to
 * another connotes a relationship between those words. The type of the relationship is specified by the type
 * of pointer. See the WordNet documentation for information on pointer types. To avoid confusion with
 * the <code>Relationship</code> class, these relationships will be referred to as links.
 *
 * @author John Didion <jdidion@didion.net>
 */
public class PointerUtils {

    private static final Log log = LogFactory.getLog(PointerUtils.class);

    /**
     * Representation of infinite depth. Used to tell the pointer operations to
     * return all links to an infinite depth.
     */
    public static final int INFINITY = Integer.MAX_VALUE;

    private PointerUtils() {
    }

    /**
     * Returns the immediate parents of <code>synset</code>.
     *
     * @param synset synset
     * @return the immediate parents of <code>synset</code>
     */
    public static PointerTargetNodeList getDirectHypernyms(Synset synset) {
        return getPointerTargets(synset, PointerType.HYPERNYM);
    }

    /**
     * Returns all of the ancestors of <code>synset</code>.
     *
     * @param synset synset
     * @return all of the ancestors of <code>synset</code>
     */
    public static PointerTargetTree getHypernymTree(Synset synset) {
        return getHypernymTree(synset, INFINITY);
    }

    /**
     * Returns all of the ancestors of <code>synset</code> to depth <code>depth</code>.
     *
     * @param synset synset
     * @param depth  depth
     * @return all of the ancestors of <code>synset</code> to depth <code>depth</code>
     */
    public static PointerTargetTree getHypernymTree(Synset synset, int depth) {
        return new PointerTargetTree(synset, makePointerTargetTreeList(synset, PointerType.HYPERNYM, depth));
    }

    /**
     * Returns the immediate children of <code>synset</code>.
     *
     * @param synset synset
     * @return the immediate children of <code>synset</code>
     */
    public static PointerTargetNodeList getDirectHyponyms(Synset synset) {
        return getPointerTargets(synset, PointerType.HYPONYM);
    }

    /**
     * Returns all of the children of <code>synset</code>.
     *
     * @param synset synset
     * @return all of the children of <code>synset</code>
     */
    public static PointerTargetTree getHyponymTree(Synset synset) {
        return getHyponymTree(synset, INFINITY);
    }

    /**
     * Returns all of the children of <code>synset</code> to depth <code>depth</code>.
     *
     * @param synset synset
     * @param depth  depth
     * @return all of the children of <code>synset</code> to depth <code>depth</code>
     */
    public static PointerTargetTree getHyponymTree(Synset synset, int depth) {
        return new PointerTargetTree(synset, makePointerTargetTreeList(synset, PointerType.HYPONYM, depth));
    }

    /**
     * Returns <code>synset</code>'s siblings (the hyponyms of its hypernyms).
     *
     * @param synset synset
     * @return <code>synset</code>'s siblings (the hyponyms of its hypernyms)
     */
    public static PointerTargetNodeList getCoordinateTerms(Synset synset) {
        PointerTargetNodeList list = new PointerTargetNodeList();
        for (PointerTargetNode o : getDirectHypernyms(synset)) {
            list.addAll(getPointerTargets(o.getSynset(), PointerType.HYPONYM));
        }
        return list;
    }

    /**
     * Returns the words that mean the opposite of <code>synset</code>.
     *
     * @param synset synset
     * @return the words that mean the opposite of <code>synset</code>
     */
    public static PointerTargetNodeList getAntonyms(Synset synset) {
        return getPointerTargets(synset, PointerType.ANTONYM);
    }

    /**
     * Returns the words that mean the opposite of <code>synset</code> and the immediate synonyms of those words.
     *
     * @param synset synset
     * @return the words that mean the opposite of <code>synset</code> and the immediate synonyms of those words
     */
    public static PointerTargetTree getExtendedAntonyms(Synset synset) {
        return getExtendedAntonyms(synset, 1);
    }

    /**
     * Finds all antonyms of <code>synset</code>, and all synonyms of those antonyms to depth <code>depth</code>.
     *
     * @param synset synset
     * @param depth  depth
     * @return all antonyms of <code>synset</code>, and all synonyms of those antonyms to depth <code>depth</code>
     */
    public static PointerTargetTree getExtendedAntonyms(Synset synset, int depth) {
        PointerTargetTreeNodeList list = new PointerTargetTreeNodeList();
        if (synset.getPOS() == POS.ADJECTIVE) {
            PointerTargetNodeList antonyms = getAntonyms(synset);
            list = makePointerTargetTreeList(antonyms, PointerType.SIMILAR_TO, PointerType.ANTONYM, depth, false);
        }
        return new PointerTargetTree(new PointerTargetTreeNode(synset, list, null));
    }

    /**
     * Returns the immediate antonyms of all words that mean the same as <code>synset</code>.
     *
     * @param synset synset
     * @return the immediate antonyms of all words that mean the same as <code>synset</code>
     */
    public static PointerTargetTree getIndirectAntonyms(Synset synset) {
        return getIndirectAntonyms(synset, 1);
    }

    /**
     * Returns the antonyms of all words that mean the same as <code>synset</code> to depth <code>depth</code>.
     *
     * @param synset synset
     * @param depth  depth
     * @return the antonyms of all words that mean the same as <code>synset</code> to depth <code>depth</code>
     */
    public static PointerTargetTree getIndirectAntonyms(Synset synset, int depth) {
        PointerTargetTreeNodeList list = new PointerTargetTreeNodeList();
        if (synset.getPOS() == POS.ADJECTIVE) {
            PointerTargetNodeList synonyms = getSynonyms(synset);
            list = makePointerTargetTreeList(synonyms, PointerType.ANTONYM, PointerType.ANTONYM, depth, false);
        }
        return new PointerTargetTree(new PointerTargetTreeNode(synset, list, null));
    }

    /**
     * Returns the attributes of <code>synset</code>.
     *
     * @param synset synset
     * @return the attributes of <code>synset</code>
     */
    public static PointerTargetNodeList getAttributes(Synset synset) {
        return getPointerTargets(synset, PointerType.ATTRIBUTE);
    }

    /**
     * Finds what words are related to <code>synset</code>.
     *
     * @param synset synset
     * @return what words are related to <code>synset</code>
     */
    public static PointerTargetNodeList getAlsoSees(Synset synset) {
        return getPointerTargets(synset, PointerType.SEE_ALSO);
    }

    /**
     * Finds all See Also relations to depth <code>depth</code>.
     *
     * @param synset synset
     * @param depth  depth
     * @return all See Also relations to depth <code>depth</code>
     */
    public static PointerTargetTree getAlsoSeeTree(Synset synset, int depth) {
        return new PointerTargetTree(synset, makePointerTargetTreeList(synset, PointerType.SEE_ALSO, depth));
    }

    /**
     * Returns meronyms of <code>synset</code>.
     *
     * @param synset synset
     * @return meronyms of <code>synset</code>
     */
    public static PointerTargetNodeList getMeronyms(Synset synset) {
        PointerTargetNodeList list = new PointerTargetNodeList();
        list.addAll(getPartMeronyms(synset));
        list.addAll(getMemberMeronyms(synset));
        list.addAll(getSubstanceMeronyms(synset));
        return list;
    }

    /**
     * Returns part meronyms of <code>synset</code>.
     *
     * @param synset synset
     * @return part meronyms of <code>synset</code>
     */
    public static PointerTargetNodeList getPartMeronyms(Synset synset) {
        return getPointerTargets(synset, PointerType.PART_MERONYM);
    }

    /**
     * Returns member meronyms of <code>synset</code>.
     *
     * @param synset synset
     * @return member meronyms of <code>synset</code>
     */
    public static PointerTargetNodeList getMemberMeronyms(Synset synset) {
        return getPointerTargets(synset, PointerType.MEMBER_MERONYM);
    }

    /**
     * Returns substance meronyms of <code>synset</code>.
     *
     * @param synset synset
     * @return substance meronyms of <code>synset</code>
     */
    public static PointerTargetNodeList getSubstanceMeronyms(Synset synset) {
        return getPointerTargets(synset, PointerType.SUBSTANCE_MERONYM);
    }

    /**
     * Returns meronyms of <code>synset</code> and of all its ancestors.
     *
     * @param synset synset
     * @return meronyms of <code>synset</code> and of all its ancestors
     */
    public static PointerTargetTree getInheritedMeronyms(Synset synset) {
        return getInheritedMeronyms(synset, INFINITY, INFINITY);
    }

    /**
     * Returns meronyms of each synset, to depth <code>pointerDepth</code> starting at
     * <code>synset</code> and going for all of <code>synset</code>'s ancestors to depth
     * <code>ancestorDepth</code>.
     *
     * @param synset        synset
     * @param ancestorDepth ancestor depth
     * @param pointerDepth  pointer depth
     * @return meronyms
     */
    public static PointerTargetTree getInheritedMeronyms(Synset synset, int pointerDepth, int ancestorDepth) {
        PointerType[] types = new PointerType[3];
        types[0] = PointerType.PART_MERONYM;
        types[1] = PointerType.MEMBER_MERONYM;
        types[2] = PointerType.SUBSTANCE_MERONYM;
        return makeInheritedTree(synset, types, null, pointerDepth, ancestorDepth, false);
    }

    /**
     * Returns part meronyms of <code>synset</code> and of all its ancestors.
     *
     * @param synset synset
     * @return part meronyms of <code>synset</code> and of all its ancestors
     */
    public static PointerTargetTree getInheritedPartMeronyms(Synset synset) {
        return getInheritedPartMeronyms(synset, INFINITY, INFINITY);
    }

    /**
     * Returns part meronyms of each synset, to depth <code>pointerDepth</code>, starting at
     * <code>synset</code> and going for all of <code>synset</code>'s ancestors to depth
     * <code>ancestorDepth</code>.
     *
     * @param synset        synset
     * @param ancestorDepth ancestor depth
     * @param pointerDepth  pointer depth
     * @return part meronyms
     */
    public static PointerTargetTree getInheritedPartMeronyms(Synset synset, int pointerDepth, int ancestorDepth) {
        return makeInheritedTree(synset, PointerType.PART_MERONYM, null, pointerDepth, ancestorDepth);
    }

    /**
     * Returns member meronyms of synset and of its ancestors.
     *
     * @param synset synset
     * @return member meronyms
     */
    public static PointerTargetTree getInheritedMemberMeronyms(Synset synset) {
        return getInheritedMemberMeronyms(synset, INFINITY, INFINITY);
    }

    /**
     * Returns member meronyms of each synset, to depth <code>pointerDepth</code>, starting at
     * <code>synset</code> and going for all of <code>synset</code>'s ancestors to depth
     * <code>ancestorDepth</code>.
     *
     * @param synset        synset
     * @param ancestorDepth ancestor depth
     * @param pointerDepth  pointer depth
     * @return member meronyms
     */
    public static PointerTargetTree getInheritedMemberMeronyms(Synset synset, int pointerDepth, int ancestorDepth) {
        return makeInheritedTree(synset, PointerType.MEMBER_MERONYM, null, pointerDepth, ancestorDepth);
    }

    /**
     * Returns substance meronyms of <code>synset</code> and of its ancestors.
     *
     * @param synset synset
     * @return substance meronyms of <code>synset</code> and of its ancestors
     */
    public static PointerTargetTree getInheritedSubstanceMeronyms(Synset synset) {
        return getInheritedSubstanceMeronyms(synset, INFINITY, INFINITY);
    }

    /**
     * Returns substance meronyms of each synset, to depth <code>pointerDepth</code>, starting at
     * <code>synset</code> and going for all of <code>synset</code>'s ancestors to depth
     * <code>ancestorDepth</code>.
     *
     * @param synset        synset
     * @param ancestorDepth ancestor depth
     * @param pointerDepth  pointer depth
     * @return substance meronyms
     */
    public static PointerTargetTree getInheritedSubstanceMeronyms(Synset synset, int pointerDepth, int ancestorDepth) {
        return makeInheritedTree(synset, PointerType.SUBSTANCE_MERONYM, null, pointerDepth, ancestorDepth);
    }

    /**
     * Returns holonyms of <code>synset</code>.
     *
     * @param synset synset
     * @return holonyms of <code>synset</code>
     */
    public static PointerTargetNodeList getHolonyms(Synset synset) {
        PointerTargetNodeList list = new PointerTargetNodeList();
        list.addAll(getPartHolonyms(synset));
        list.addAll(getMemberHolonyms(synset));
        list.addAll(getSubstanceHolonyms(synset));
        return list;
    }

    /**
     * Returns part holonyms of <code>synset</code>.
     *
     * @param synset synset
     * @return part holonyms of <code>synset</code>
     */
    public static PointerTargetNodeList getPartHolonyms(Synset synset) {
        return getPointerTargets(synset, PointerType.PART_HOLONYM);
    }

    /**
     * Returns member holonyms of <code>synset</code>.
     *
     * @param synset synset
     * @return member holonyms of <code>synset</code>
     */
    public static PointerTargetNodeList getMemberHolonyms(Synset synset) {
        return getPointerTargets(synset, PointerType.MEMBER_HOLONYM);
    }

    /**
     * Returns substance holonyms of <code>synset</code>.
     *
     * @param synset synset
     * @return substance holonyms of <code>synset</code>
     */
    public static PointerTargetNodeList getSubstanceHolonyms(Synset synset) {
        return getPointerTargets(synset, PointerType.SUBSTANCE_HOLONYM);
    }

    /**
     * Returns holonyms of <code>synset</code> and of all its ancestors.
     *
     * @param synset synset
     * @return holonyms
     */
    public static PointerTargetTree getInheritedHolonyms(Synset synset) {
        return getInheritedHolonyms(synset, INFINITY, INFINITY);
    }

    /**
     * Returns holonyms of each synset, to depth <code>pointerDepth</code>, starting at <code>synset</code>
     * and going for all of <code>synset</code>'s ancestors to depth <code>ancestorDepth</code>.
     *
     * @param synset        synset
     * @param ancestorDepth ancestor depth
     * @param pointerDepth  pointer depth
     * @return holonyms
     */
    public static PointerTargetTree getInheritedHolonyms(Synset synset, int pointerDepth, int ancestorDepth) {
        PointerType[] types = new PointerType[3];
        types[0] = PointerType.PART_HOLONYM;
        types[1] = PointerType.MEMBER_HOLONYM;
        types[2] = PointerType.SUBSTANCE_HOLONYM;
        return makeInheritedTree(synset, types, null, pointerDepth, ancestorDepth, false);
    }

    /**
     * Returns part holonyms of <code>synset</code> and of all its ancestors.
     *
     * @param synset synset
     * @return part holonyms of <code>synset</code> and of all its ancestors
     */
    public static PointerTargetTree getInheritedPartHolonyms(Synset synset) {
        return getInheritedPartHolonyms(synset, INFINITY, INFINITY);
    }

    /**
     * Returns part holonyms of each synset, to depth <code>pointerDepth</code>, starting at <code>synset</code>
     * and going for all of <code>synset</code>'s ancestors to depth <code>ancestorDepth</code>.
     *
     * @param synset        synset
     * @param pointerDepth  pointer depth
     * @param ancestorDepth ancestor depth
     * @return part holonyms
     */
    public static PointerTargetTree getInheritedPartHolonyms(Synset synset, int pointerDepth, int ancestorDepth) {
        return makeInheritedTree(synset, PointerType.PART_HOLONYM, null, pointerDepth, ancestorDepth);
    }

    /**
     * Returns member holonyms of <code>synset</code> and of all its ancestors.
     *
     * @param synset synset
     * @return member holonyms of <code>synset</code> and of all its ancestors
     */
    public static PointerTargetTree getInheritedMemberHolonyms(Synset synset) {
        return getInheritedMemberHolonyms(synset, INFINITY, INFINITY);
    }

    /**
     * Returns member holonyms of each synset, to depth <code>pointerDepth</code>, starting at <code>synset</code>
     * and going for all of <code>synset</code>'s ancestors to depth <code>ancestorDepth</code>.
     *
     * @param synset        synset
     * @param pointerDepth  pointer depth
     * @param ancestorDepth ancestor depth
     * @return member holonyms of each synset
     */
    public static PointerTargetTree getInheritedMemberHolonyms(Synset synset, int pointerDepth, int ancestorDepth) {
        return makeInheritedTree(synset, PointerType.MEMBER_HOLONYM, null, pointerDepth, ancestorDepth);
    }

    /**
     * Returns substance holonyms of <code>synset</code> and of all its ancestors.
     *
     * @param synset synset
     * @return substance holonyms of <code>synset</code> and of all its ancestors
     */
    public static PointerTargetTree getInheritedSubstanceHolonyms(Synset synset) {
        return getInheritedSubstanceHolonyms(synset, INFINITY, INFINITY);
    }

    /**
     * Returns substance holonyms of each synset, to depth <code>pointerDepth</code>, starting at <code>synset</code>
     * and going for all of <code>synset</code>'s ancestors to depth <code>ancestorDepth</code>.
     *
     * @param synset        synset
     * @param pointerDepth  pointer depth
     * @param ancestorDepth ancestor depth
     * @return substance holonyms
     */
    public static PointerTargetTree getInheritedSubstanceHolonyms(Synset synset, int pointerDepth, int ancestorDepth) {
        return makeInheritedTree(synset, PointerType.SUBSTANCE_HOLONYM, null, pointerDepth, ancestorDepth);
    }

    /**
     * Finds direct entailments of <code>synset</code>.
     *
     * @param synset synset
     * @return direct entailments of <code>synset</code>
     */
    public static PointerTargetNodeList getEntailments(Synset synset) {
        return getPointerTargets(synset, PointerType.ENTAILMENT);
    }

    /**
     * Finds all entailments for <code>synset</code>.
     *
     * @param synset synset
     * @return all entailments for <code>synset</code>
     */
    public static PointerTargetTree getEntailmentTree(Synset synset) {
        return getEntailmentTree(synset, INFINITY);
    }

    /**
     * Finds all entailments for <code>synset</code> to depth <code>depth</code>.
     *
     * @param synset synset
     * @param depth  depth
     * @return all entailments for <code>synset</code> to depth <code>depth</code>
     */
    public static PointerTargetTree getEntailmentTree(Synset synset, int depth) {
        return new PointerTargetTree(synset, makePointerTargetTreeList(synset, PointerType.ENTAILMENT, depth));
    }

    /**
     * Finds direct cause links of <code>synset</code>.
     *
     * @param synset synset
     * @return direct cause links of <code>synset</code>
     */
    public static PointerTargetNodeList getCauses(Synset synset) {
        return getPointerTargets(synset, PointerType.CAUSE);
    }

    /**
     * Finds all cause links for <code>synset</code>.
     *
     * @param synset synset
     * @return all cause links for <code>synset</code>
     */
    public static PointerTargetTree getCauseTree(Synset synset) {
        return getCauseTree(synset, INFINITY);
    }

    /**
     * Finds all cause links for <code>synset</code> to depth <code>depth</code>.
     *
     * @param synset synset
     * @param depth  depth
     * @return all cause links for <code>synset</code> to depth <code>depth</code>
     */
    public static PointerTargetTree getCauseTree(Synset synset, int depth) {
        return new PointerTargetTree(synset, makePointerTargetTreeList(synset, PointerType.CAUSE, depth));
    }

    /**
     * Returns the group that this verb belongs to.
     *
     * @param synset synset
     * @return the group that this verb belongs to
     */
    public static PointerTargetNodeList getVerbGroup(Synset synset) {
        // We need to go through all this hassle because
        // 1. a verb does not always have links to all the verbs in its group
        // 2. two verbs in the same group sometimes have reciprocal links, and we want
        //    to make sure that each verb synset appears in the final list only once

        PointerTargetNodeList nodes = new PointerTargetNodeList();
        nodes.add(new PointerTargetNode(synset, PointerType.VERB_GROUP));
        int maxIndex = 0;
        int index = -1;
        do {
            index++;
            PointerTargetNode node = nodes.get(index);
            for (PointerTargetNode o : getPointerTargets(node.getSynset(), PointerType.VERB_GROUP)) {
                if (!nodes.contains(o)) {
                    nodes.add(o);
                    maxIndex++;
                }
            }
        } while (index < maxIndex);

        return nodes;
    }

    /**
     * Finds participle of links of <code>synset</code>.
     *
     * @param synset synset
     * @return participle of links of <code>synset</code>
     */
    public static PointerTargetNodeList getParticipleOf(Synset synset) {
        return getPointerTargets(synset, PointerType.PARTICIPLE_OF);
    }

    /**
     * Returns the synonyms for <var>synset</var>. This is meant for adjectives. Synonyms to
     * nouns and verbs are just their hypernyms.
     *
     * @param synset synset
     * @return the synonyms for <var>synset</var>
     */
    public static PointerTargetNodeList getSynonyms(Synset synset) {
        return getPointerTargets(synset, PointerType.SIMILAR_TO);
    }

    /**
     * Returns all the synonyms of <code>synset</code> to depth <code>depth</code>.
     *
     * @param synset synset
     * @param depth  depth
     * @return all the synonyms of <code>synset</code> to depth <code>depth</code>
     */
    public static PointerTargetTree getSynonymTree(Synset synset, int depth) {
        return new PointerTargetTree(synset, makePointerTargetTreeList(synset, PointerType.SIMILAR_TO, null, depth, false));
    }

    /**
     * Returns the pertainyms for <var>synset</var>.
     *
     * @param synset synset
     * @return the pertainyms for <var>synset</var>
     */
    public static PointerTargetNodeList getPertainyms(Synset synset) {
        return getPointerTargets(synset, PointerType.PERTAINYM);
    }

    /**
     * Returns all the pointer targets of <var>synset</var> of type <var>type</var>.
     *
     * @param synset synset
     * @param type   pointer type
     * @return all the pointer targets of <var>synset</var> of type <var>type</var>
     */
    private static PointerTargetNodeList getPointerTargets(Synset synset, PointerType type) {
        return new PointerTargetNodeList(synset.getTargets(type), type);
    }

    /**
     * Makes a nested list of pointer targets to the default depth, starting at <code>synset</code>. Each
     * level of the list is related to the previous level by a pointer of type <var>searchType</var>.
     *
     * @param synset     synset
     * @param searchType the pointer type to include in the pointer lists
     * @return a nested list of pointer targets to depth <var>depth</var>, starting at <code>synset</code>
     */
    public static PointerTargetTreeNodeList makePointerTargetTreeList(Synset synset, PointerType searchType) {
        return makePointerTargetTreeList(synset, searchType, INFINITY);
    }

    /**
     * Makes a nested list of pointer targets to depth <var>depth</var>, starting at <code>synset</code>. Each
     * level of the list is related to the previous level by a pointer of type <var>searchType</var>.
     *
     * @param synset     synset
     * @param searchType the pointer type to include in the pointer lists
     * @param depth      depth
     * @return a nested list of pointer targets to depth <var>depth</var>, starting at <code>synset</code>
     */
    public static PointerTargetTreeNodeList makePointerTargetTreeList(Synset synset, PointerType searchType, int depth) {
        return makePointerTargetTreeList(synset, searchType, null, depth, true);
    }

    /**
     * Makes a nested list of pointer targets to depth <var>depth</var>, starting at <code>synset</code>. Each
     * level of the list is related to the previous level by a pointer of type <var>searchType</var>.
     *
     * @param searchType        the pointer type to include in the pointer lists
     * @param labelType         the type used to label each pointer target in the tree
     * @param depth             depth
     * @param allowRedundancies if true, duplicate items will be included in the tree
     * @param synset            synset
     * @return a nested list of pointer targets to depth <var>depth</var>, starting at <code>synset</code>
     */
    public static PointerTargetTreeNodeList makePointerTargetTreeList(Synset synset, PointerType searchType,
                                                                      PointerType labelType, int depth,
                                                                      boolean allowRedundancies) {
        PointerType[] searchTypes = new PointerType[1];
        searchTypes[0] = searchType;
        return makePointerTargetTreeList(synset, searchTypes, labelType, depth, allowRedundancies);
    }

    /**
     * Makes a nested list of pointer targets to the default depth, starting at <code>synset</code>. Each
     * level of the list is related to the previous level by one of the pointer types specified by
     * <var>searchTypes</var>.
     *
     * @param synset      synset
     * @param searchTypes the pointer types to include in the pointer lists
     * @return a nested list of pointer targets to depth <var>depth</var>, starting at <code>synset</code>
     */
    public static PointerTargetTreeNodeList makePointerTargetTreeList(Synset synset, PointerType[] searchTypes) {
        return makePointerTargetTreeList(synset, searchTypes, INFINITY);
    }

    /**
     * Makes a nested list of pointer targets to depth <var>depth</var>, starting at <code>synset</code>. Each
     * level of the list is related to the previous level by one of the pointer types specified by
     * <var>searchTypes</var>.
     *
     * @param synset      synset
     * @param searchTypes the pointer types to include in the pointer lists
     * @param depth       depth
     * @return a nested list of pointer targets to depth <var>depth</var>, starting at <code>synset</code>
     */
    public static PointerTargetTreeNodeList makePointerTargetTreeList(Synset synset, PointerType[] searchTypes, int depth) {
        return makePointerTargetTreeList(synset, searchTypes, null, depth, true);
    }

    /**
     * Makes a nested list of pointer targets to depth <var>depth</var>, starting at <code>synset</code>. Each
     * level of the list is related to the previous level by one of the pointer types specified by
     * <var>searchTypes</var>.
     *
     * @param searchTypes       the pointer types to include in the pointer lists
     * @param labelType         the type used to label each pointer target in the tree
     * @param depth             depth
     * @param allowRedundancies if true, duplicate items will be included in the tree
     * @param synset            synset
     * @return a nested list of pointer targets to depth <var>depth</var>, starting at <code>synset</code>
     */
    public static PointerTargetTreeNodeList makePointerTargetTreeList(Synset synset, PointerType[] searchTypes,
                                                                      PointerType labelType, int depth,
                                                                      boolean allowRedundancies) {
        return makePointerTargetTreeList(synset, searchTypes, labelType, depth, allowRedundancies, null);
    }

    /**
     * Makes a nested list of pointer targets to depth <var>depth</var>, starting at each <code>synset</code> in
     * <var>list</var>. Each level of the list is related to the previous level by a pointer of type
     * <var>searchType</var>.
     *
     * @param list              list
     * @param searchType        the pointer type to include in the pointer lists
     * @param labelType         the type used to label each pointer target in the tree
     * @param depth             depth
     * @param allowRedundancies if true, duplicate items will be included in the tree
     * @return a nested list of pointer targets to depth <var>depth</var>, starting at each <code>synset</code> in <var>list</var>
     */
    public static PointerTargetTreeNodeList makePointerTargetTreeList(PointerTargetNodeList list, PointerType searchType,
                                                                      PointerType labelType, int depth,
                                                                      boolean allowRedundancies) {
        PointerType[] searchTypes = new PointerType[1];
        searchTypes[0] = searchType;
        return makePointerTargetTreeList(list, searchTypes, labelType, depth, allowRedundancies);
    }

    /**
     * Makes a nested list of pointer targets to depth <var>depth</var>, starting at each <code>synset</code> in
     * <var>list</var>. Each level of the list is related to the previous level by one of the pointer types specified
     * by <var>searchTypes</var>.
     *
     * @param searchTypes       the pointer types to include in the pointer lists
     * @param labelType         the type used to label each pointer target in the tree
     * @param depth             depth
     * @param allowRedundancies if true, duplicate items will be included in the tree
     * @param list              list
     * @return a nested list of pointer targets to depth <var>depth</var>, starting at each <code>synset</code> in <var>list</var>
     */
    public static PointerTargetTreeNodeList makePointerTargetTreeList(PointerTargetNodeList list, PointerType[] searchTypes,
                                                                      PointerType labelType, int depth,
                                                                      boolean allowRedundancies) {
        PointerTargetTreeNodeList treeList = new PointerTargetTreeNodeList();
        for (PointerTargetNode node : list) {
            treeList.add(node.getPointerTarget(),
                    makePointerTargetTreeList(node.getSynset(), searchTypes, labelType, depth, allowRedundancies),
                    labelType);
        }
        return treeList;
    }

    private static PointerTargetTreeNodeList makePointerTargetTreeList(Synset synset, PointerType[] searchTypes,
                                                                       PointerType labelType, int depth,
                                                                       boolean allowRedundancies,
                                                                       PointerTargetTreeNode parent) {
        depth--;
        PointerTargetTreeNodeList list = new PointerTargetTreeNodeList();
        for (PointerType type : searchTypes) {
            PointerTargetNodeList targets = new PointerTargetNodeList(synset.getTargets(type), type);
            if (targets.size() > 0) {
                for (PointerTargetNode ptr : targets) {
                    ptr.getSynset();
                    PointerTargetTreeNode node =
                            new PointerTargetTreeNode(ptr.getPointerTarget(),
                                    labelType == null ? type : labelType, parent);
                    if (allowRedundancies || !list.contains(node)) {
                        if (depth != 0) {
                            //check cycles through parent
                            Set<PointerTargetTreeNode> parents = new HashSet<PointerTargetTreeNode>();
                            PointerTargetTreeNode currentParent = parent;
                            while (null != currentParent) {
                                if (!parents.contains(currentParent)) {
                                    parents.add(currentParent);
                                    currentParent = currentParent.getParent();
                                } else {
                                    //cycle
                                    if (log.isWarnEnabled()) {
                                        log.warn(JWNL.resolveMessage("DICTIONARY_WARN_001", currentParent));
                                    }
                                    break;
                                }
                            }
                            if (null == currentParent) {
                                node.setChildTreeList(makePointerTargetTreeList(node.getSynset(), searchTypes, labelType,
                                        depth, allowRedundancies, node));
                            }
                        }
                        list.add(node);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Creates a hypernym tree starting at <var>synset</var>, and add to each node a nested list pointer targets of type
     * <var>searchType</var>, starting at the node's pointer target. This method uses the default depths.
     *
     * @param synset     synset
     * @param searchType the pointer type to include in the pointer lists
     * @return a hypernym tree starting at <var>synset</var>
     */
    public static PointerTargetTree makeInheritedTree(Synset synset, PointerType searchType) {
        return makeInheritedTree(synset, searchType, null, INFINITY, INFINITY);
    }

    /**
     * Creates a hypernym tree starting at <var>synset</var>, and add to each node a nested list pointer targets of type
     * <var>searchType</var>, starting at the node's pointer target.
     *
     * @param searchType    pointer type
     * @param labelType     the type used to label each pointer target in the tree
     * @param pointerDepth  the depth to which to search for each pointer list
     * @param ancestorDepth the depth to which to go to in the hypernym list
     * @param synset        synset
     * @return a hypernym tree starting at <var>synset</var>
     */
    public static PointerTargetTree makeInheritedTree(Synset synset, PointerType searchType, PointerType labelType,
                                                      int pointerDepth, int ancestorDepth) {
        return makeInheritedTree(synset, searchType, labelType, pointerDepth, ancestorDepth, true);
    }

    /**
     * Creates a hypernym tree starting at <var>synset</var>, and add to each node a nested list pointer targets of type
     * <var>searchType</var>, starting at the node's pointer target.
     *
     * @param searchType        pointer type
     * @param labelType         the type used to label each pointer target in the tree
     * @param pointerDepth      the depth to which to search for each pointer list
     * @param ancestorDepth     the depth to which to go to in the hypernym list
     * @param allowRedundancies if true, duplicate items are allowed in the list
     * @param synset            synset
     * @return a hypernym tree starting at <var>synset</var>
     */
    public static PointerTargetTree makeInheritedTree(Synset synset, PointerType searchType, PointerType labelType,
                                                      int pointerDepth, int ancestorDepth, boolean allowRedundancies) {
        PointerType[] searchTypes = new PointerType[1];
        searchTypes[0] = searchType;
        return makeInheritedTree(synset, searchTypes, labelType, pointerDepth, ancestorDepth, allowRedundancies);
    }

    /**
     * Creates a hypernym tree starting at <var>synset</var>, and add to each node a nested list pointer targets of
     * the types specified in <var>searchTypes</var>, starting at the node's pointer target. This method uses the
     * default depths.
     *
     * @param synset      synset
     * @param searchTypes the pointer types to include in the pointer lists
     * @return hypernym tree starting at <var>synset</var>
     */
    public static PointerTargetTree makeInheritedTree(Synset synset, PointerType[] searchTypes) {
        return makeInheritedTree(synset, searchTypes, null, INFINITY, INFINITY);
    }

    /**
     * Creates a hypernym tree starting at <var>synset</var>, and add to each node a nested list pointer targets of
     * the types specified in <var>searchTypes</var>, starting at the node's pointer target.
     *
     * @param searchTypes   the pointer types to include in the pointer lists
     * @param labelType     the type used to label each pointer target in the tree
     * @param pointerDepth  the depth to which to search for each pointer list
     * @param ancestorDepth the depth to which to go to in the hypernym list
     * @param synset        synset
     * @return hypernym tree starting at <var>synset</var>
     */
    public static PointerTargetTree makeInheritedTree(Synset synset, PointerType[] searchTypes, PointerType labelType,
                                                      int pointerDepth, int ancestorDepth) {
        return makeInheritedTree(synset, searchTypes, labelType, pointerDepth, ancestorDepth, true);
    }

    /**
     * Creates a hypernym tree starting at <var>synset</var>, and add to each node a nested list pointer targets of
     * the types specified in <var>searchTypes</var>, starting at the node's pointer target.
     *
     * @param searchTypes       the pointer types to include in the pointer lists
     * @param labelType         the type used to label each pointer target in the tree
     * @param pointerDepth      the depth to which to search for each pointer list
     * @param ancestorDepth     the depth to which to go to in the hypernym list
     * @param allowRedundancies if true, duplicate items are allowed in the list
     * @param synset            synset
     * @return hypernym tree starting at <var>synset</var>
     */
    public static PointerTargetTree makeInheritedTree(Synset synset, PointerType[] searchTypes, PointerType labelType,
                                                      int pointerDepth, int ancestorDepth, boolean allowRedundancies) {
        PointerTargetTree hypernyms = getHypernymTree(synset, INFINITY);
        return makeInheritedTree(hypernyms, searchTypes, labelType, pointerDepth, ancestorDepth, allowRedundancies);
    }

    /**
     * Turn an existing tree into an inheritance tree.
     *
     * @param tree              the tree to convert
     * @param searchTypes       the pointer types to include in the pointer lists
     * @param labelType         the type used to label each pointer target in the tree
     * @param pointerDepth      the depth to which to search for each pointer list
     * @param ancestorDepth     the depth to which to go to in <code>tree</code>
     * @param allowRedundancies if true, duplicate items are allowed in the list
     * @return inheritance tree
     */
    public static PointerTargetTree makeInheritedTree(PointerTargetTree tree, PointerType[] searchTypes,
                                                      PointerType labelType, int pointerDepth, int ancestorDepth,
                                                      boolean allowRedundancies) {
        PointerTargetTreeNode root = tree.getRootNode();
        root.setPointerTreeList(makePointerTargetTreeList(root.getSynset(), searchTypes, labelType, pointerDepth, allowRedundancies));
        root.setChildTreeList(makeInheritedTreeList(root.getChildTreeList(), searchTypes, labelType, pointerDepth,
                ancestorDepth, allowRedundancies));
        return new PointerTargetTree(root);
    }

    /**
     * Turn an existing tree list into an inheritance tree list.
     *
     * @param list              the tree list to convert
     * @param searchTypes       the pointer types to include in the pointer lists
     * @param labelType         the type used to label each pointer target in the tree
     * @param pointerDepth      the depth to which to search for each pointer list
     * @param ancestorDepth     the depth to which to go to in <code>tree</code>
     * @param allowRedundancies if true, duplicate items are allowed in the list
     * @return inheritance tree list
     */
    public static PointerTargetTreeNodeList makeInheritedTreeList(PointerTargetTreeNodeList list,
                                                                  PointerType[] searchTypes, PointerType labelType,
                                                                  int pointerDepth, int ancestorDepth,
                                                                  boolean allowRedundancies) {
        ancestorDepth--;
        PointerTargetTreeNodeList inherited = new PointerTargetTreeNodeList();
        if (null != list) {//AA: cycle with "period"...
            for (PointerTargetTreeNode node : list) {
                if (allowRedundancies || !inherited.contains(node)) {
                    if (ancestorDepth == 0) {
                        inherited.add(node.getPointerTarget(),
                                null,
                                makePointerTargetTreeList(node.getSynset(), searchTypes, labelType, pointerDepth, allowRedundancies),
                                PointerType.HYPERNYM);
                    } else {
                        inherited.add(node.getPointerTarget(),
                                makeInheritedTreeList(node.getChildTreeList(), searchTypes, labelType,
                                        pointerDepth, ancestorDepth, allowRedundancies),
                                makePointerTargetTreeList(node.getSynset(), searchTypes, labelType, pointerDepth, allowRedundancies),
                                PointerType.HYPERNYM);
                    }
                }
            }
        }
        return inherited;
    }
}