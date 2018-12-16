package net.sf.extjwnl.data;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;
import net.sf.extjwnl.util.factory.Owned;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A <code>PointerTarget</code> is the source or target of a <code>Pointer</code>.
 * The target of a semantic <code>PointerTarget</code> is a <code>Synset</code>;
 * the target of a lexical <code>PointerTarget</code> is a <code>Word</code>.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public abstract class PointerTarget implements Serializable, Owned {

	private static final long serialVersionUID = 5L;

	protected transient Dictionary dictionary;

	protected PointerTarget(Dictionary dictionary) {
		this.dictionary = dictionary;
	}

	public abstract POS getPOS();

	public abstract Synset getSynset();

	public abstract int getIndex();

	/**
	 * Returns a list of Target's pointers.
	 *
	 * @return a list of Target's pointers
	 */
	public abstract List<Pointer> getPointers();

	public abstract String toString();

	/**
	 * Returns all pointers of type <var>type</var>.
	 *
	 * @param type pointer type
	 * @return all pointers of type <var>type</var>
	 */
	public List<Pointer> getPointers(PointerType type) {
		List<Pointer> result = new ArrayList<>();
		for (Pointer pointer : getPointers()) {
			if (pointer.getType().equals(type)
					|| PointerType.HYPERNYM == type && PointerType.INSTANCE_HYPERNYM == pointer.getType()
					|| PointerType.HYPONYM == type && PointerType.INSTANCES_HYPONYM == pointer.getType()) {
				result.add(pointer);
			}
		}
		return result;
	}

	/**
	 * Returns all the pointer targets of this synset.
	 *
	 * @return all the pointer targets of this synset
	 * @throws JWNLException JWNLException
	 */
	public List<PointerTarget> getTargets() throws JWNLException {
		return collectTargets(getPointers());
	}

	/**
	 * Returns all the targets of the pointers of type <var>type</var>.
	 *
	 * @param type pointer type
	 * @return all the targets of the pointers of type <var>type</var>
	 * @throws JWNLException JWNLException
	 */
	public List<PointerTarget> getTargets(PointerType type) throws JWNLException {
		return collectTargets(getPointers(type));
	}

	/**
	 * Returns of all the targets of <var>pointers</var>.
	 *
	 * @param pointers pointer to return targets of
	 * @return all the targets of <var>pointers</var>
	 * @throws JWNLException JWNLException
	 */
	private List<PointerTarget> collectTargets(List<Pointer> pointers) throws JWNLException {
		List<PointerTarget> targets = new ArrayList<>(pointers.size());
		for (Pointer pointer : pointers) {
			targets.add(pointer.getTarget());
		}
		return targets;
	}

	public Dictionary getDictionary() {
		return dictionary;
	}

	public void setDictionary(Dictionary dictionary) throws JWNLException {
		this.dictionary = dictionary;
	}
}