package net.sf.extjwnl;

/**
 * Base level exception.
 *
 * @author John Didion (jdidion@didion.net)
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class JWNLException extends Exception {

    public JWNLException(Throwable cause) {
        super(cause);
    }

    public JWNLException(String message) {
        super(message);
    }

    public JWNLException(String message, Throwable cause) {
        super(message, cause);
    }
}