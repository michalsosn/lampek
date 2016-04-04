package pl.lodz.p.michalsosn.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.NoSuchElementException;

/**
 * @author Michał Sośnicki
 */
@ControllerAdvice//(assignableTypes = ImageRestController.class)
public class GeneralRestControllerAdvice {

    private final Logger log
            = LoggerFactory.getLogger(GeneralRestControllerAdvice.class);

    @ResponseBody
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    VndErrors handleNoSuchElementException(NoSuchElementException ex) {
        log.error("NoSuchElementException handled.", ex);
        return new VndErrors("error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    VndErrors handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("IllegalArgumentException handled.", ex);
        return new VndErrors("error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    VndErrors handleNullPointerException(NullPointerException ex) {
        log.error("NullPointerException handled.", ex);
        return new VndErrors("error", ex.getMessage());
    }

}
