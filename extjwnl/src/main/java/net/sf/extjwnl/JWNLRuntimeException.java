package net.sf.extjwnl;

/**
 * Base level runtime exception.
 *
 * @author John Didion <jdidion@didion.net>
 * @author <a rel="author" href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class JWNLRuntimeException extends RuntimeException {

    public JWNLRuntimeException(Throwable cause) {
        super(cause);
    }

    public JWNLRuntimeException(String message) {
        super(message);
    }

    public JWNLRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}