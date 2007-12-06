package net.didion.jwnl.util;

import net.didion.jwnl.JWNL;

import java.io.Serializable;

/** Implements lazy resolving for a resource key */
public class Resolvable implements Serializable {
	static final long serialVersionUID = 4753740475813500883L;

	private String _unresolved = null;
	private transient String _resolved = null;

	public Resolvable(String msg) {
		_unresolved = msg;
	}

	public String toString() {
		if (_resolved == null)
			_resolved = JWNL.resolveMessage(_unresolved);
		return _resolved;
	}
}
