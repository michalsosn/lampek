package pl.lodz.p.michalsosn.security;

/**
 * @author Michał Sośnicki
 */
public class NotAuthenticatedException extends RuntimeException {
    public NotAuthenticatedException() {
    }

    public NotAuthenticatedException(String message) {
        super(message);
    }
}
