package net.sf.extjwnl.util;

import net.sf.extjwnl.JWNL;

import java.io.Serializable;

/**
 * Implements lazy resolving for a resource key.
 *
 * @author didion
 * @author Aliaksandr Autayeu avtaev@gmail.com
 */
public class Resolvable implements Serializable {

    private static final long serialVersionUID = 1L;

    private String unresolved = null;
    private transient String resolved = null;

    public Resolvable(String msg) {
        unresolved = msg;
    }

    public String toString() {
        if (resolved == null) {
            resolved = JWNL.resolveMessage(unresolved);
        }
        return resolved;
    }
}
