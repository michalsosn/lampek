package pl.lodz.p.michalsosn.security;

/**
 * @author Michał Sośnicki
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException() {
    }

    public ForbiddenException(String message) {
        super(message);
    }
}
