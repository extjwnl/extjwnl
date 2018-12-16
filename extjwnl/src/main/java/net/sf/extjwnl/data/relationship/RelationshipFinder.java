package net.sf.extjwnl.data.relationship;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.list.*;

import java.util.List;

/**
 * Helper class to find relations.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class RelationshipFinder {

    private static final int DEFAULT_ASYMMETRIC_SEARCH_DEPTH = Integer.MAX_VALUE;
    private static final int DEFAULT_SYMMETRIC_SEARCH_DEPTH = 2;

    /**
     * Looks whether the target word is one of the words in one of the synsets
     * of the source word.
     *
     * @param sourceWord source word
     * @param targetWord target word
     * @return int the sense of the source word that contains the target word
     */
    public static int getImmediateRelationship(IndexWord sourceWord, IndexWord targetWord) {
        List<Synset> senses = sourceWord.getSenses();
        String lemma = targetWord.getLemma();
        for (int i = 0; i < senses.size(); i++) {
            if (senses.get(i).containsWord(lemma)) {
                return i + 1;
            }
        }
        return -1;
    }

    /**
     * Finds all relationships of type <var>type</var> between <var>sourceSynset</var> and <var>targetSynset</var>.
     * This method creates a symmetric or asymmetric relationship based on whether <var>type</var> is symmetric.
     *
     * @param sourceSynset source synset
     * @param targetSynset target synset
     * @param type         pointer type
     * @return all relationships of type <var>type</var> between <var>sourceSynset</var> and <var>targetSynset</var>
     * @throws CloneNotSupportedException CloneNotSupportedException
	 * @throws JWNLException JWNLException
	 */
    public static RelationshipList findRelationships(
            Synset sourceSynset, Synset targetSynset, PointerType type) throws CloneNotSupportedException, JWNLException {

        return (type.isSymmetric()) ?
                findSymmetricRelationships(sourceSynset, targetSynset, type) :
                findAsymmetricRelationships(sourceSynset, targetSynset, type);
    }

    /**
     * Finds all relationships of type <var>type</var> between <var>sourceSynset</var> and <var>targetSynset</var>
     * to depth <var>depth</var>. This method creates a symmetric or asymmetric relationship based on
     * whether <var>type</var> is symmetric.
     *
     * @param sourceSynset source synset
     * @param targetSynset target synset
     * @param type         pointer type
     * @param depth        depth
     * @return all relationships of type <var>type</var> between <var>sourceSynset</var> and <var>targetSynset</var>
     * @throws CloneNotSupportedException CloneNotSupportedException
	 * @throws JWNLException JWNLException
     */
    public static RelationshipList findRelationships(
            Synset sourceSynset, Synset targetSynset, PointerType type, int depth) throws CloneNotSupportedException, JWNLException {

        return (type.isSymmetric()) ?
                findSymmetricRelationships(sourceSynset, targetSynset, type, depth) :
                findAsymmetricRelationships(sourceSynset, targetSynset, type, depth);
    }

    /**
     * Finds the asymmetric relationship(s) between two words. A relationship is
     * asymmetric if its type is asymmetric (i.e. it's not its own inverse).
     *
     * @param sourceSynset source synset
     * @param targetSynset target synset
     * @param type         pointer type
     * @return all relationships of type <var>type</var> between <var>sourceSynset</var> and <var>targetSynset</var>
     * @throws CloneNotSupportedException CloneNotSupportedException
     */
    private static RelationshipList findAsymmetricRelationships(
            Synset sourceSynset, Synset targetSynset, PointerType type) throws CloneNotSupportedException, JWNLException {

        return findAsymmetricRelationships(sourceSynset, targetSynset, type, DEFAULT_ASYMMETRIC_SEARCH_DEPTH);
    }

    /**
     * Finds the asymmetric relationship(s) between two words. A relationship is
     * asymmetric if its type is asymmetric (i.e. it's not its own inverse).
     *
     * @param sourceSynset source synset
     * @param targetSynset target synset
     * @param type         pointer type
     * @param depth        depth
     * @return all relationships of type <var>type</var> between <var>sourceSynset</var> and <var>targetSynset</var>
     * @throws CloneNotSupportedException CloneNotSupportedException
     */
    private static RelationshipList findAsymmetricRelationships(
            Synset sourceSynset, Synset targetSynset, PointerType type, int depth) throws CloneNotSupportedException, JWNLException {

        // We run the reversal function on the trees to get linear (non-branching)
        // paths from the source word to its deepest ancestor (i.e. if there are
        // multiple relations from a single word anywhere in the path, the reversal
        // function will break them down into multiple, linear paths).
        List<PointerTargetNodeList> sourceRelations = new PointerTargetTree(
                sourceSynset, PointerUtils.makePointerTargetTreeList(sourceSynset, type, depth)).reverse();
        List<PointerTargetNodeList> targetRelations = new PointerTargetTree(
                targetSynset, PointerUtils.makePointerTargetTreeList(targetSynset, type, depth)).reverse();

        RelationshipList relationships = new RelationshipList();
        // Do an exhaustive search for relationships
        for (PointerTargetNodeList sourceRelation : sourceRelations) {
            for (PointerTargetNodeList targetRelation : targetRelations) {
                Relationship relationship = findAsymmetricRelationship(
                        sourceRelation, targetRelation, type, sourceSynset, targetSynset);
                if (relationship != null) {
                    relationships.add(relationship);
                }
            }
        }
        return relationships;
    }

    /**
     * Finds a relationship between two asymmetric lists ordered from deepest
     * to shallowest ancestor. Each node has it's PointerType set to the kind of
     * relationship one need to follow to get from it to the next node in the list.
     * Take the dog/cat relationship. To get to carnivore, a hypernym relationship
     * must be used to get from dog to carnivore, but then a hyponym relationship
     * must be used to get from carnivore to cat. The list will look like this:
     * dog(hyper) -> canine(hyper) -> carnivore(hypo) -> feline(hypo) -> cat(hypo).
     * In this instance, cat's PointerType is meaningless, but is kept to facilitate
     * things like reversing the relationship (which just involves setting each node's
     * pointer type to the symmetric type of its current type.
     *
     * @param sourceNodes  source nodes
     * @param targetNodes  target nodes
     * @param type         pointer type
     * @param sourceSynset source synset
     * @param targetSynset target synset
     * @return relationship
     * @throws CloneNotSupportedException CloneNotSupportedException
     */
    private static Relationship findAsymmetricRelationship(
            PointerTargetNodeList sourceNodes, PointerTargetNodeList targetNodes,
            PointerType type, Synset sourceSynset, Synset targetSynset) throws CloneNotSupportedException {

        PointerTargetNode sourceRoot = sourceNodes.get(0);
        PointerTargetNode targetRoot = targetNodes.get(0);
        // If the deepest ancestors of the words are not the same,
        // then there is no relationship between the words.
        if (!sourceRoot.getSynset().equals(targetRoot.getSynset())) {
            return null;
        }

        PointerTargetNodeList relationship = new PointerTargetNodeList();
        int targetStart = 0;
        int commonParentIndex = 0;
        for (int i = sourceNodes.size() - 1; i >= 0; i--) {
            PointerTargetNode testNode = sourceNodes.get(i);
            int idx = targetNodes.indexOf(testNode);
            if (idx >= 0) {
                targetStart = idx;
                break;
            } else {
                relationship.add(testNode.clone());
                commonParentIndex++;
            }
        }
        for (int i = targetStart; i < targetNodes.size(); i++) {
            PointerTargetNode node = targetNodes.get(i).clone();
            node.setType(type.getSymmetricType());
            relationship.add(node);
        }
        return new AsymmetricRelationship(type, relationship, commonParentIndex, sourceSynset, targetSynset);
    }

    /**
     * A symmetric relationship is one whose type is symmetric (i.e. is it's own
     * inverse. An example of a symmetric relationship is synonymy.
     *
     * @param type         pointer type
     * @param sourceSynset source synset
     * @param targetSynset target synset
     * @return list of symmetric relationships between source and target
     */
    private static RelationshipList findSymmetricRelationships(
            Synset sourceSynset, Synset targetSynset, PointerType type) throws JWNLException {

        return findSymmetricRelationships(sourceSynset, targetSynset, type, DEFAULT_SYMMETRIC_SEARCH_DEPTH);
    }

    /**
     * A symmetric relationship is one whose type is symmetric (i.e. is it's own inverse).
     *
     * @param type         pointer type
     * @param sourceSynset source synset
     * @param targetSynset target synset
     * @param depth        depth
     * @return list of symmetric relationships between source and target
     */
    private static RelationshipList findSymmetricRelationships(
            final Synset sourceSynset, final Synset targetSynset, PointerType type, int depth) throws JWNLException {

        PointerTargetTree tree = new PointerTargetTree(
                sourceSynset, PointerUtils.makePointerTargetTreeList(sourceSynset, type, null, depth, false));

        PointerTargetTreeNodeList.Operation opr = testNode -> {
            if (targetSynset.equals(testNode.getSynset())) {

                return testNode;
            }
            return null;
        };
        List l = tree.getAllMatches(opr);

        RelationshipList list = new RelationshipList();
        for (Object aL : l) {
            PointerTargetNodeList nodes = findSymmetricRelationship((PointerTargetTreeNode) aL, type);
            list.add(new SymmetricRelationship(type, nodes, sourceSynset, targetSynset));
        }
        return list;
    }

    /**
     * Builds a relationship from <var>node</var> back to it's root ancestor and
     * then reverse the list.
     *
     * @param node node to start with
     * @param type pointer type
     * @return list of relationships from root ancestor to the node
     */
    private static PointerTargetNodeList findSymmetricRelationship(PointerTargetTreeNode node, PointerType type) {
        PointerTargetNodeList list = new PointerTargetNodeList();
        buildSymmetricRelationshipList(list, node);
        list = list.reverse();
        // set the root's pointer type
        list.get(0).setType(type);
        return list;
    }

    /**
     * Builds the symmetric relationship list.
     *
     * @param list list to populate
     * @param node node to start with
     */
    private static void buildSymmetricRelationshipList(PointerTargetNodeList list, PointerTargetTreeNode node) {
        list.add(node.getPointerTarget(), node.getType());
        if (node.getParent() != null) {
            buildSymmetricRelationshipList(list, node.getParent());
        }
    }
}