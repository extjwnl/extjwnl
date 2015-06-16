package net.sf.extjwnl;

/**
 * IO exception wrapper.
 *
 * @author <a href="http://autayeu.com/">Aliaksandr Autayeu</a>
 */
public class JWNLIOException extends JWNLException {

    public JWNLIOException(Throwable cause) {
        super(cause);
    }

    public JWNLIOException(String message) {
        super(message);
    }

    public JWNLIOException(String message, Throwable cause) {
        super(message, cause);
    }
}