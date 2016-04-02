package pl.lodz.p.michalsosn.rest;

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
@ControllerAdvice(assignableTypes = ImageRestController.class)
public class ImageRestControllerAdvice {

    @ResponseBody
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    VndErrors handleNoSuchElementException(NoSuchElementException ex) {
        return new VndErrors("error", ex.getMessage());
    }

}
