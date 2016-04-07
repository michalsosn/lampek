package pl.lodz.p.michalsosn.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import pl.lodz.p.michalsosn.security.ForbiddenException;
import pl.lodz.p.michalsosn.security.NotAuthenticatedException;

import java.util.NoSuchElementException;

/**
 * @author Michał Sośnicki
 */
@ControllerAdvice
public class GeneralRestControllerAdvice {

    private final Logger log
            = LoggerFactory.getLogger(GeneralRestControllerAdvice.class);

    @ResponseBody
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    VndErrors handleNoSuchElementException(NoSuchElementException ex) {
        log.warn("NoSuchElementException handled.", ex);
        return new VndErrors("error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    VndErrors handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException handled.", ex);
        return new VndErrors("error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    VndErrors handleNullPointerException(NullPointerException ex) {
        log.error("NullPointerException handled.", ex);
        return new VndErrors("error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    VndErrors handleForbiddenException(ForbiddenException ex) {
        log.warn("ForbiddenException handled.", ex);
        return new VndErrors("error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(NotAuthenticatedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    VndErrors handleNotAuthenticatedException(NotAuthenticatedException ex) {
        log.warn("NotAuthenticatedException handled.", ex);
        return new VndErrors("error", ex.getMessage());
    }

}
