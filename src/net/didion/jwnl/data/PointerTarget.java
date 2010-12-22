/**
 * Java WordNet Library (JWNL)
 * See the documentation for copyright information.
 */
package net.didion.jwnl.data;import net.didion.jwnl.JWNLException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * A <code>PointerTarget</code> is the source or target of a <code>Pointer</code>.
 * The target of a semantic <code>PointerTarget</code> is a <code>Synset</code>;
 * the target of a lexical <code>PointerTarget</code> is a <code>Word</code>.
 */
public abstract class PointerTarget implements Serializable {
	static final long serialVersionUID = 3230195199146939027L;

	protected PointerTarget() {
	}

	/** Return this target's POS */
	public abstract POS getPOS();

    public abstract Synset getSynset();

    public abstract int getIndex();

	/** Return a list of Target's pointers */
	public abstract Pointer[] getPointers();

	public abstract String toString();

	public boolean equals(Object obj) {
		return (obj instanceof PointerTarget) && ((PointerTarget)obj).getPOS().equals(getPOS());
	}

	/** Get all pointers of type <code>type</code>.*/
	public Pointer[] getPointers(PointerType type) {
		List<Pointer> list = new ArrayList<Pointer>();
		Pointer[] pointers = getPointers();
        for (Pointer pointer : pointers) {
            if (pointer.getType().equals(type)
                    || type.equals(PointerType.HYPERNYM) && pointer.getType().equals(PointerType.INSTANCE_HYPERNYM)
                    || type.equals(PointerType.HYPONYM) && pointer.getType().equals(PointerType.INSTANCES_HYPONYM)) {
                list.add(pointer);
            }
        }
		return list.toArray(new Pointer[list.size()]);
	}

	/** Get all the pointer targets of this synset */
	public PointerTarget[] getTargets() throws JWNLException {
		return collectTargets(getPointers());
	}

	/** Get all the targets of the pointers of type <code>type</code>.*/
	public PointerTarget[] getTargets(PointerType type) throws JWNLException {
		return collectTargets(getPointers(type));
	}

	/** Get an array of all the targets of <code>pointers</code>.*/
	private PointerTarget[] collectTargets(Pointer[] pointers) throws JWNLException {
		PointerTarget[] targets = new PointerTarget[pointers.length];
		for (int i = 0; i < pointers.length; ++i)
			targets[i] = pointers[i].getTarget();
		return targets;
	}
}